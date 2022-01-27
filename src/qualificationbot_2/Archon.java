package qualificationbot_2;

import battlecode.common.*;

public class Archon {

    private static final int UNEXPLORED_BLOCK = 0;
    private static final int EMPTY_BLOCK = 2002;

    private static boolean[][] inExploreBlockList = new boolean[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static boolean[][] unmineable = new boolean[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static int[][] nextRoundMineable = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static int[][] enemyBlockInfoType = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];

    public static final int MIN_MINEABLE_LEAD = 21;

    private static int mineBlocksListSize = 0;
    private static int exploreBlocksListSize = 0;
    private static MapLocation[] mineBlocksList = new MapLocation[Utils.MAX_NUM_BLOCKS];
    private static MapLocation[] exploreBlocksList = new MapLocation[Utils.MAX_NUM_BLOCKS];

    private static int safeArchonBuilderCount = 0;

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
        int builderCount = Communication.getDroidCount(RobotType.BUILDER);

        int budget = Communication.getBudget();

        Communication.updateArchonIdx();
        Communication.updateDroidCount();

        // budget allocation
        if (archonIdx == 0) {
            budget = Math.min((1 << Communication.BUDGET_SIZE) - 1, Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()));
            Communication.setBudget(budget);
        }

        // initial exploration list around archon
        if (roundNum == 1) {
            addExploreBlocks(2);
        }

        // update explore index and expand to new blocks
        while (exploreIndex < exploreBlocksListSize) {
            MapLocation block = exploreBlocksList[exploreIndex];
            if (nextRoundMineable[block.x][block.y] == UNEXPLORED_BLOCK) break;
            exploreIndex++;

            Direction expandDir = myBlock.directionTo(block);

            addExploreBlock(block.add(expandDir));
            addExploreBlock(block.add(expandDir.rotateLeft()));
            addExploreBlock(block.add(expandDir.rotateRight()));
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
        int distToClosestMineableBlock = Integer.MAX_VALUE;

        MapLocation centerBlock = new MapLocation(mapBlockWidth / 2, mapBlockHeight / 2);

        Direction dirToClosestEnemyThreat = myBlock.directionTo(centerBlock);
        Direction dirToClosestMineableBlock = null;

        // mining targets
        int miningTargetIdx = 0;
        for (int i = 0; i < mineBlocksListSize && miningTargetIdx < numTargets; i++) {
            MapLocation block = mineBlocksList[i];
            if (nextRoundMineable[block.x][block.y] <= roundNum && isClosestArchonToBlock(block)) {
                Communication.setTargetMiningBlock(block, archonIdx * numTargets + miningTargetIdx);
                miningTargetIdx++;
                int dist = Utils.dist(block, myBlock);
                if (dist < distToClosestMineableBlock) {
                    distToClosestMineableBlock = dist;
                    dirToClosestMineableBlock = myBlock.directionTo(block);
                }
                Memory.rc.setIndicatorLine(Memory.rc.getLocation(), Utils.getCenterOfBlock(block), 0, 100, 0);

                unmineable[block.x][block.y] = false;
            }
        }

        // enemy targets
        int enemyTargetIdx = 0;
        Outer:
        for (int x = 0; x < mapBlockWidth; x++) {
            for (int y = 0; y < mapBlockHeight; y++) {
                if (enemyBlockInfoType[x][y] != Communication.BLOCK_TYPE_THREAT && enemyBlockInfoType[x][y] != Communication.BLOCK_TYPE_NONTHREAT) continue;
                MapLocation block = new MapLocation(x, y);
                if (!isClosestArchonToBlock(block)) continue;
                Communication.setTargetEnemyBlock(block, archonIdx * numTargets + enemyTargetIdx);
                int dist = Utils.dist(block, myBlock);
                if (dist < distToClosestEnemyThreat) {
                    distToClosestEnemyThreat = dist;
                    dirToClosestEnemyThreat = myBlock.directionTo(block);
                }
                enemyTargetIdx++;

                Memory.rc.setIndicatorLine(Memory.rc.getLocation(), Utils.getCenterOfBlock(block), 100, 0, 0);

                if (enemyTargetIdx >= numTargets) break Outer;
            }
        }

        // explore targets
        int exploreTargetIdx = 0;
        for (int i = exploreBlocksListSize - 1; i >= exploreIndex && exploreTargetIdx < numTargets; i--) {
            MapLocation block = exploreBlocksList[i];
            if (nextRoundMineable[block.x][block.y] == UNEXPLORED_BLOCK) {
                Communication.setTargetExploreBlock(block, archonIdx * numTargets + exploreTargetIdx);
                exploreTargetIdx++;
            }
        }

        /* ~~~ TROOP MANAGEMENT ~~~ */

        int mapBlockArea = mapBlockWidth * mapBlockHeight;
        int numUnexploredBlocks = mapBlockArea - exploreIndex;
        int numLeadBlocks = mineBlocksListSize;

        int minNumMiners = mapBlockArea / 24;
        int maxNumMiners = (numLeadBlocks + numUnexploredBlocks / 16) / 2;

        boolean canSeeEnemies = Memory.rc.senseNearbyRobots(RobotType.ARCHON.visionRadiusSquared, Memory.rc.getTeam().opponent()).length > 0;

        int blocksFromAction = Math.max(4, Math.min(
                distToClosestEnemyThreat,
                getDistToPotentialEnemyArchon(archonIdx)));

        // 1. archons closer to center should spawn more soldiers
        // 2. need to spawn some soldiers early before enemy threats are seeen
        // 3. spawn some amount of soldiers based on distance to enemy threat, distance to center

        int safestArchonIdx = getSafestArchonIdx();

        if (builderCount == 0 || Communication.getLaboratoryCount() > 0) {
            if (archonIdx == safestArchonIdx && Memory.rc.getTeamGoldAmount(Memory.rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
                if (Utils.tryBuild(RobotType.SAGE, dirToClosestEnemyThreat)) {
                    Communication.incrementDroidCount(RobotType.SAGE);
                }
            } else if (blocksFromAction >= 6 && archonIdx == safestArchonIdx && safeArchonBuilderCount < 1 && minerCount >= minNumMiners) {
                safeArchonBuilderCount += Utils.tryBuild(RobotType.BUILDER, myBlock.directionTo(centerBlock).opposite()) ? 1 : 0;
            } else if (canSeeEnemies || minerCount >= maxNumMiners || (minerCount >= minNumMiners && roundNum % Math.max(1, distToClosestMineableBlock) != 0 && roundNum % (blocksFromAction / 4) == 0)) {
                if (budget / numArchons >= RobotType.SOLDIER.buildCostLead) {
                    if (Utils.tryBuild(RobotType.SOLDIER, dirToClosestEnemyThreat)) {
                        Communication.incrementDroidCount(RobotType.SOLDIER);
                    }
                }
            } else {
                if (budget / numArchons >= RobotType.MINER.buildCostLead) {
                    if (Utils.tryBuild(RobotType.MINER, dirToClosestMineableBlock)) {
                        Communication.incrementDroidCount(RobotType.MINER);
                    }
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

    public static int getDistToPotentialEnemyArchon(int archonIdx) throws GameActionException {
        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;
        MapLocation archonBlock = Communication.getArchonBlock(archonIdx);
        int xDistToEdge = Math.min(archonBlock.x, mapBlockWidth - archonBlock.x - 1);
        int yDistToEdge = Math.min(archonBlock.y, mapBlockHeight - archonBlock.y - 1);
        return Math.min(mapBlockWidth - 2 * xDistToEdge, mapBlockHeight - 2 * yDistToEdge);
    }

    public static int getSafestArchonIdx() throws GameActionException {
        int safestArchonIdx = 0;
        int furthestDistFromPotentialEnemyArchon = Integer.MIN_VALUE;
        for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
            int distFromPotentialEnemyArchon = getDistToPotentialEnemyArchon(i);
            if (distFromPotentialEnemyArchon > furthestDistFromPotentialEnemyArchon) {
                furthestDistFromPotentialEnemyArchon = distFromPotentialEnemyArchon;
                safestArchonIdx = i;
            }
        }

        return safestArchonIdx;
    }

    private static int[][] isClosestArchonToBlock = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];

    private static boolean isClosestArchonToBlock(MapLocation block) throws GameActionException {
        if (isClosestArchonToBlock[block.x][block.y] == 0) {
            MapLocation myBlock = Utils.getMyBlock();
            for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
                MapLocation archonBlock = Communication.getArchonBlock(i);
                if (archonBlock != null && archonBlock.distanceSquaredTo(block) < myBlock.distanceSquaredTo(block)) {
                    isClosestArchonToBlock[block.x][block.y] = 1;
                    return false;
                }
            }

            isClosestArchonToBlock[block.x][block.y] = 2;
        }

        return isClosestArchonToBlock[block.x][block.y] == 2;
    }

    private static void updateMineBlocks() throws GameActionException {
        int roundNum = Memory.rc.getRoundNum();

        MapLocation[] emptyLeadBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_NO_LEAD);
        for (int i = 0; i < emptyLeadBlocks.length; i++) {
            nextRoundMineable[emptyLeadBlocks[i].x][emptyLeadBlocks[i].y] = EMPTY_BLOCK;
        }

        MapLocation[] mineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_MINEABLE);
        for (int i = 0; i < mineableBlocks.length; i++) {
            addMineBlock(mineableBlocks[i]);
            nextRoundMineable[mineableBlocks[i].x][mineableBlocks[i].y] = roundNum;
            unmineable[mineableBlocks[i].x][mineableBlocks[i].y] = false;
        }

        MapLocation[] unmineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_UNMINEABLE);
        for (int i = 0; i < unmineableBlocks.length; i++) {
            addMineBlock(unmineableBlocks[i]);

            // only reset lead regen timer if it was mineable or unexplored before
            if (!unmineable[unmineableBlocks[i].x][unmineableBlocks[i].y] || nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y] <= roundNum) {
                nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y] = roundNum + 20 * (MIN_MINEABLE_LEAD - 1) / 5;
                unmineable[unmineableBlocks[i].x][unmineableBlocks[i].y] = true;
            }
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

    private static void addExploreBlock(MapLocation block) {
        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;

        if (block.x >= 0 && block.y >= 0 && block.x < mapBlockWidth && block.y < mapBlockHeight
                && !inExploreBlockList[block.x][block.y]
                && nextRoundMineable[block.x][block.y] == UNEXPLORED_BLOCK) {
            exploreBlocksList[exploreBlocksListSize] = block;
            inExploreBlockList[block.x][block.y] = true;
            exploreBlocksListSize++;
        }
    }

    private static void addMineBlock(MapLocation block) {
        if (nextRoundMineable[block.x][block.y] == UNEXPLORED_BLOCK) {
            mineBlocksList[mineBlocksListSize++] = block;
        }
    }

    private static void addExploreBlocks(int dist) throws GameActionException {
        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;

        MapLocation myBlock = Utils.getMyBlock();

        int startIdx = 0;

        // add all blocks of same dist
        int numNewLocations = 0;

        // add all new blocks
        int sideLength = dist * 2;
        for (int i = 0; i < sideLength * 4; i++) {
            int side = i / sideLength;
            int idx = i % sideLength;

            int dx, dy;
            switch (side) {
                case 0: // top
                    dx = -dist + idx;
                    dy = dist;
                    break;
                case 1: // right
                    dx = dist;
                    dy = dist - idx;
                    break;
                case 2: // bottom
                    dx = dist - idx;
                    dy = -dist;
                    break;
                case 3: // left
                    dx = -dist;
                    dy = -dist + idx;
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
    }

}