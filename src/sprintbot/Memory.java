package sprintbot;

import battlecode.common.*;

import java.util.Random;

public class Memory {
    public static RobotController rc;

    public static int archonId;
    public static MapLocation archonLocation;

    public static MapLocation myBlock;

    public static MapLocation[] leadList;
    public static int numLeadInBlock;
    public static boolean blockIsMinable;

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

    // TODO: remove robots list and lead list updates from here
    public static void update() throws GameActionException {
        myBlock = Utils.getMyBlock();
        leadList = rc.senseNearbyLocationsWithLead();
//        robotsList = rc.senseNearbyRobots();

        numLeadInBlock = 0;
        blockIsMinable = false;
        for (int i = 0; i < leadList.length; i++) {
            int lead = rc.senseLead(leadList[i]);
            if (Utils.getBlock(leadList[i]).equals(myBlock)) {
                numLeadInBlock += lead;
                blockIsMinable |= lead >= 10;
            }
        }

        rc.setIndicatorString("" + numLeadInBlock);

//        numMinersInBlock = 0;
//        for (int i = 0; i < robotsList.length; i++) {
//            if (robotsList[i].getType() != RobotType.MINER || robotsList[i].getTeam() != rc.getTeam()) continue;
//            numMinersInBlock += Utils.getBlock(robotsList[i].getLocation()).equals(myBlock) ? 1 : 0;
//        }
    }
}
