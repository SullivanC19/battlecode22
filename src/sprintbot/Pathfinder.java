package sprintbot;

import battlecode.common.*;

public class Pathfinder {
    private static MapLocation exploreLoc = null;
    public static void explore() throws GameActionException {
        if (exploreLoc == null) {
            int width = Memory.rc.getMapWidth();
            int height = Memory.rc.getMapHeight();

            int x = Memory.archonLocation.x + Utils.rng.nextInt(width * 2 + 1) - width;
            int y = Memory.archonLocation.y + Utils.rng.nextInt(height * 2 + 1) - height;

            x = Math.max(0, Math.min(width - 1, x));
            y = Math.max(0, Math.min(height - 1, y));
            exploreLoc = new MapLocation(x, y);
        }

        Direction dir = Memory.rc.getLocation().directionTo(exploreLoc);
        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
        } else if (Memory.rc.getMovementCooldownTurns() == 0) {
            exploreLoc = null;
        }
    }

    public static void exploreEnemyArchons() throws GameActionException {
        if (exploreLoc == null) {

        }
        int width = Memory.rc.getMapWidth();
        int height = Memory.rc.getMapHeight();


    }
}
