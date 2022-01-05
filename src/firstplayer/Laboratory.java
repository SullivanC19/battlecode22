package firstplayer;

import battlecode.common.*;

import java.util.Random;

public class Laboratory {
    public static void run(RobotController rc) throws GameActionException {
        while (rc.canTransmute()) {
            rc.transmute();
        }
        Clock.yield();
    }
}