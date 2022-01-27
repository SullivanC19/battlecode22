package qualificationbot_1;

import battlecode.common.*;

public class Archon {

    private static final int UNEXPLORED_BLOCK = 0;
    private static final int EMPTY_BLOCK = 2002;

    private static int[][] nextRoundMineable = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static int[][] enemyBlockInfoType = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];

    public static final int MIN_MINEABLE_LEAD = 11;

    private static int mineBlocksListSize = 0;
    private static int exploreBlocksListSize = 0;
    private static MapLocation[] mineBlocksList = new MapLocation[Utils.MAX_NUM_BLOCKS];
    private static MapLocation[] exploreBlocksList = new MapLocation[Utils.MAX_NUM_BLOCKS];

    private static int exploreDist = 2;
    private static int exploreIndex = 0;

    public static void run() throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();

        int mapWidth = Memory.rc.getMapWidth();
        int mapHeight = Memory.rc.getMapHeight();
        int mapBlockWidth = mapWidth / Utils.BLOCK_SIZE;
        int mapBlockHeight = mapHeight / Utils.BLOCK_SIZE;

        int archonIdx = Communication.getArchonIdx();
        int roundNum = Memory.rc.getRoundNum();
        int numArchons = Memory.rc.getArchonCount();

        int minerCount = Communication.getDroidCount(RobotType.MINER);
        int soldierCount = Communication.getDroidCount(RobotType.SOLDIER);

        int budget = Communication.getBudget();

        Communication.updateArchonIdx();
        Communication.updateDroidCount();

        // budget allocation
        if (archonIdx == 0) {
            budget = Math.min((1 << Communication.BUDGET_SIZE) - 1, Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()));
            Communication.setBudget(budget);
        }

        // exploration list
        if (exploreIndex == exploreBlocksListSize) {
            addExploreBlocks();
        }

        // update explore index
        while (exploreIndex < exploreBlocksListSize) {
            MapLocation block = exploreBlocksList[exploreIndex];
            if (nextRoundMineable[block.x][block.y] == UNEXPLORED_BLOCK) break;
            exploreIndex++;
        }

        Memory.rc.setIndicatorString("archonIdx: " + archonIdx + " (" + minerCount + ", " + soldierCount + ")");

        // send nearby visible lead
        MapLocation[] nearbyMineableLead = Memory.rc.senseNearbyLocationsWithLead(32, MIN_MINEABLE_LEAD);
        for (int i = 0; i < nearbyMineableLead.length; i++) {
            Communication.addBlockInfo(Communication.BLOCK_TYPE_MINEABLE, Utils.getBlock(nearbyMineableLead[i]));
        }

        // pull block info from comms
        updateMineBlocks();
        updateEnemyBlocks();

        /* ~~~ RECTANGLE CONSTRUCTION ~~~ */

//        int minX, minY, maxX, maxY;
//        minX = maxX = myBlock.x;
//        minY = maxY = myBlock.y;
//
//        // create max rectangle based on explored
//        while (minX > 0 && Communication.getExplored(new MapLocation(minX - 1, myBlock.y))) minX--;
//        while (minY > 0 && Communication.getExplored(new MapLocation(myBlock.x, minY - 1))) minY--;
//        while (maxX < mapBlockWidth - 1 && Communication.getExplored(new MapLocation(maxX + 1, myBlock.y))) maxX++;
//        while (maxY < mapBlockHeight - 1 && Communication.getExplored(new MapLocation(myBlock.x, maxY + 1))) maxY++;
//
//        // restrain rectangle greedily based on enemy threat blocks
//        for (int x = minX; x <= maxX; x++) {
//            for (int y = minY; y <= maxY; y++) {
//                if (enemyBlockInfoType[x][y] != Communication.BLOCK_TYPE_THREAT) continue;
//
//                int width = maxX - minX + 1;
//                int height = maxY - minY + 1;
//
//                int minXDiff = x - minX;
//                int minYDiff = y - minY;
//                int maxXDiff = maxX - x;
//                int maxYDiff = maxY - y;
//
//                int xReduce = Math.min(minXDiff, maxXDiff) * height;
//                int yReduce = Math.min(minYDiff, maxYDiff) * width;
//
//                if (xReduce < yReduce) {
//                    if (minXDiff < maxXDiff) {
//                        minX = x + 1;
//                    } else {
//                        maxX = x - 1;
//                    }
//                } else {
//                    if (minYDiff < maxYDiff) {
//                        minY = y + 1;
//                    } else {
//                        maxY = y - 1;
//                    }
//                }
//            }
//        }
//
//        // just in case the rectangle is empty
//        minX = Math.min(myBlock.x, minX);
//        minY = Math.min(myBlock.y, minY);
//        maxX = Math.max(myBlock.x, maxX);
//        maxY = Math.max(myBlock.y, maxY);
//
//        // send rectangle coords
//        Communication.setArchonRectangle(archonIdx,
//                new MapLocation[] {
//                        new MapLocation(minX, minY),
//                        new MapLocation(maxX, maxY)
//                });

        /* ~~~ TARGETING ~~~ */

        int numTargets = Communication.NUM_TARGET_BLOCKS / numArchons;

        // reset target blocks
        for (int i = archonIdx * numTargets; i < (archonIdx + 1) * numTargets; i++) {
            Communication.setTargetMiningBlock(null, i);
            Communication.setTargetEnemyBlock(null, i);
            Communication.setTargetExploreBlock(null, i);
        }

        // info about targets for troop management
        int distToClosestEnemyThreat = Integer.MAX_VALUE;
        boolean hasMiningTarget = false;


        // mining targets
        int miningTargetIdx = 0;
        for (int i = 0; i < mineBlocksListSize && miningTargetIdx < numTargets; i++) {
            MapLocation block = mineBlocksList[i];
            if (nextRoundMineable[block.x][block.y] <= roundNum && isClosestArchonToBlock(block)) {
                Communication.setTargetMiningBlock(block, archonIdx * numTargets + miningTargetIdx);
                miningTargetIdx++;
                hasMiningTarget = true;
                Memory.rc.setIndicatorLine(Memory.rc.getLocation(), Utils.getCenterOfBlock(block), 0, 100, 0);
            }
        }


        // enemy targets
        int enemyTargetIdx = 0;
        Outer:
        for (int x = 0; x < mapBlockWidth; x++) {
            for (int y = 0; y < mapBlockHeight; y++) {
                if (enemyBlockInfoType[x][y] != Communication.BLOCK_TYPE_THREAT) continue;
                MapLocation block = new MapLocation(x, y);
                if (!isClosestArchonToBlock(block)) continue;
                Communication.setTargetEnemyBlock(block, archonIdx * numTargets + enemyTargetIdx);
                distToClosestEnemyThreat =
                        Math.min(distToClosestEnemyThreat, Utils.dist(block, myBlock));
                enemyTargetIdx++;
                if (enemyTargetIdx >= numTargets) break Outer;
            }
        }

        // explore targets
        int exploreTargetIdx = 0;
        for (int i = exploreIndex; i < exploreBlocksListSize && exploreTargetIdx < numTargets; i++) {
            MapLocation block = exploreBlocksList[i];
            if (nextRoundMineable[block.x][block.y] == UNEXPLORED_BLOCK) {
                Communication.setTargetExploreBlock(block, archonIdx * numTargets + exploreTargetIdx);
                exploreTargetIdx++;
            }
        }

        /* ~~~ TROOP MANAGEMENT ~~~ */

        boolean canSeeEnemies = Memory.rc.senseNearbyRobots(RobotType.ARCHON.visionRadiusSquared, Memory.rc.getTeam().opponent()).length > 0;

        int blocksFromCenter = Utils.dist(myBlock, new MapLocation(mapBlockWidth / 2, mapBlockHeight / 2));

        int maxNumMiners = mapBlockWidth * mapBlockHeight / 4;

        double roundProgress = (Math.min(500, Math.max(50, roundNum)) - 50) / (500. - 50.);
        double dangerLevel = 1. / Math.min(Math.max(1, distToClosestEnemyThreat - 1), blocksFromCenter * 2);

        double expFracSoldiers = (.75 - .17) * Math.max(roundProgress, dangerLevel) + .17;

        double expNumSoldiers = (soldierCount + minerCount) * expFracSoldiers;

        if ((minerCount >= 6 && (!hasMiningTarget || soldierCount < expNumSoldiers)) || canSeeEnemies || minerCount > maxNumMiners) {
            if (budget / numArchons >= RobotType.SOLDIER.buildCostLead) {
                if (Utils.tryBuild(RobotType.SOLDIER)) { // TODO spawn towards enemy if there is one
                    Communication.incrementDroidCount(RobotType.SOLDIER);
                }
            }
        } else {
            if (budget / numArchons >= RobotType.MINER.buildCostLead) {
                if (Utils.tryBuild(RobotType.MINER)) { // TODO spawn towards lead block if there is one
                    Communication.incrementDroidCount(RobotType.MINER);
                }
            }
        }

        /* ~~~ HEALING ~~~ */

        RobotInfo[] nearbyRobots = Memory.rc.senseNearbyRobots(RobotType.ARCHON.actionRadiusSquared, Memory.rc.getTeam());
        RobotInfo highestHealthRobot = null;
        for (int i = 0; i < nearbyRobots.length; i++) {
            int level = nearbyRobots[i].getLevel();
            int health = nearbyRobots[i].getHealth();
            if (health < nearbyRobots[i].getType().getMaxHealth(level)
                    && (highestHealthRobot == null || health > highestHealthRobot.getHealth())) {
                highestHealthRobot = nearbyRobots[i];
            }
        }

        if (highestHealthRobot != null && Memory.rc.canRepair(highestHealthRobot.getLocation())) {
            Memory.rc.repair(highestHealthRobot.getLocation());
        }

        /* ~~~ COMMUNICATION ~~~ */

        if (archonIdx + 1 >= Memory.rc.getArchonCount()) {
            Communication.resetBlockInfo();
        }
    }

    private static boolean isClosestArchonToBlock(MapLocation block) throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();
        for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
            MapLocation archonBlock = Communication.getArchonBlock(i);
            if (archonBlock != null && archonBlock.distanceSquaredTo(block) < myBlock.distanceSquaredTo(block)) {
                return false;
            }
        }
        return true;
    }

    private static void updateMineBlocks() throws GameActionException {
        int roundNum = Memory.rc.getRoundNum();

        MapLocation[] emptyLeadBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_NO_LEAD);
        for (int i = 0; i < emptyLeadBlocks.length; i++) {
            nextRoundMineable[emptyLeadBlocks[i].x][emptyLeadBlocks[i].y] = EMPTY_BLOCK;
        }

        MapLocation[] mineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_MINEABLE);
        for (int i = 0; i < mineableBlocks.length; i++) {
            if (nextRoundMineable[mineableBlocks[i].x][mineableBlocks[i].y] == UNEXPLORED_BLOCK) {
                addMineBlock(mineableBlocks[i]);
            }
            nextRoundMineable[mineableBlocks[i].x][mineableBlocks[i].y] = roundNum;
        }

        MapLocation[] unmineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_UNMINEABLE);
        for (int i = 0; i < unmineableBlocks.length; i++) {
            int curVal = nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y];
            nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y] = roundNum + 20 * (MIN_MINEABLE_LEAD - 1) / 5;
            if (curVal == UNEXPLORED_BLOCK) addMineBlock(unmineableBlocks[i]);
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

    private static void addMineBlock(MapLocation block) {
        mineBlocksList[mineBlocksListSize++] = block;
    }

    private static void addExploreBlocks() throws GameActionException {
        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;

        MapLocation myBlock = Utils.getMyBlock();

        int startIdx = 0;

        // add all blocks of same dist
        int numNewLocations = 0;

        // add all new blocks
        int sideLength = exploreDist * 2;
        for (int i = 0; i < sideLength * 4; i++) {
            int side = i / sideLength;
            int idx = i % sideLength;

            int dx, dy;
            switch (side) {
                case 0: // top
                    dx = -exploreDist + idx;
                    dy = exploreDist;
                    break;
                case 1: // right
                    dx = exploreDist;
                    dy = exploreDist - idx;
                    break;
                case 2: // bottom
                    dx = exploreDist - idx;
                    dy = -exploreDist;
                    break;
                case 3: // left
                    dx = -exploreDist;
                    dy = -exploreDist + idx;
                    break;
                default:
                    dx = 0; dy = 0;
            }

            MapLocation potentialBlock = myBlock.translate(dx, dy);

            if (potentialBlock.x >= 0
                    && potentialBlock.y >= 0
                    && potentialBlock.x < mapBlockWidth
                    && potentialBlock.y < mapBlockHeight) {
                exploreBlocksList[exploreBlocksListSize + numNewLocations] = potentialBlock;
                numNewLocations++;
            }
        }

        // fisher-yates shuffle with blocks of same dist
        for (int i = numNewLocations - 1; i > 0; i--) {
            int swapIdx = startIdx + Utils.rng.nextInt(i + 1);
            MapLocation tmp = exploreBlocksList[swapIdx];
            exploreBlocksList[swapIdx] = exploreBlocksList[startIdx + i];
            exploreBlocksList[startIdx + i] = tmp;
        }

        exploreBlocksListSize += numNewLocations;
        exploreDist++;
    }

}