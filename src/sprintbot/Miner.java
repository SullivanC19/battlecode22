package sprintbot;

import battlecode.common.*;

public class Miner {

    public static void run() throws GameActionException {
        Memory.update();

        Communication.getDroidCount(RobotType.MINER);

        int bestDist = Integer.MAX_VALUE;
        MapLocation closestLeadLocation = null;
        for (int i = 0; i < Memory.leadList.length; i++) {
            int dist = Memory.rc.getLocation().distanceSquaredTo(Memory.leadList[i]);
            int lead = Memory.rc.senseLead(Memory.leadList[i]);
            if (!(lead >= 10 || (lead > 1 && dist < 10))) continue;
            if (dist < bestDist) {
                bestDist = dist;
                closestLeadLocation = Memory.leadList[i];
                Pathfinder.exploreLoc = null;
            }
        }

        int type = Memory.numLeadInBlock == 0 ?
                Communication.BLOCK_TYPE_NO_LEAD :
                (Memory.blockIsMinable ? Communication.BLOCK_TYPE_MINEABLE : Communication.BLOCK_TYPE_UNMINEABLE);

        // TODO: this doesnt actually get the enemy robots in block
        if (Memory.rc.senseNearbyRobots(20, Memory.rc.getTeam().opponent()).length != 0) {
            type = Communication.BLOCK_TYPE_NONTHREAT;
        }

        Communication.addBlockInfo(type, Memory.myBlock);

        if (Pathfinder.exploreLoc != null) {
            Pathfinder.explore();
            Communication.updateDroidCount();
            return;
        }

        if (closestLeadLocation != null) {
            Pathfinder.moveToward(closestLeadLocation);
        }
//        else if (closestLeadLocation != null) {
//            int bestScore = Integer.MIN_VALUE;
//            Direction bestShuffle = Direction.CENTER;
//            for (Direction shuffleDir : Direction.allDirections()) {
//                MapLocation shuffleLoc = closestLeadLocation.add(shuffleDir);
//                if (!Memory.rc.canSenseLocation(shuffleLoc)) continue;
//
//                int lead = Memory.rc.senseLead(shuffleLoc);
//                int minersAdj = -1;
//                int rubble = Memory.rc.senseRubble(shuffleLoc);
//
//                for (Direction dir : Direction.allDirections()) {
//                    MapLocation adjLoc = shuffleLoc.add(dir);
//                    if (Memory.rc.canSenseLocation(adjLoc)) {
//                        lead += Math.max(Memory.rc.senseLead(adjLoc) - 1, 0);
//                        RobotInfo ri = Memory.rc.senseRobotAtLocation(adjLoc);
//                        minersAdj += (ri != null && ri.getType() == RobotType.MINER ? 1 : 0);
//                    }
//                }
//
//                int score = 2 * Math.max(5, lead) - minersAdj + rubble;
//                if (score > bestScore) {
//                    bestScore = score;
//                    bestShuffle = shuffleDir;
//                }
//            }
//
//            if (Memory.rc.canMove(bestShuffle)) {
//                Memory.rc.move(bestShuffle);
//            }
//        }
        else {
            int closestLeadBlockDist = Integer.MAX_VALUE;
            MapLocation closestLeadBlock = null;
            MapLocation[] miningBlocks = Communication.getAllTargetBlocks(true);
            for (MapLocation miningBlock: miningBlocks) {
                if (miningBlock == null) continue;

                int dist = miningBlock.distanceSquaredTo(Memory.myBlock);
                if (dist < closestLeadBlockDist) {
                    closestLeadBlockDist = dist;
                    closestLeadBlock = miningBlock;
                }
            }

            if (closestLeadBlock != null && Utils.rng.nextInt(200) >= closestLeadBlockDist) {
                Pathfinder.moveToward(Utils.getCenterOfBlock(closestLeadBlock));
            } else {
                Pathfinder.explore();
            }
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