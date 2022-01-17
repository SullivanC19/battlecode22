package sprintbot;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;




public class Archon {
    private static int[][] nextRoundMineable = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static int[][] enemyBlockInfoType = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    public static void run() throws GameActionException {
        Memory.update();

        int numMineableBlocksClosestTo = 0;
        int totalMineableBlocks = 0;

        for (int i = 0; i < Memory.leadList.length; i++) {
            if (Memory.rc.senseLead(Memory.leadList[i]) <= 7) continue;
            Communication.addBlockInfo(Communication.BLOCK_TYPE_MINEABLE, Utils.getBlock(Memory.leadList[i]));
        }

        int minerCount = Communication.getDroidCount(RobotType.MINER);

        int builderCount = Communication.getDroidCount(RobotType.BUILDER);
        int soldierCount = Communication.getDroidCount(RobotType.SOLDIER);
        int totalDroids = minerCount + builderCount + soldierCount;

        int archonIdx = Communication.getArchonIdx();
        int numArchons = Memory.rc.getArchonCount();


        /* ~~~ RESOURCE GATHERING ~~~ */

        updateMineableBlocks();
        updateUnmineableBlocks();
        updateNoLeadBlocks();

        int totalLead = Memory.rc.getTeamLeadAmount(Memory.rc.getTeam());

        int[] bestDist = new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE};
        MapLocation[] targetMiningBlocks = new MapLocation[] {null, null};
        for (int x = 0; x < nextRoundMineable.length; x++) {
            for (int y = 0; y < nextRoundMineable[0].length; y++) {
                if (nextRoundMineable[x][y] > Memory.rc.getRoundNum() || nextRoundMineable[x][y] == 0) continue;

                totalMineableBlocks++;

                MapLocation block = new MapLocation(x, y);
                int dist = Memory.myBlock.distanceSquaredTo(block);

                boolean isClosest = true;
                for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
                    if (dist > block.distanceSquaredTo(Communication.getArchonBlock(i))) {
                        isClosest = false;
                        break;
                    }
                }

                if (!isClosest) continue;

                if (dist < bestDist[0]) {
                    bestDist[0] = dist;
                    targetMiningBlocks[0] = block;
                } else if (dist < bestDist[1]) {
                    bestDist[1] = dist;
                    targetMiningBlocks[1] = block;
                }

                numMineableBlocksClosestTo++;
            }
        }

        Memory.rc.setIndicatorString(archonIdx + "/" + Memory.rc.getArchonCount() + ": " + numMineableBlocksClosestTo + " / " + totalMineableBlocks + " (" + minerCount + ")");
        if (targetMiningBlocks[0] != null) Memory.rc.setIndicatorDot(Utils.getCenterOfBlock(targetMiningBlocks[0]), 0, 255, 0);
        if (targetMiningBlocks[1] != null) Memory.rc.setIndicatorDot(Utils.getCenterOfBlock(targetMiningBlocks[1]), 0, 255, 0);

        Communication.setTargetBlocks(targetMiningBlocks, true);


        /* ~~~ TROOP MANAGEMENT ~~~ */

        updateEnemyBlocks();

        int closestEnemyBlockDist = Integer.MAX_VALUE;
        MapLocation closestEnemyBlock = null;
        for (int x = 0; x < enemyBlockInfoType.length; x++) {
            for (int y = 0; y < enemyBlockInfoType[0].length; y++) {
                if (enemyBlockInfoType[x][y] == 0 || enemyBlockInfoType[x][y] == Communication.BLOCK_TYPE_NO_ENEMY) continue;
                MapLocation block = new MapLocation(x, y);
                int dist = block.distanceSquaredTo(Memory.myBlock);
                if (dist < closestEnemyBlockDist) {
                    closestEnemyBlock = block;
                    closestEnemyBlockDist = dist;
                }
            }
        }

        int roundNum = Memory.rc.getRoundNum();
        boolean canSeeEnemies = Memory.rc.senseNearbyRobots(RobotType.ARCHON.visionRadiusSquared, Memory.rc.getTeam().opponent()).length > 0;

        // always build miners early especially if the map is large
        if (minerCount < totalMineableBlocks || minerCount < numArchons * 5) {
            if (!canSeeEnemies && totalLead * (numMineableBlocksClosestTo + 1) / (totalMineableBlocks + numArchons) >= RobotType.SOLDIER.buildCostLead) {
                Utils.tryBuild(RobotType.MINER);
            }
        } else if ((roundNum < 10 || closestEnemyBlock != null)
                && totalLead / (numArchons - archonIdx) >= RobotType.SOLDIER.buildCostLead
                && (canSeeEnemies || soldierCount < minerCount || roundNum % 100 < 20)) {
            Utils.tryBuild(RobotType.SOLDIER);
        }

        Communication.setTargetBlocks(new MapLocation[] {closestEnemyBlock, null}, false);

        /* ~~~ COMMUNICATION ~~~ */

        if (Communication.getArchonIdx() + 1 >= Memory.rc.getArchonCount()) {
            Communication.resetBlockInfo();
        }

        Communication.updateArchonIdx();
        Communication.updateDroidCount();
    }

    private static void updateMineableBlocks() throws GameActionException {
        MapLocation[] mineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_MINEABLE);
        for (int i = 0; i < mineableBlocks.length; i++) {
            nextRoundMineable[mineableBlocks[i].x][mineableBlocks[i].y] = Memory.rc.getRoundNum();
        }
    }

    private static void updateUnmineableBlocks() throws GameActionException {
        MapLocation[] unmineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_UNMINEABLE);
        for (int i = 0; i < unmineableBlocks.length; i++) {
            if (nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y] <= Memory.rc.getRoundNum()) {
                nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y] = Memory.rc.getRoundNum() + 120;
            }
        }
    }

    private static void updateNoLeadBlocks() throws GameActionException {
        MapLocation[] noLeadBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_NO_LEAD);
        for (int i = 0; i < noLeadBlocks.length; i++) {
            nextRoundMineable[noLeadBlocks[i].x][noLeadBlocks[i].y] = 0;
        }
    }

    private static void updateEnemyBlocks() throws GameActionException {
        for (int type : new int[] { Communication.BLOCK_TYPE_NO_ENEMY, Communication.BLOCK_TYPE_NONTHREAT, Communication.BLOCK_TYPE_THREAT}) {
            MapLocation[] enemyBlocks = Communication.getBlocks(type);
            for (int i = 0; i < enemyBlocks.length; i++) {
                enemyBlockInfoType[enemyBlocks[i].x][enemyBlocks[i].y] = type;
            }
        }
    }
}