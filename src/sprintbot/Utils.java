package sprintbot;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

public class Utils {
    public static final int MAX_MAP_SIZE = 64;
    public static final int LEAD_BLOCK_SIZE = 4;
    public static final int NUM_BLOCKS = MAX_MAP_SIZE / LEAD_BLOCK_SIZE;

    public static final Random rng = new Random(6147);
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static final RobotType[] droidTypes =
            new RobotType[] {RobotType.SOLDIER, RobotType.SAGE, RobotType.MINER, RobotType.BUILDER};
    public static final RobotType[] leadDroidTypes =
            new RobotType[] {RobotType.SOLDIER, RobotType.MINER, RobotType.BUILDER};
    public static final RobotType[] buildingTypes =
            new RobotType[] {RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.LABORATORY};
    public static final RobotType[] buildableTypes =
            new RobotType[] {RobotType.WATCHTOWER, RobotType.LABORATORY};

    public static boolean isDroidType(RobotType robotType) {
        return Arrays.asList(droidTypes).contains(robotType);
    }

    public static boolean isBuildingType(RobotType robotType) {
        return Arrays.asList(buildingTypes).contains(robotType);
    }

    public static boolean isBuildableType(RobotType robotType) {
        return Arrays.asList(buildableTypes).contains(robotType);
    }

    public static boolean tryBuild(RobotType robotType) throws GameActionException {
        for (Direction dir : directions) {
            if (Memory.rc.canBuildRobot(robotType, dir)) {
                Memory.rc.buildRobot(robotType, dir);
                return true;
            }
        }

        return false;
    }

    public static RobotInfo getClosestEnemyRobot() {
        int visionRadiusSquared = Memory.rc.getType().visionRadiusSquared;
        Team opponent = Memory.rc.getTeam().opponent();
        RobotInfo[] nearbyEnemyRobots = Memory.rc.senseNearbyRobots(visionRadiusSquared, opponent);

        RobotInfo closestEnemyRobot = null;
        int closestDist = Integer.MAX_VALUE;
        for (RobotInfo ri : nearbyEnemyRobots) {
            int dist = ri.getLocation().distanceSquaredTo(Memory.rc.getLocation());
            if (dist < closestDist) {
                closestDist = dist;
                closestEnemyRobot = ri;
            }
        }

        return closestEnemyRobot;
    }

    public static Direction randomDirection() {
        return directions[rng.nextInt(directions.length)];
    }

}
