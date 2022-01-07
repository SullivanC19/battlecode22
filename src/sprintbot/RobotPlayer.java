package sprintbot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.util.Random;

public strictfp class RobotPlayer {

    static int turnCount = 0;
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

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                switch (rc.getType()) {
                    case ARCHON:        Archon.run(rc);     break;
                    case MINER:         Miner.run(rc);      break;
                    case SOLDIER:       Soldier.run(rc);    break;
                    case LABORATORY:    Laboratory.run(rc); break;
                    case WATCHTOWER:    Watchtower.run(rc); break;
                    case BUILDER:       Builder.run(rc);    break;
                    case SAGE:          Sage.run(rc);       break;
                }
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }

            turnCount++;
        }
    }
}
