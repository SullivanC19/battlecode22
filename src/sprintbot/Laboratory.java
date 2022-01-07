package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Laboratory {
    public static void run(RobotController rc) throws GameActionException {
        while (rc.canTransmute()) {
            rc.transmute();
        }
    }
}