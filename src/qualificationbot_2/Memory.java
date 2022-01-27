package qualificationbot_2;

import battlecode.common.*;

import java.util.Random;

public class Memory {
    public static RobotController rc;

    public static int archonId;
    public static MapLocation archonLocation;

    public static MapLocation myBlock;

    public static RobotInfo[] robotsList;
    public static int numMinersInBlock;

    public static void init(RobotController rc) throws GameActionException {
        Memory.rc = rc;
        Utils.rng = new Random(Memory.rc.getID());

        if (rc.getType() == RobotType.ARCHON) {
            initArchon();
        } else if (Utils.isDroidType(rc.getType())) {
            initDroid();
        } else {
            initBuilding();
        }
    }

    private static void initArchon() throws GameActionException {
        archonId = rc.getID();
        archonLocation = rc.getLocation();
        Communication.setArchonBlock();
    }

    private static void initDroid() {
        for (RobotInfo ri : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (ri.getType() == RobotType.ARCHON) {
                archonId = ri.getID();
                archonLocation = ri.getLocation();
            }
        }
    }

    private static void initBuilding() {

    }
}
