package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Laboratory {
    public static void run() throws GameActionException {
        while (Memory.rc.canTransmute()) {
            Memory.rc.transmute();
        }
    }
}