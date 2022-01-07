package sprintbot;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class Communication {
    public static final int SHARED_ARRAY_SIZE = 64;
    public static final int SHARED_ARRAY_INTEGER_SIZE = 16;

    public static final int LEAD_BLOCK_ARRAY_SIZE = Utils.NUM_BLOCKS * Utils.NUM_BLOCKS / SHARED_ARRAY_INTEGER_SIZE;
    public static final int DROID_COUNTS_ARRAY_SIZE = Utils.droidTypes.length * Utils.MAX_NUM_ARCHONS;

    public static void updateLead() throws GameActionException {
        MapLocation loc = Memory.rc.getLocation();
        int blockX = loc.x / Utils.LEAD_BLOCK_SIZE;
        int blockY = loc.y / Utils.LEAD_BLOCK_SIZE;

        boolean sensesLeadInBlock = false;
        int blockRange = (Utils.LEAD_BLOCK_SIZE - 1) * (Utils.LEAD_BLOCK_SIZE - 1) * 2;
        for (MapLocation leadLoc : Memory.rc.senseNearbyLocationsWithLead(blockRange)) {
            if (leadLoc.x / Utils.LEAD_BLOCK_SIZE == blockX
                    && leadLoc.y / Utils.LEAD_BLOCK_SIZE == blockY) {
                sensesLeadInBlock = true;
                break;
            }
        }

        setLeadBlock(blockX, blockY, sensesLeadInBlock);
    }

    public static void updateDroidCount() throws GameActionException {
        // archon's reset the droid count
        if (Memory.rc.getType() == RobotType.ARCHON) {
            int firstIdx = LEAD_BLOCK_ARRAY_SIZE + getDroidCountIdx(Memory.archonId, 0);
            for (int i = 0; i < Utils.droidTypes.length; i++) {
                Memory.rc.writeSharedArray(firstIdx + i, 0);
            }
            return;
        }

        int droidTypeIdx = Utils.getDroidTypeIdx(Memory.rc.getType());
        if (droidTypeIdx == -1) return;

        int i = LEAD_BLOCK_ARRAY_SIZE + getDroidCountIdx(Memory.archonId, droidTypeIdx);
        int curCount = Memory.rc.readSharedArray(i);
        Memory.rc.writeSharedArray(i, curCount + 1);
    }

    private static int getDroidCountIdx(int archonId, int droidTypeIdx) {
        return archonId * Utils.droidTypes.length + droidTypeIdx;
    }

    public static int getDroidCount(RobotType robotType) throws GameActionException {
        int droidTypeIdx = Utils.getDroidTypeIdx(robotType);
        return Memory.rc.readSharedArray(LEAD_BLOCK_ARRAY_SIZE + getDroidCountIdx(Memory.archonId, droidTypeIdx));
    }

    private static int getBlockIdx(int blockX, int blockY) {
        return blockX * Utils.NUM_BLOCKS + blockY;
    }

    private static void setLeadBlock(int blockX, int blockY, boolean isLead) throws GameActionException {
        int v = isLead ? 1 : 0;

        int idx = getBlockIdx(blockX, blockY);
        int i = idx / SHARED_ARRAY_INTEGER_SIZE;
        int n = idx % SHARED_ARRAY_INTEGER_SIZE;

        // change bit operation taken from https://stackoverflow.com/questions/47981/how-do-you-set-clear-and-toggle-a-single-bit
        int val = Memory.rc.readSharedArray(i);
        val ^= (-v ^ val) & (1 << n);

        Memory.rc.writeSharedArray(i, val);
    }

    public static boolean isLead(int blockX, int blockY) throws GameActionException {
        int idx = getBlockIdx(blockX, blockY);
        int i = idx / SHARED_ARRAY_INTEGER_SIZE;
        int n = idx % SHARED_ARRAY_INTEGER_SIZE;

        int val = Memory.rc.readSharedArray(i);
        return (val & (1 << n)) > 0;
    }

    public static List<MapLocation> getAllLeadBlocks() throws GameActionException {
        List<MapLocation> leadBlocks = new ArrayList<>();
        for (int i = 0; i < LEAD_BLOCK_ARRAY_SIZE; i++) {
            int val = Memory.rc.readSharedArray(i);
            if (val == 0) continue;

            for (int n = 0; n < SHARED_ARRAY_INTEGER_SIZE; n++) {
                if ((val & (1 << n)) > 0) {
                    int idx = i * SHARED_ARRAY_INTEGER_SIZE + n;
                    leadBlocks.add(
                            new MapLocation(idx / Utils.NUM_BLOCKS,
                                            idx % Utils.NUM_BLOCKS));
                }
            }
        }

        return leadBlocks;

    }

    public static int getTotalLeadArea() throws GameActionException {
        int tot = 0;
        for (int i = 0; i < LEAD_BLOCK_ARRAY_SIZE; i++) {
            tot += Integer.bitCount(Memory.rc.readSharedArray(i));
        }
        return tot;
    }
}
