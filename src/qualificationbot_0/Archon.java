package qualificationbot_0;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

import java.util.Map;


public class Archon {

    private static final int UNEXPLORED_BLOCK = 0;
    private static final int CURRENTLY_EXPLORING = 2001;
    private static final int EMPTY_BLOCK = 2002;

    private static int[][] nextRoundMineable = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static int[][] enemyBlockInfoType = new int[Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];

    private static MapLocation[] targetMiningBlocks = new MapLocation[2];

    public static final int MIN_MINEABLE_LEAD = 11;

    private static MapLocation[] mineBlocksDeque = new MapLocation[Utils.MAX_NUM_BLOCKS];
    private static int mineBlocksFront = 0;
    private static int mineBlocksBack = 0;

    private static MapLocation[] explorationBlocks = new MapLocation[Utils.MAX_NUM_BLOCKS];
    private static int explorationCurIndex = 0;

    public static void run() throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();

        int archonIdx = Communication.getArchonIdx();
        int numArchons = Memory.rc.getArchonCount();

        int minerCount = Communication.getDroidCount(RobotType.MINER);
        int soldierCount = Communication.getDroidCount(RobotType.SOLDIER);

        int budget = Communication.getBudget();
        if (archonIdx == 0) {
            budget = Math.min((1 << Communication.BUDGET_SIZE) - 1, Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()));
            Communication.setBudget(budget);
        }

        Memory.rc.setIndicatorString(archonIdx + ": " + budget);

        if (explorationBlocks[0] == null) {
            constructExplorationBlocks();
        }

        // update nearby visible lead
        MapLocation[] nearbyMineableLead = Memory.rc.senseNearbyLocationsWithLead(32, MIN_MINEABLE_LEAD);
        for (int i = 0; i < nearbyMineableLead.length; i++) {
            Communication.addBlockInfo(Communication.BLOCK_TYPE_MINEABLE, Utils.getBlock(nearbyMineableLead[i]));
        }

        // pull block info from comms
        updateMineBlocks();
        updateEnemyBlocks();

        /* ~~~ RESOURCE GATHERING ~~~ */

        int totalLead = Memory.rc.getTeamLeadAmount(Memory.rc.getTeam());

        for (int i = 0; i < 2; i++) {
            if (targetMiningBlocks[i] != null
                    && !Communication.getTargetMiningBlockPicked(archonIdx, i)
                    && nextRoundMineable[targetMiningBlocks[i].x][targetMiningBlocks[i].y] == CURRENTLY_EXPLORING) continue;

            MapLocation mineBlock = peekFrontMineQueue();
            while (mineBlock != null
                    && nextRoundMineable[mineBlock.x][mineBlock.y] > Memory.rc.getRoundNum()
                    && !isClosestArchonToBlock(mineBlock)) {
                removeFromFrontMineQueue();
                mineBlock = peekFrontMineQueue();
            }

            MapLocation exploreBlock = explorationCurIndex >= explorationBlocks.length ? null : explorationBlocks[explorationCurIndex];
            while (exploreBlock != null
                    && nextRoundMineable[exploreBlock.x][exploreBlock.y] != UNEXPLORED_BLOCK) {
                exploreBlock = explorationBlocks[++explorationCurIndex];
            }

            if (mineBlock == null && exploreBlock != null) {
                targetMiningBlocks[i] = exploreBlock;
                if (exploreBlock != null) explorationCurIndex++;
            } else {
                targetMiningBlocks[i] = mineBlock;
                if (mineBlock != null) removeFromFrontMineQueue();
            }

            if (targetMiningBlocks[i] != null) {
                nextRoundMineable[targetMiningBlocks[i].x][targetMiningBlocks[i].y] = CURRENTLY_EXPLORING;
            }
        }

        for (int i = 0; i < 2; i++) {
            if (targetMiningBlocks[i] != null) {
                Memory.rc.setIndicatorLine(Memory.archonLocation, Utils.getCenterOfBlock(targetMiningBlocks[i]), 0, 255, 0);
            }
        }

//        if (targetMiningBlocks[0] != null) Memory.rc.setIndicatorDot(Utils.getCenterOfBlock(targetMiningBlocks[0]), 0, 255, 0);
//        if (targetMiningBlocks[1] != null) Memory.rc.setIndicatorDot(Utils.getCenterOfBlock(targetMiningBlocks[1]), 0, 255, 0);

        Communication.setTargetBlocks(targetMiningBlocks, true);

        /* ~~~ TROOP MANAGEMENT ~~~ */

        int closestEnemyThreatBlockDist = Integer.MAX_VALUE;
        int closestEnemyNonThreatBlockDist = Integer.MIN_VALUE;
        MapLocation closestEnemyThreatBlock = null;
        MapLocation closestEnemyNonThreatBlock = null;
        for (int x = 0; x < enemyBlockInfoType.length; x++) {
            for (int y = 0; y < enemyBlockInfoType[0].length; y++) {
                if (enemyBlockInfoType[x][y] == UNEXPLORED_BLOCK
                        || enemyBlockInfoType[x][y] == Communication.BLOCK_TYPE_NO_ENEMY) continue;

                MapLocation block = new MapLocation(x, y);
                int dist = block.distanceSquaredTo(myBlock);

                if (enemyBlockInfoType[x][y] == Communication.BLOCK_TYPE_THREAT) {
                    if (dist < closestEnemyThreatBlockDist) {
                        closestEnemyThreatBlock = block;
                        closestEnemyThreatBlockDist = dist;
                    }
                } else {
                    if (dist > closestEnemyNonThreatBlockDist) {
                        closestEnemyNonThreatBlock = block;
                        closestEnemyNonThreatBlockDist = dist;
                    }
                }

            }
        }

        Communication.setTargetBlocks(new MapLocation[] {closestEnemyThreatBlock, closestEnemyNonThreatBlock}, false);

        int roundNum = Memory.rc.getRoundNum();
        boolean canSeeEnemies = Memory.rc.senseNearbyRobots(RobotType.ARCHON.visionRadiusSquared, Memory.rc.getTeam().opponent()).length > 0;
        int mapArea = Memory.rc.getMapWidth() * Memory.rc.getMapHeight();

        int maxNumMiners = mapArea / 64;

        double roundProgress = (Math.min(1000, Math.max(50, roundNum)) - 50) / (1000. - 50.);
        double dangerLevel = 1. / Math.min(Math.max(1, closestEnemyThreatBlockDist), 50);

        double expFracSoldiers = (.8 - .2) * Math.max(roundProgress, dangerLevel) + .2;

        double expNumSoldiers = (soldierCount + minerCount) * expFracSoldiers;

        if (canSeeEnemies || soldierCount < expNumSoldiers || minerCount > maxNumMiners) {
            if (budget / numArchons >= RobotType.SOLDIER.buildCostLead) {
                Utils.tryBuild(RobotType.SOLDIER); // TODO spawn towards enemy if there is one
            }
        } else {
            if (budget / numArchons >= RobotType.MINER.buildCostLead) {
                Utils.tryBuild(RobotType.MINER); // TODO spawn towards lead block if there is one
            }
        }

        /* ~~~ COMMUNICATION ~~~ */

        if (Communication.getArchonIdx() + 1 >= Memory.rc.getArchonCount()) {
            Communication.resetBlockInfo();
        }

        Communication.updateArchonIdx();
        Communication.updateDroidCount();
    }

    private static void constructExplorationBlocks() throws GameActionException {
        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;

        MapLocation myBlock = Utils.getMyBlock();

        int startIdx = 0;
        for (int dist = 2; dist < 5; dist++) {
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
                    explorationBlocks[startIdx + numNewLocations] = potentialBlock;
                    numNewLocations++;
                }
            }

            if (numNewLocations == 0) break;

            // fisher-yates shuffle with blocks of same dist
            for (int i = numNewLocations - 1; i > 0; i--) {
                int swapIdx = startIdx + Utils.rng.nextInt(i + 1);
                MapLocation tmp = explorationBlocks[swapIdx];
                explorationBlocks[swapIdx] = explorationBlocks[startIdx + i];
                explorationBlocks[startIdx + i] = tmp;
            }

            startIdx += numNewLocations;
        }
    }

    private static boolean isClosestArchonToBlock(MapLocation block) throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();
        for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
            if (Communication.getArchonBlock(i).distanceSquaredTo(block) < myBlock.distanceSquaredTo(block)) {
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
            if (nextRoundMineable[mineableBlocks[i].x][mineableBlocks[i].y] != CURRENTLY_EXPLORING) {
                nextRoundMineable[mineableBlocks[i].x][mineableBlocks[i].y] = roundNum;
                addToFrontMineQueue(mineableBlocks[i]);
            }
        }

        MapLocation[] unmineableBlocks = Communication.getBlocks(Communication.BLOCK_TYPE_UNMINEABLE);
        for (int i = 0; i < unmineableBlocks.length; i++) {
            int curVal = nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y];
            if (curVal == UNEXPLORED_BLOCK) {
                curVal = Integer.MAX_VALUE;
            }

            int potVal = roundNum + 20 * (MIN_MINEABLE_LEAD - 1) / 5;
            if (potVal < curVal) {
                nextRoundMineable[unmineableBlocks[i].x][unmineableBlocks[i].y] = potVal;
                addToBackMineQueue(unmineableBlocks[i]);
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

    private static void addToBackMineQueue(MapLocation block) {
        mineBlocksDeque[mineBlocksBack] = block;
        mineBlocksBack = (mineBlocksBack + 1) % mineBlocksDeque.length;
    }

    private static void addToFrontMineQueue(MapLocation block) {
        mineBlocksFront = (mineBlocksFront - 1 + mineBlocksDeque.length) % mineBlocksDeque.length;
        mineBlocksDeque[mineBlocksFront] = block;
    }

    private static boolean isEmptyMineQueue() {
        return mineBlocksFront == mineBlocksBack;
    }

    private static MapLocation peekFrontMineQueue() {
        if (isEmptyMineQueue()) return null;
        return mineBlocksDeque[mineBlocksFront];
    }

    private static void removeFromFrontMineQueue() {
        mineBlocksFront = (mineBlocksFront + 1) % mineBlocksDeque.length;
    }

}