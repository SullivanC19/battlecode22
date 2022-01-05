package firstplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

public class Builder {

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
    static final RobotType[] buildingTypes = new RobotType[] {RobotType.LABORATORY, RobotType.WATCHTOWER, RobotType.ARCHON};

    public static void run(RobotController rc) throws GameActionException {
        // find closest friendly repairable building
        MapLocation closestRepairLocation = null;
        int closestRepairDist = Integer.MAX_VALUE;
        for (RobotInfo ri : rc.senseNearbyRobots()) {
            if (ri.getTeam() == rc.getTeam()
            && ri.getHealth() < ri.getType().getMaxHealth(1 )
            && isRepairableType(ri.getType())
            && ri.getLocation().distanceSquaredTo(rc.getLocation()) < closestRepairDist) {
                closestRepairLocation = ri.getLocation();
                closestRepairDist = closestRepairLocation.distanceSquaredTo(rc.getLocation());
            }
        }

        // move toward closest friendly building or move randomly
        Direction dir = directions[rng.nextInt(directions.length)];
        if (closestRepairLocation != null) {
            dir = rc.getLocation().directionTo(closestRepairLocation);
        }

        if (rc.canMove(dir)) {
            rc.move(dir);
        }

        // if possible, repair closest friendly building
        while (rc.canRepair(closestRepairLocation)) {
            rc.repair(closestRepairLocation);
        }

        // build buildings sometimes randomly
        if (rng.nextInt() % 50 == 0) {
            RobotType buildingType = buildingTypes[rng.nextInt() % (buildingTypes.length - 1)]; // can't build archons
            tryBuild(buildingType, rc);
        }
    }

    public static boolean isRepairableType(RobotType robotType) {
        return Arrays.asList(buildingTypes).contains(robotType);
    }

    public static void tryBuild(RobotType robotType, RobotController rc) throws GameActionException {
        for (Direction dir : directions) {
            if (rc.canBuildRobot(robotType, dir)) {
                rc.buildRobot(robotType, dir);
            }
        }
    }
}
