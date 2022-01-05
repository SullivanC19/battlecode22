package firstplayer;

import battlecode.common.*;

public class Laboratory {
    public static void run(RobotController rc) throws GameActionException {
        while (rc.canTransmute()) {
            rc.transmute();
        }
    }
}