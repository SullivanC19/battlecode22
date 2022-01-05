package firstplayer;

import battlecode.common.*;

import java.util.Random;

public class Archon {
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
    static final RobotType[] droidTypes =
            new RobotType[] {RobotType.BUILDER, RobotType.MINER, RobotType.SAGE, RobotType.SOLDIER};

    public static void run(RobotController rc) throws GameActionException {
        tryBuild(droidTypes[rng.nextInt() % droidTypes.length], rc);
    }

    public static void tryBuild(RobotType robotType, RobotController rc) throws GameActionException {
        for (Direction dir : directions) {
            if (rc.canBuildRobot(robotType, dir)) {
                rc.buildRobot(robotType, dir);
            }
        }
    }
}