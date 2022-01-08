package sprintbot;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pathfinder {
    private static MapLocation exploreLoc = null;
    private static final List<MapLocation> potentialEnemyArchonLocations = new ArrayList<>(Arrays.asList(
            new MapLocation(
                    Memory.rc.getMapWidth() - 1 - Memory.archonLocation.x,
                    Memory.rc.getMapHeight() - 1 - Memory.archonLocation.y),
            new MapLocation(
                    Memory.archonLocation.x,
                    Memory.rc.getMapHeight() - 1 - Memory.archonLocation.y),
            new MapLocation(
                    Memory.rc.getMapWidth() - 1 - Memory.archonLocation.x,
                    Memory.archonLocation.y)
    ));

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
            if (potentialEnemyArchonLocations.isEmpty()) {
                explore();
                return;
            }
            exploreLoc = potentialEnemyArchonLocations.remove((int) (Math.random() * potentialEnemyArchonLocations.size()));
        }

        moveToward(exploreLoc);

        if (Memory.rc.canSenseLocation(exploreLoc)) {
            RobotInfo robotAtLocation = Memory.rc.senseRobotAtLocation(exploreLoc);
            if (robotAtLocation == null
                    || robotAtLocation.getTeam() == Memory.rc.getTeam()
                    || robotAtLocation.getType() != RobotType.ARCHON) {
                exploreLoc = null;
            }
        }
    }

    public static void moveToward(MapLocation loc) throws GameActionException {
        Direction dir = Memory.rc.getLocation().directionTo(loc);
        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
        } else if (Memory.rc.canMove(dir.rotateRight())) {
            Memory.rc.move(dir.rotateRight());
        } else if (Memory.rc.canMove(dir.rotateLeft())) {
            Memory.rc.move(dir.rotateLeft());
        }
    }

    public static void shuffleRandomly() throws GameActionException {
        Direction dir = Utils.randomDirection();
        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
        }
    }
}
