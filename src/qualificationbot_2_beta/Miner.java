package qualificationbot_2_beta;

import battlecode.common.*;

public class Miner {

    public static int MIN_TARGETABLE_LEAD = 11;

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

        MapLocation[] leadLocs = Memory.rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared);

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
            }

            if (lead >= MIN_TARGETABLE_LEAD
                    && dist < closestMiningLocDist
                    && (closestEnemyThreat == null
                    || closestEnemyThreat.getLocation().distanceSquaredTo(leadLocs[i]) > closestEnemyThreat.getType().actionRadiusSquared)) {
                closestMiningLoc = leadLocs[i];
                closestMiningLocDist = dist;
            }

            blockHasLead |= inMyBlock;
        }

        if (!blockHasMineableLead) {
            Communication.addBlockInfo(
                    blockHasLead ? Communication.BLOCK_TYPE_UNMINEABLE : Communication.BLOCK_TYPE_NO_LEAD,
                    myBlock);
        }

        if (closestEnemyThreat != null) {
            RobotInfo[] teammates = Memory.rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, Memory.rc.getTeam());

            int distToClosestFriendlySoldier = Integer.MAX_VALUE;
            MapLocation closestFriendlySoldierLoc = null;
            for (int i = 0; i < teammates.length; i++) {
                int dist = Utils.dist(myLoc, teammates[i].getLocation());
                if (teammates[i].getType() == RobotType.SOLDIER && dist < distToClosestFriendlySoldier) {
                    distToClosestFriendlySoldier = dist;
                    closestFriendlySoldierLoc = teammates[i].getLocation();
                }
            }

            if (closestFriendlySoldierLoc != null) {
                Pathfinder.moveTo(closestFriendlySoldierLoc);
            } else {
                Pathfinder.moveTo(Memory.archonLocation);
            }
        } else if (closestMiningLoc != null) {
            RobotInfo[] teammates = Memory.rc.senseNearbyRobots(8, Memory.rc.getTeam());

            int distToClosestFriendlyMiner = Integer.MAX_VALUE;
            MapLocation closestFriendlyMinerLoc = null;
            for (int i = 0; i < teammates.length; i++) {
                int dist = Utils.dist(myLoc, teammates[i].getLocation());
                if (teammates[i].getType() == RobotType.SOLDIER && dist < distToClosestFriendlyMiner) {
                    distToClosestFriendlyMiner = dist;
                    closestFriendlyMinerLoc = teammates[i].getLocation();
                }
            }

            MapLocation targetMineLoc = closestMiningLoc;

            int bestScore = Integer.MIN_VALUE;
            for (Direction dir : Direction.allDirections()) {
                MapLocation adjLoc = closestMiningLoc.add(dir);
                if (dir == Direction.CENTER
                        || (Memory.rc.canSenseLocation(adjLoc)
                        && !Memory.rc.isLocationOccupied(adjLoc))) {
                    int score =
                            50 * (closestFriendlyMinerLoc == null ? 0 : Utils.dist(adjLoc, closestFriendlyMinerLoc))
                            - Memory.rc.senseRubble(adjLoc)
                            + (adjLoc.equals(myLoc) ? 1 : 0);
                    if (score > bestScore) {
                        bestScore = score;
                        targetMineLoc = adjLoc;
                    }
                }
            }

            Pathfinder.moveTo(targetMineLoc);
        } else {
            // find target block
            int distToTargetBlock = Integer.MAX_VALUE;

            MapLocation targetBlock = null;

            // mining blocks
            for (int i = 0; i < Communication.NUM_TARGET_BLOCKS; i++) {
                MapLocation targetMiningBlock = Communication.getTargetMiningBlock(i);
                if (targetMiningBlock == null) continue;
                MapLocation center = Utils.getCenterOfBlock(targetMiningBlock);
                int dist = Utils.dist(center, myLoc) + Utils.dist(Memory.archonLocation, center) / 3;
                if (dist < distToTargetBlock) {
                    targetBlock = targetMiningBlock;
                    distToTargetBlock = dist;
                }
            }

            String targetMiningString = targetBlock + "";

            // explore blocks
            for (int i = 0; i < Communication.NUM_TARGET_BLOCKS; i++) {
                MapLocation targetExploreBlock = Communication.getTargetExploreBlock(i);
                if (targetExploreBlock == null) continue;
                int dist = Utils.dist(Utils.getCenterOfBlock(targetExploreBlock), myLoc);
                if (dist < distToTargetBlock) {
                    targetBlock = targetExploreBlock;
                    distToTargetBlock = dist;
                }
            }

            Memory.rc.setIndicatorString(targetMiningString + " : " + targetBlock);
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