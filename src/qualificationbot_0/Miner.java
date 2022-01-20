package qualificationbot_0;

import battlecode.common.*;

import java.nio.file.Path;

public class Miner {

    private static MapLocation targetBlock = null;

    private static MapLocation[] lastTargetMineBlock = new MapLocation[8];
    private static int[] numTurnsSameTarget = new int[8];

    public static void run() throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();

        RobotInfo[] enemyRobots = Memory.rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, Memory.rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            boolean enemyThreat = false;
            for (int i = 0; i < enemyRobots.length && !enemyThreat; i++) {
                RobotType type = enemyRobots[i].getType();
                enemyThreat = type == RobotType.WATCHTOWER || type == RobotType.SOLDIER || type == RobotType.SAGE;
            }

            Communication.addBlockInfo(
                    enemyThreat ? Communication.BLOCK_TYPE_THREAT : Communication.BLOCK_TYPE_NONTHREAT,
                    myBlock);
        }

        int archonIdx = Communication.getArchonIdx();
        int numMineTargets = Memory.rc.getArchonCount() * 2;

        MapLocation[] miningTargetBlocks = Communication.getAllTargetBlocks(true);
        for (int i = archonIdx * 2; (i + 1) % numMineTargets != archonIdx * 2; i = (i + 1) % numMineTargets) {
            if (miningTargetBlocks[i] == null) continue;
            if (lastTargetMineBlock[i] == null || !miningTargetBlocks[i].equals(lastTargetMineBlock[i])) {
                numTurnsSameTarget[i] = 0;
            }

            lastTargetMineBlock[i] = miningTargetBlocks[i];
            numTurnsSameTarget[i]++;

            if (targetBlock == null
                    && !Communication.getTargetMiningBlockPicked(i / 2, i % 2)) {
                targetBlock = miningTargetBlocks[i];
                Communication.setTargetMiningBlockPicked(i / 2, i % 2);
            }
        }

        MapLocation[] leadBlocks = Memory.rc.senseNearbyLocationsWithLead();

        boolean blockHasLead = false;
        boolean blockHasMineableLead = false;
        int bestDist = Integer.MAX_VALUE;
        MapLocation closestMiningLoc = null;
        for (int i = 0; i < leadBlocks.length; i++) {
            int lead = Memory.rc.senseLead(leadBlocks[i]);
            MapLocation block = Utils.getBlock(leadBlocks[i]);

            if (lead >= Archon.MIN_MINEABLE_LEAD) {
                Communication.addBlockInfo(Communication.BLOCK_TYPE_MINEABLE, block);
                blockHasMineableLead |= block.equals(myBlock);
            }

            blockHasLead |= block.equals(myBlock);

            int dist = Memory.rc.getLocation().distanceSquaredTo(leadBlocks[i]);
            if (lead >= Archon.MIN_MINEABLE_LEAD && dist < bestDist) {
                closestMiningLoc = leadBlocks[i];
                bestDist = dist;
            }
        }

        if (!blockHasMineableLead) {
            Communication.addBlockInfo(
                    blockHasLead ? Communication.BLOCK_TYPE_UNMINEABLE : Communication.BLOCK_TYPE_NO_LEAD,
                    myBlock);
        }

        if (myBlock.equals(targetBlock)) targetBlock = null;

        if (closestMiningLoc != null) {
            Pathfinder.moveTo(closestMiningLoc);
        } else if (targetBlock != null) {
            Pathfinder.moveTo(Utils.getCenterOfBlock(targetBlock));
        } else {
            Pathfinder.explore();
        }


        // mine
        for (Direction dir : Direction.allDirections()) {
            MapLocation loc = Memory.rc.getLocation().add(dir);
            while (Memory.rc.canMineLead(loc) && Memory.rc.senseLead(loc) > 1) {
                Memory.rc.mineLead(loc);
            }
        }

        Communication.updateDroidCount();
    }

}