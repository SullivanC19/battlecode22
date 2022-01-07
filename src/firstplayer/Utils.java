package firstplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

public class Utils {
    static final Random rng = new Random(6147);
    static final Direction[] directions = {
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

    public static boolean tryBuild(RobotType robotType, RobotController rc) throws GameActionException {
        for (Direction dir : directions) {
            if (rc.canBuildRobot(robotType, dir)) {
                rc.buildRobot(robotType, dir);
                return true;
            }
        }

        return false;
    }

    public static RobotInfo getClosestEnemyRobot(RobotController rc) {
        RobotInfo[] nearbyEnemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());

        RobotInfo closestEnemyRobot = null;
        int closestDist = Integer.MAX_VALUE;
        for (RobotInfo ri : nearbyEnemyRobots) {
            int dist = ri.getLocation().distanceSquaredTo(rc.getLocation());
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
