package sprintbot;

import battlecode.common.*;

public class Communication {
    public static final int SHARED_ARRAY_SIZE = 64;
    public static final int SHARED_ARRAY_INTEGER_SIZE = 16;

    public static final int BLOCK_LOC_SIZE = 8;

    // archon blocks
    public static final int ARCHON_BLOCKS_SIZE = BLOCK_LOC_SIZE * Utils.MAX_NUM_ARCHONS;
    public static final int ARCHON_BLOCKS_START_BIT = 0;

    // archon idx
    public static final int ARCHON_INDEX_SIZE = 2;
    public static final int ARCHON_INDEX_START_BIT = ARCHON_BLOCKS_START_BIT + ARCHON_BLOCKS_SIZE;

    // droid counts
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

    // target blocks
    public static final int TARGET_MINING_BLOCKS_SIZE = BLOCK_LOC_SIZE * 2 * Utils.MAX_NUM_ARCHONS;
    public static final int TARGET_ENEMY_BLOCKS_SIZE = BLOCK_LOC_SIZE * 2 * Utils.MAX_NUM_ARCHONS;
    public static final int TARGET_BLOCKS_SIZE = TARGET_MINING_BLOCKS_SIZE + TARGET_ENEMY_BLOCKS_SIZE;

    public static final int TARGET_BLOCKS_START_BIT = DROID_COUNTS_START_BIT + DROID_COUNTS_SIZE;
    public static final int TARGET_MINING_BLOCKS_START_BIT = TARGET_BLOCKS_START_BIT;
    public static final int TARGET_ENEMY_BLOCKS_START_BIT = TARGET_MINING_BLOCKS_START_BIT + TARGET_MINING_BLOCKS_SIZE;

    // enemy archon locations
    public static final int ENEMY_ARCHON_BLOCKS_SIZE = BLOCK_LOC_SIZE * Utils.MAX_NUM_ARCHONS;
    public static final int ENEMY_ARCHON_SET_SIZE = 8;
    public static final int ENEMY_ARCHON_SIZE = ENEMY_ARCHON_BLOCKS_SIZE + ENEMY_ARCHON_SET_SIZE;

    public static final int ENEMY_ARCHON_START_BIT = TARGET_BLOCKS_START_BIT + TARGET_BLOCKS_SIZE;
    public static final int ENEMY_ARCHON_SET_START_BIT = ENEMY_ARCHON_START_BIT;
    public static final int ENEMY_ARCHON_BLOCKS_START_BIT = ENEMY_ARCHON_SET_START_BIT + ENEMY_ARCHON_SET_SIZE;

    // block info size
    public static final int BLOCK_INFO_ARRAY_COUNT_SIZE = BLOCK_LOC_SIZE;

    public static final int NO_LEAD_BLOCKS_SIZE = BLOCK_INFO_ARRAY_COUNT_SIZE + BLOCK_LOC_SIZE * 16;
    public static final int NO_ENEMY_BLOCKS_SIZE = BLOCK_INFO_ARRAY_COUNT_SIZE + BLOCK_LOC_SIZE * 16;
    public static final int MINEABLE_BLOCKS_SIZE = BLOCK_INFO_ARRAY_COUNT_SIZE + BLOCK_LOC_SIZE * 16;
    public static final int UNMINEABLE_BLOCKS_SIZE = BLOCK_INFO_ARRAY_COUNT_SIZE + BLOCK_LOC_SIZE * 16;
    public static final int ENEMY_THREAT_BLOCKS_SIZE = BLOCK_INFO_ARRAY_COUNT_SIZE + BLOCK_LOC_SIZE * 16;
    public static final int ENEMY_NONTHREAT_BLOCKS_SIZE = BLOCK_INFO_ARRAY_COUNT_SIZE + BLOCK_LOC_SIZE * 16;
    public static final int BLOCK_INFO_SIZE = NO_LEAD_BLOCKS_SIZE + NO_ENEMY_BLOCKS_SIZE + MINEABLE_BLOCKS_SIZE + UNMINEABLE_BLOCKS_SIZE + ENEMY_THREAT_BLOCKS_SIZE + ENEMY_NONTHREAT_BLOCKS_SIZE;

    public static final int BLOCK_INFO_START_BIT = ENEMY_ARCHON_START_BIT + ENEMY_ARCHON_SIZE;
    public static final int NO_LEAD_BLOCKS_START_BIT = BLOCK_INFO_START_BIT;
    public static final int NO_ENEMY_BLOCKS_START_BIT = NO_LEAD_BLOCKS_START_BIT + NO_LEAD_BLOCKS_SIZE;
    public static final int MINEABLE_BLOCKS_START_BIT = NO_ENEMY_BLOCKS_START_BIT + NO_ENEMY_BLOCKS_SIZE;
    public static final int UNMINEABLE_BLOCKS_START_BIT = MINEABLE_BLOCKS_START_BIT + MINEABLE_BLOCKS_SIZE;
    public static final int ENEMY_THREAT_BLOCKS_START_BIT = UNMINEABLE_BLOCKS_START_BIT + UNMINEABLE_BLOCKS_SIZE;
    public static final int ENEMY_NONTHREAT_BLOCKS_START_BIT = ENEMY_THREAT_BLOCKS_START_BIT + ENEMY_THREAT_BLOCKS_SIZE;

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
        writeValue(
                (readValue(ARCHON_INDEX_START_BIT, ARCHON_INDEX_SIZE) + 1) % Memory.rc.getArchonCount(),
                ARCHON_INDEX_START_BIT,
                ARCHON_INDEX_SIZE);
    }

    public static void updateDroidCount() throws GameActionException {
        lastBuilderCount = readValue(BUILDER_COUNT_START_BIT, BUILDER_COUNT_SIZE);
        lastSageCount = readValue(SAGE_COUNT_START_BIT, SAGE_COUNT_SIZE);
        lastMinerCount = readValue(MINER_COUNT_START_BIT, MINER_COUNT_SIZE);
        lastSoldierCount = readValue(SOLDIER_COUNT_START_BIT, SOLDIER_COUNT_SIZE);

        int bit, size;
        switch (Memory.rc.getType()) {
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

    public static void setTargetBlocks(MapLocation[] targetBlocks, boolean isMining) throws GameActionException {
        int bit = (isMining ? TARGET_MINING_BLOCKS_START_BIT : TARGET_ENEMY_BLOCKS_START_BIT) + getArchonIdx() * BLOCK_LOC_SIZE * 2;
        writeValue(encodeBlock(targetBlocks[0]), bit, BLOCK_LOC_SIZE);
        writeValue(encodeBlock(targetBlocks[1]), bit + BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
    }

    public static void addEnemyArchonBlock(MapLocation enemyArchonBlock) throws GameActionException {
        for (int i = 0; i < Utils.MAX_NUM_ARCHONS; i++) {
            if (readValue(ENEMY_ARCHON_SET_START_BIT + i, 1) == 0) {
                writeValue(
                        encodeBlock(enemyArchonBlock),
                        ENEMY_ARCHON_BLOCKS_START_BIT + i * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
                writeValue(0,ENEMY_ARCHON_SET_START_BIT + i, 1);
                break;
            }
        }
    }

    public static void removeEnemyArchonBlock(MapLocation enemyArchonBlock) throws GameActionException {
        for (int i = 0; i < Utils.MAX_NUM_ARCHONS; i++) {
            MapLocation block = decodeBlock(readValue(ENEMY_ARCHON_BLOCKS_START_BIT + i * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
            if (block.equals(enemyArchonBlock)) {
                writeValue(0,ENEMY_ARCHON_SET_START_BIT + i, 1);
                break;
            }
        }
    }

    public static void resetBlockInfo() throws GameActionException {
        writeValue(0, NO_LEAD_BLOCKS_START_BIT, BLOCK_INFO_ARRAY_COUNT_SIZE);
        writeValue(0, NO_ENEMY_BLOCKS_START_BIT, BLOCK_INFO_ARRAY_COUNT_SIZE);
        writeValue(0, MINEABLE_BLOCKS_START_BIT, BLOCK_INFO_ARRAY_COUNT_SIZE);
        writeValue(0, UNMINEABLE_BLOCKS_START_BIT, BLOCK_INFO_ARRAY_COUNT_SIZE);
        writeValue(0, ENEMY_THREAT_BLOCKS_START_BIT, BLOCK_INFO_ARRAY_COUNT_SIZE);
        writeValue(0, ENEMY_NONTHREAT_BLOCKS_START_BIT, BLOCK_INFO_ARRAY_COUNT_SIZE);
    }

    public static void addBlockInfo(int blockType, MapLocation block) throws GameActionException {
        int sizeIdx;
        switch (blockType) {
            case BLOCK_TYPE_NO_LEAD: sizeIdx = NO_LEAD_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NO_ENEMY: sizeIdx = NO_ENEMY_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_MINEABLE: sizeIdx = MINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_UNMINEABLE: sizeIdx = UNMINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_THREAT: sizeIdx = ENEMY_THREAT_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NONTHREAT: sizeIdx = ENEMY_NONTHREAT_BLOCKS_START_BIT;
                break;
            default: sizeIdx = -1;
        }

        int size = readValue(sizeIdx, BLOCK_INFO_ARRAY_COUNT_SIZE);
        if (size >= (blockType <= BLOCK_TYPE_NO_ENEMY ? 8 : 16)) return;

        for (int i = 0; i < size; i++) {
            MapLocation existingBlock = decodeBlock(readValue(sizeIdx + BLOCK_INFO_ARRAY_COUNT_SIZE + i * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
            if (block.equals(existingBlock)) return;
        }

        writeValue(encodeBlock(block), sizeIdx + BLOCK_INFO_ARRAY_COUNT_SIZE + size * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE);
        writeValue(size + 1, sizeIdx, BLOCK_INFO_ARRAY_COUNT_SIZE);
    }

    public static MapLocation getArchonBlock(int archonIdx) throws GameActionException {
        return decodeBlock(readValue(ARCHON_BLOCKS_START_BIT + archonIdx * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
    }

    public static int getArchonIdx() throws GameActionException {
        return readValue(ARCHON_INDEX_START_BIT, ARCHON_INDEX_SIZE);
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
        Memory.rc.setIndicatorString(readValue(bit, size) + " - " + lastCount);
        return (readValue(bit, size) - lastCount + (Memory.rc.getType() == robotType ? 1 : 0) + maxCount) % maxCount;
    }

    public static MapLocation[] getAllTargetBlocks(boolean isMining) throws GameActionException {
        int startBit = isMining ? TARGET_MINING_BLOCKS_START_BIT : TARGET_ENEMY_BLOCKS_START_BIT;
        MapLocation[] targetBlocks = new MapLocation[TARGET_MINING_BLOCKS_SIZE / BLOCK_LOC_SIZE];
        for (int i = 0; i < Memory.rc.getArchonCount(); i++) {
            targetBlocks[i * 2] =
                    decodeBlock(readValue(startBit + (i * 2) * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
            targetBlocks[i * 2 + 1] =
                    decodeBlock(readValue(startBit + (i * 2 + 1) * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
        }
        return targetBlocks;
    }

    public static MapLocation[] getEnemyArchonBlocks() throws GameActionException {
        int size = Integer.bitCount(readValue(ENEMY_ARCHON_SET_START_BIT, Utils.MAX_NUM_ARCHONS));
        MapLocation[] enemyArchonBlocks = new MapLocation[size];
        for (int i = 0; i < size; i++) {
            enemyArchonBlocks[i] = decodeBlock(ENEMY_ARCHON_BLOCKS_START_BIT + i * BLOCK_LOC_SIZE);
        }

        return enemyArchonBlocks;
    }

    public static MapLocation[] getBlocks(int blockType) throws GameActionException {
        int sizeIdx;
        switch (blockType) {
            case BLOCK_TYPE_NO_LEAD: sizeIdx = NO_LEAD_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NO_ENEMY: sizeIdx = NO_ENEMY_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_MINEABLE: sizeIdx = MINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_UNMINEABLE: sizeIdx = UNMINEABLE_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_THREAT: sizeIdx = ENEMY_THREAT_BLOCKS_START_BIT;
                break;
            case BLOCK_TYPE_NONTHREAT: sizeIdx = ENEMY_NONTHREAT_BLOCKS_START_BIT;
                break;
            default: return new MapLocation[0];
        }

        int size = readValue(sizeIdx, BLOCK_INFO_ARRAY_COUNT_SIZE);
        MapLocation[] blocks = new MapLocation[size];

        for (int i = 0; i < size; i++) {
            blocks[i] = decodeBlock(readValue(sizeIdx + BLOCK_INFO_ARRAY_COUNT_SIZE + i * BLOCK_LOC_SIZE, BLOCK_LOC_SIZE));
        }

        return blocks;
    }

}
