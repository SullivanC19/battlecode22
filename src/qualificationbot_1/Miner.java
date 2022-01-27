package qualificationbot_1;

import battlecode.common.*;

import java.nio.file.Path;

public class Miner {

    public static void run() throws GameActionException {
        Communication.updateArchonIdx();
        Communication.resetBlockInfoBroadcasted();

        // NOTE remember to pull counts before this
        Communication.updateDroidCount();

        MapLocation myBlock = Utils.getMyBlock();
        MapLocation myLoc = Memory.rc.getLocation();

        RobotInfo[] enemyRobots = Memory.rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, Memory.rc.getTeam().opponent());
        RobotInfo closestEnemyThreat = null;
        int closestEnemyThreatDist = Integer.MAX_VALUE;
        if (enemyRobots.length > 0) {
            for (int i = 0; i < enemyRobots.length; i++) {
                int dist = myLoc.distanceSquaredTo(enemyRobots[i].getLocation());
                RobotType type = enemyRobots[i].getType();
                if ((type == RobotType.WATCHTOWER || type == RobotType.SOLDIER || type == RobotType.SAGE)
                && dist < closestEnemyThreatDist) {
                    closestEnemyThreat = enemyRobots[i];
                    closestEnemyThreatDist = dist;
                }
            }

            Communication.addBlockInfo(
                    closestEnemyThreat != null ? Communication.BLOCK_TYPE_THREAT : Communication.BLOCK_TYPE_NONTHREAT,
                    myBlock);
        }

        // micro toward lead blocks
        MapLocation[] leadLocs = Memory.rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared, Archon.MIN_MINEABLE_LEAD);

        boolean blockHasLead = false;
        boolean blockHasMineableLead = false;
        int closestMiningLocDist = Integer.MAX_VALUE;
        MapLocation closestMiningLoc = null;
        for (int i = 0; i < leadLocs.length; i++) {
            int lead = Memory.rc.senseLead(leadLocs[i]);
            MapLocation block = Utils.getBlock(leadLocs[i]);
            boolean inMyBlock = block.equals(myBlock);
            int dist = Utils.dist(myLoc, leadLocs[i]);

            if (lead >= Archon.MIN_MINEABLE_LEAD) {
                Communication.addBlockInfo(Communication.BLOCK_TYPE_MINEABLE, block);
                blockHasMineableLead |= inMyBlock;

                if (dist < closestMiningLocDist
                        && (closestEnemyThreat == null
                        || closestEnemyThreat.getLocation().distanceSquaredTo(leadLocs[i]) > closestEnemyThreat.getType().actionRadiusSquared)) {
                    closestMiningLoc = leadLocs[i];
                    closestMiningLocDist = dist;
                }
            }

            blockHasLead |= block.equals(myBlock);
        }

        if (!blockHasMineableLead) {
            Communication.addBlockInfo(
                    blockHasLead ? Communication.BLOCK_TYPE_UNMINEABLE : Communication.BLOCK_TYPE_NO_LEAD,
                    myBlock);
        }

        if (closestEnemyThreat != null) {
            Pathfinder.moveTo(Memory.archonLocation);
        } else if (closestMiningLoc != null) {
            // micro
            if (closestMiningLocDist <= 3) {
                int lowestRubble = Memory.rc.senseRubble(closestMiningLoc);
                Direction bestDir = Direction.CENTER;
                for (Direction dir : Direction.allDirections()) {
                    MapLocation adjLoc = closestMiningLoc.add(dir);
                    if (dir == Direction.CENTER
                            || (Memory.rc.canSenseLocation(adjLoc)
                            && !Memory.rc.isLocationOccupied(adjLoc))) {
                        int rubble = Memory.rc.senseRubble(adjLoc);
                        if (rubble < lowestRubble) {
                            lowestRubble = rubble;
                            bestDir = dir;
                        }
                    }
                }

                closestMiningLoc = closestMiningLoc.add(bestDir);
            }

            Pathfinder.moveTo(closestMiningLoc);
        } else {
            // find target block
            int distToTargetBlock = Integer.MAX_VALUE;
            MapLocation targetBlock = null;

            int numArchons = Memory.rc.getArchonCount();

            // mining blocks
            for (int i = 0; i < Communication.NUM_TARGET_BLOCKS; i++) {
                MapLocation targetMiningBlock = Communication.getTargetMiningBlock(i);
                if (targetMiningBlock == null) continue;
                int dist = Utils.dist(targetMiningBlock, myBlock) + Memory.rc.getRoundNum();
                if (dist < distToTargetBlock) {
                    targetBlock = targetMiningBlock;
                    distToTargetBlock = dist;
                }
            }

            // explore blocks
            for (int i = 0; i < Communication.NUM_TARGET_BLOCKS; i++) {
                MapLocation targetExploreBlock = Communication.getTargetExploreBlock(i);
                if (targetExploreBlock == null) continue;
                int dist = Utils.dist(targetExploreBlock, myBlock);
                if (dist < distToTargetBlock) {
                    targetBlock = targetExploreBlock;
                    distToTargetBlock = dist;
                }
            }

            if (targetBlock != null) Pathfinder.moveTo(Utils.getCenterOfBlock(targetBlock));
        }

        // mine
        for (Direction dir : Direction.allDirections()) {
            MapLocation loc = Memory.rc.getLocation().add(dir);
            while (Memory.rc.canMineLead(loc) && Memory.rc.senseLead(loc) > 1) {
                Memory.rc.mineLead(loc);
            }
        }

    }

}