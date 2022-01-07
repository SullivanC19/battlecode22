package sprintbot;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class Communication {
    public static final int MAX_MAP_SIZE = 64;
    public static final int BLOCK_SIZE = 4;
    public static final int SHARED_ARRAY_SIZE = 64;
    public static final int SHARED_ARRAY_INTEGER_SIZE = 16;

    public static void updateFood(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int blockX = loc.x / BLOCK_SIZE;
        int blockY = loc.y / BLOCK_SIZE;

        boolean sensesFoodInBlock = false;
        for (MapLocation leadLoc : rc.senseNearbyLocationsWithLead((BLOCK_SIZE - 1) * (BLOCK_SIZE - 1) * 2)) {
            if (leadLoc.x / BLOCK_SIZE == blockX && leadLoc.y / BLOCK_SIZE == blockY) {
                sensesFoodInBlock = true;
                break;
            }
        }

        setFood(blockX, blockY, sensesFoodInBlock, rc);
    }

    private static int getDroidCount(RobotType robotType) {
        switch (robotType) {
            case BUILDER:
                return 0;
            case SOLDIER:
                return 0;
            case MINER:
                return 0;
            case SAGE:
                return 0;
            default:
                return 0;
        }
    }

    private static int getBlockIdx(int blockX, int blockY) {
        return blockX * (MAX_MAP_SIZE / BLOCK_SIZE) + blockY;
    }

    private static void setFood(int blockX, int blockY, boolean food, RobotController rc) throws GameActionException {
        int v = food ? 1 : 0;

        int idx = getBlockIdx(blockX, blockY);
        int i = idx / SHARED_ARRAY_INTEGER_SIZE;
        int n = idx % SHARED_ARRAY_INTEGER_SIZE;

        // change bit operation taken from https://stackoverflow.com/questions/47981/how-do-you-set-clear-and-toggle-a-single-bit
        int val = rc.readSharedArray(i);
        val ^= (-v ^ val) & (1 << n);

        rc.writeSharedArray(i, val);
    }

    public static boolean isFood(int blockX, int blockY, RobotController rc) throws GameActionException {
        int idx = getBlockIdx(blockX, blockY);
        int i = idx / SHARED_ARRAY_INTEGER_SIZE;
        int n = idx % SHARED_ARRAY_INTEGER_SIZE;

        int val = rc.readSharedArray(i);
        return (val & (1 << n)) > 0;
    }

    public static List<MapLocation> getAllFoodBlocks(RobotController rc) throws GameActionException {
        List<MapLocation> foodBlocks = new ArrayList<>();
        for (int i = 0; i < (MAX_MAP_SIZE / BLOCK_SIZE) * (MAX_MAP_SIZE / BLOCK_SIZE) / SHARED_ARRAY_INTEGER_SIZE; i++) {
            int val = rc.readSharedArray(i);
            if (val == 0) continue;

            for (int n = 0; n < SHARED_ARRAY_INTEGER_SIZE; n++) {
                if ((val & (1 << n)) > 0) {
                    int idx = i * SHARED_ARRAY_INTEGER_SIZE + n;
                    foodBlocks.add(new MapLocation(idx / (MAX_MAP_SIZE / BLOCK_SIZE), idx % (MAX_MAP_SIZE / BLOCK_SIZE)));
                }
            }
        }

        return foodBlocks;

    }

    public static int getTotalFoodArea(RobotController rc) throws GameActionException {
        int tot = 0;
        for (int i = 0; i < (MAX_MAP_SIZE / BLOCK_SIZE) * (MAX_MAP_SIZE / BLOCK_SIZE) / SHARED_ARRAY_INTEGER_SIZE; i++) {
            tot += Integer.bitCount(rc.readSharedArray(i));
        }
        return tot;
    }
}
