package qualificationbot_2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

import java.util.Arrays;

public class Communication {
    public static final int SHARED_ARRAY_SIZE = 64;
    public static final int SHARED_ARRAY_INTEGER_SIZE = 16;

    public static final int BLOCK_LOC_SIZE = Utils.BLOCK_SIZE * 2;

    // archon blocks (32)
    public static final int ARCHON_BLOCKS_SIZE = BLOCK_LOC_SIZE * Utils.MAX_NUM_ARCHONS;
    public static final int ARCHON_BLOCKS_START_BIT = 0;

    // archon idx (2)
    public static final int ARCHON_INDEX_SIZE = 2;
    public static final int ARCHON_INDEX_START_BIT = ARCHON_BLOCKS_START_BIT + ARCHON_BLOCKS_SIZE;

    // droid counts (30)
    public static final int BUILDER_COUNT_SIZE = 7;
    public static final int SAGE_COUNT_SIZE = 7;
    public static final int MINER_COUNT_SIZE = 8;
    public static final int SOLDIER_COUNT_SIZE = 8;
    public static final int DROID_COUNTS_SIZE = BUILDER_COUNT_SIZE + SAGE_COUNT_SIZE + MINER_COUNT_SIZE + SOLDIER_COUNT_SIZE;

    public static final int DROID_COUNTS_START_BIT = ARCHON_INDEX_START_BIT + ARCHON_INDEX_SIZE;
    public static final int BUILDER_COUNT_START_BIT = DROID_COUNTS_START_BIT;
    public static final int SAGE_COUNT_START_BIT = BUILDER_COUNT_START_BIT + BUILDER_COUNT_SIZE;
    public static final int MINER_COUNT_START_BIT = SAGE_COUNT_START_BIT + SAGE_COUNT_SIZE;
    public static final int SOLDIER_COUNT_START_BIT = MINER_COUNT_START_BIT + MINER_COUNT_SIZE;

    // target blocks (128)
    public static final int NUM_TARGET_BLOCKS = 12;

    public static final int TARGET_MINING_BLOCKS_SIZE = BLOCK_LOC_SIZE * NUM_TARGET_BLOCKS;
    public static final int TARGET_ENEMY_BLOCKS_SIZE = BLOCK_LOC_SIZE * NUM_TARGET_BLOCKS;
    public static final int TARGET_EXPLORE_BLOCKS_SIZE = BLOCK_LOC_SIZE * NUM_TARGET_BLOCKS;
    public static final int TARGET_BLOCKS_SIZE = TARGET_MINING_BLOCKS_SIZE + TARGET_ENEMY_BLOCKS_SIZE + TARGET_EXPLORE_BLOCKS_SIZE;

    public static final int TARGET_BLOCKS_START_BIT = DROID_COUNTS_START_BIT + DROID_COUNTS_SIZE;
    public static final int TARGET_MINING_BLOCKS_START_BIT = TARGET_BLOCKS_START_BIT;
    public static final int TARGET_ENEMY_BLOCKS_START_BIT = TARGET_MINING_BLOCKS_START_BIT + TARGET_MINING_BLOCKS_SIZE;
    public static final int TARGET_EXPLORE_BLOCKS_START_BIT = TARGET_ENEMY_BLOCKS_START_BIT + TARGET_ENEMY_BLOCKS_SIZE;

    // block info size (488)
    public static final int BLOCK_INFO_SECTION_COUNTS_SIZE = 32;
    public static final int NO_LEAD_BLOCKS_SIZE = BLOCK_LOC_SIZE * 12;
    public static final int NO_ENEMY_BLOCKS_SIZE = BLOCK_LOC_SIZE * 12;
    public static final int MINEABLE_BLOCKS_SIZE = BLOCK_LOC_SIZE * 6;
    public static final int UNMINEABLE_BLOCKS_SIZE = BLOCK_LOC_SIZE * 6;
    public static final int ENEMY_THREAT_BLOCKS_SIZE = BLOCK_LOC_SIZE * 6;
    public static final int ENEMY_NONTHREAT_BLOCKS_SIZE = BLOCK_LOC_SIZE * 6;
    public static final int BLOCK_INFO_SIZE = BLOCK_INFO_SECTION_COUNTS_SIZE + NO_LEAD_BLOCKS_SIZE + NO_ENEMY_BLOCKS_SIZE + MINEABLE_BLOCKS_SIZE + UNMINEABLE_BLOCKS_SIZE + ENEMY_THREAT_BLOCKS_SIZE + ENEMY_NONTHREAT_BLOCKS_SIZE;

    public static final int BLOCK_INFO_START_BIT = TARGET_BLOCKS_START_BIT + TARGET_BLOCKS_SIZE;
    public static final int BLOCK_INFO_SECTION_COUNTS_START_BIT = BLOCK_INFO_START_BIT;
    public static final int NO_LEAD_BLOCKS_START_BIT = BLOCK_INFO_SECTION_COUNTS_START_BIT + BLOCK_INFO_SECTION_COUNTS_SIZE;
    public static final int NO_ENEMY_BLOCKS_START_BIT = NO_LEAD_BLOCKS_START_BIT + NO_LEAD_BLOCKS_SIZE;
    public static final int MINEABLE_BLOCKS_START_BIT = NO_ENEMY_BLOCKS_START_BIT + NO_ENEMY_BLOCKS_SIZE;
    public static final int UNMINEABLE_BLOCKS_START_BIT = MINEABLE_BLOCKS_START_BIT + MINEABLE_BLOCKS_SIZE;
    public static final int ENEMY_THREAT_BLOCKS_START_BIT = UNMINEABLE_BLOCKS_START_BIT + UNMINEABLE_BLOCKS_SIZE;
    public static final int ENEMY_NONTHREAT_BLOCKS_START_BIT = ENEMY_THREAT_BLOCKS_START_BIT + ENEMY_THREAT_BLOCKS_SIZE;

    // budget (16)
    public static final int BUDGET_START_BIT = BLOCK_INFO_START_BIT + BLOCK_INFO_SIZE;
    public static final int BUDGET_SIZE = 16;

    // archon rectangles (32)
    public static final int ARCHON_RECT_START_BIT = BUDGET_START_BIT + BUDGET_SIZE;
    public static final int ARCHON_RECT_SIZE = Utils.MAX_NUM_ARCHONS * BLOCK_LOC_SIZE * 2;

    // labs built
    public static final int LABORATORY_COUNT_START_BIT = ARCHON_RECT_START_BIT + ARCHON_RECT_SIZE;
    public static final int LABORATORY_COUNT_SIZE = 2;

    // block type
    public static final int BLOCK_TYPE_NO_LEAD = 0;
    public static final int BLOCK_TYPE_NO_ENEMY = 1;
    public static final int BLOCK_TYPE_MINEABLE = 2;
    public static final int BLOCK_TYPE_UNMINEABLE = 3;
    public static final int BLOCK_TYPE_THREAT = 4;
    public static final int BLOCK_TYPE_NONTHREAT = 5;

    private static int lastBuilderCount = 0;
    private static int lastSageCount = 0;
    private static int lastMinerCount = 0;
    private static int lastSoldierCount = 0;

    private static int[][][] lastRoundBroadcasted = new int[6][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE][Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE];
    private static boolean[] updatedBlockInfoType = new boolean[6];

    private static int readValue(int startBit, int size) throws GameActionException {
        return (Memory.rc.readSharedArray(startBit / SHARED_ARRAY_INTEGER_SIZE) >> (startBit % SHARED_ARRAY_INTEGER_SIZE)) & ((1 << size) - 1);
    }

    private static void writeValue(int value, int startBit, int size) throws GameActionException {
        int i = startBit / SHARED_ARRAY_INTEGER_SIZE;
        int pos = (startBit % SHARED_ARRAY_INTEGER_SIZE);
        int curVal = Memory.rc.readSharedArray(i) & ~(((1 << size) - 1) << pos);
        Memory.rc.writeSharedArray(i, curVal | (value << pos));
    }

    private static int encodeBlock(MapLocation block) {
        if (block == null) return 255;
        return block.x + block.y * (Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE);
    }

    private static MapLocation decodeBlock(int encodedBlock) {
        if (encodedBlock == 255) return null;
        return new MapLocation(encodedBlock % (Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE), encodedBlock / (Utils.MAX_MAP_SIZE / Utils.BLOCK_SIZE));
    }

    public static void setArchonBlock() throws GameActionException {
        writeValue(encodeBlock(Utils.getBlock(Memory.archonLocation)), ARCHON_BLOCKS_START_BIT + getArchonIdx() * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
    }

    public static void updateArchonIdx() throws GameActionException {
        int nextArchonIdx = Memory.rc.getType() != RobotType.ARCHON ? 0 : ((readValue(ARCHON_INDEX_START_BIT, ARCHON_INDEX_SIZE) + 1) % Memory.rc.getArchonCount());
        writeValue(nextArchonIdx, ARCHON_INDEX_START_BIT, ARCHON_INDEX_SIZE);
    }

    public static void incrementDroidCount(RobotType type) throws GameActionException {
        int bit, size;
        switch (type) {
            case BUILDER: bit = BUILDER_COUNT_START_BIT; size = BUILDER_COUNT_SIZE;
                break;
            case SAGE: bit = SAGE_COUNT_START_BIT; size = SAGE_COUNT_SIZE;
                break;
            case MINER: bit = MINER_COUNT_START_BIT; size = MINER_COUNT_SIZE;
                break;
            case SOLDIER: bit = SOLDIER_COUNT_START_BIT; size = SOLDIER_COUNT_SIZE;
                break;
            default: bit = -1; size = -1;
        }

        if (bit != -1) {
            writeValue((readValue(bit, size) + 1) % (1 << size), bit, size);
        }
    }

    public static void updateDroidCount() throws GameActionException {
        lastBuilderCount = readValue(BUILDER_COUNT_START_BIT, BUILDER_COUNT_SIZE);
        lastSageCount = readValue(SAGE_COUNT_START_BIT, SAGE_COUNT_SIZE);
        lastMinerCount = readValue(MINER_COUNT_START_BIT, MINER_COUNT_SIZE);
        lastSoldierCount = readValue(SOLDIER_COUNT_START_BIT, SOLDIER_COUNT_SIZE);

        incrementDroidCount(Memory.rc.getType());
    }

    public static void setTargetMiningBlock(MapLocation targetBlock, int idx) throws GameActionException {
        writeValue(encodeBlock(targetBlock), TARGET_MINING_BLOCKS_START_BIT + idx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
    }

    public static void setTargetEnemyBlock(MapLocation targetBlock, int idx) throws GameActionException {
        writeValue(encodeBlock(targetBlock), TARGET_ENEMY_BLOCKS_START_BIT + idx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
    }

    public static void setTargetExploreBlock(MapLocation targetBlock, int idx) throws GameActionException {
        writeValue(encodeBlock(targetBlock), TARGET_EXPLORE_BLOCKS_START_BIT + idx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
    }

    public static MapLocation getTargetMiningBlock(int idx) throws GameActionException {
        return decodeBlock(readValue(TARGET_MINING_BLOCKS_START_BIT + idx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
    }

    public static MapLocation getTargetEnemyBlock(int idx) throws GameActionException {
        return decodeBlock(readValue(TARGET_ENEMY_BLOCKS_START_BIT + idx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
    }

    public static MapLocation getTargetExploreBlock(int idx) throws GameActionException {
        return decodeBlock(readValue(TARGET_EXPLORE_BLOCKS_START_BIT + idx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
    }

    public static void resetBlockInfo() throws GameActionException {
        for (int i = 0; i < BLOCK_INFO_SECTION_COUNTS_SIZE / 4; i++) {
            writeValue(0, BLOCK_INFO_SECTION_COUNTS_START_BIT + i * 4, 4);
        }
    }

    public static void resetBlockInfoBroadcasted() {
        Arrays.fill(updatedBlockInfoType, false);
    }

    private static void updateBlockInfoBroadcasted(int blockType) throws GameActionException {
        int size = readValue(BLOCK_INFO_SECTION_COUNTS_START_BIT + blockType * 4, 4);
        MapLocation[] broadcastedBlockInfo = getBlocks(blockType);
        for (int i = 0; i < size; i++) {
            lastRoundBroadcasted[blockType][broadcastedBlockInfo[i].x][broadcastedBlockInfo[i].y] = Memory.rc.getRoundNum();
        }
        updatedBlockInfoType[blockType] = true;
    }

    public static boolean addBlockInfo(int blockType, MapLocation block) throws GameActionException {
        if (!updatedBlockInfoType[blockType]) updateBlockInfoBroadcasted(blockType);

        if (lastRoundBroadcasted[blockType][block.x][block.y] == Memory.rc.getRoundNum()) return false;

        int startIdx;
        switch (blockType) {
            case BLOCK_TYPE_NO_LEAD: startIdx = NO_LEAD_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NO_ENEMY: startIdx = NO_ENEMY_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_MINEABLE: startIdx = MINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_UNMINEABLE: startIdx = UNMINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_THREAT: startIdx = ENEMY_THREAT_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NONTHREAT: startIdx = ENEMY_NONTHREAT_BLOCKS_START_BIT;
                break;
            default: startIdx = -1;
        }

        int size = readValue(BLOCK_INFO_SECTION_COUNTS_START_BIT + blockType * 4, 4);

        if (size >= ((blockType >= BLOCK_TYPE_MINEABLE) ? 6 : 12)) return false;

        writeValue(encodeBlock(block), startIdx + size * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
        writeValue(size + 1, BLOCK_INFO_SECTION_COUNTS_START_BIT + blockType * 4, 4);

        lastRoundBroadcasted[blockType][block.x][block.y] = Memory.rc.getRoundNum();

        return true;
    }

    public static void setBudget(int budget) throws GameActionException {
        writeValue(budget, BUDGET_START_BIT, BUDGET_SIZE);
    }

    public static MapLocation getArchonBlock(int archonIdx) throws GameActionException {
        return decodeBlock(readValue(ARCHON_BLOCKS_START_BIT + archonIdx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
    }

    public static int getArchonIdx() throws GameActionException {
        if (Memory.rc.getType() == RobotType.ARCHON) {
            return readValue(ARCHON_INDEX_START_BIT, ARCHON_INDEX_SIZE);
        }

        MapLocation myArchonBlock = Utils.getBlock(Memory.archonLocation);
        for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
            MapLocation archonBlock = Communication.getArchonBlock(i);
            if (archonBlock != null && archonBlock.equals(myArchonBlock)) {
                return i;
            }
        }

        return 0;
    }

    public static int getDroidCount(RobotType robotType) throws GameActionException {
        int bit, size, lastCount;
        switch (robotType) {
            case BUILDER: bit = BUILDER_COUNT_START_BIT; size = BUILDER_COUNT_SIZE; lastCount = lastBuilderCount;
                break;
            case SAGE: bit = SAGE_COUNT_START_BIT; size = SAGE_COUNT_SIZE; lastCount = lastSageCount;
                break;
            case MINER: bit = MINER_COUNT_START_BIT; size = MINER_COUNT_SIZE; lastCount = lastMinerCount;
                break;
            case SOLDIER: bit = SOLDIER_COUNT_START_BIT; size = SOLDIER_COUNT_SIZE; lastCount = lastSoldierCount;
                break;
            default: return 0;
        }

        int maxCount = 1 << size;
        return (readValue(bit, size) - lastCount + (Memory.rc.getType() == robotType ? 1 : 0) + maxCount) % maxCount;
    }

    public static MapLocation[] getBlocks(int blockType) throws GameActionException {
        int startIdx;
        switch (blockType) {
            case BLOCK_TYPE_NO_LEAD: startIdx = NO_LEAD_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NO_ENEMY: startIdx = NO_ENEMY_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_MINEABLE: startIdx = MINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_UNMINEABLE: startIdx = UNMINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_THREAT: startIdx = ENEMY_THREAT_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NONTHREAT: startIdx = ENEMY_NONTHREAT_BLOCKS_START_BIT;
                break;
            default: return new MapLocation[0];
        }

        int size = readValue(BLOCK_INFO_SECTION_COUNTS_START_BIT + 4 * blockType, 4);
        MapLocation[] blocks = new MapLocation[size];

        for (int i = 0; i < size; i++) {
            blocks[i] = decodeBlock(readValue(startIdx + i * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
        }

        return blocks;
    }

    public static int getBudget() throws GameActionException {
        return readValue(BUDGET_START_BIT, BUDGET_SIZE);
    }

    public static void setArchonRectangle(int archonIdx, MapLocation[] rect) throws GameActionException {
        int startBit = ARCHON_RECT_START_BIT + archonIdx * BLOCK_LOC_SIZE * 2;
        writeValue(encodeBlock(rect[0]), startBit, BLOCK_LOC_SIZE);
        writeValue(encodeBlock(rect[1]), startBit + BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
    }

    public static MapLocation[] getArchonRectangle(int archonIdx) throws GameActionException {
        int startBit = ARCHON_RECT_START_BIT + archonIdx * BLOCK_LOC_SIZE * 2;
        return new MapLocation[] {
                decodeBlock(readValue(startBit, BLOCK_LOC_SIZE)),
                decodeBlock(readValue(startBit + BLOCK_LOC_SIZE, BLOCK_LOC_SIZE))
        };
    }

    public static int getLaboratoryCount() throws GameActionException {
        return readValue(LABORATORY_COUNT_START_BIT, LABORATORY_COUNT_SIZE);
    }

    public static void incrementLaboratoryCount() throws GameActionException {
        int laboratoryCount = getLaboratoryCount() + 1;
        writeValue(laboratoryCount, LABORATORY_COUNT_START_BIT, 2);
    }

}
