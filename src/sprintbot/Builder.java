package sprintbot;

import battlecode.common.*;

public class Builder {

    public static void run() throws GameActionException {
        Communication.updateLead();
        Communication.updateDroidCount();

        // find closest friendly repairable building
        MapLocation closestRepairLocation = null;
        int closestRepairDist = Integer.MAX_VALUE;
        for (RobotInfo ri : Memory.rc.senseNearbyRobots()) {
            if (ri.getTeam() == Memory.rc.getTeam()
            && ri.getHealth() < ri.getType().getMaxHealth(ri.getLevel() )
            && Utils.isBuildingType(ri.getType())
            && ri.getLocation().distanceSquaredTo(Memory.rc.getLocation()) < closestRepairDist) {
                closestRepairLocation = ri.getLocation();
                closestRepairDist = closestRepairLocation.distanceSquaredTo(Memory.rc.getLocation());
            }
        }

        // move toward closest friendly building or move randomly
        Direction dir = Utils.randomDirection();
        if (closestRepairLocation != null) {
            dir = Memory.rc.getLocation().directionTo(closestRepairLocation);
        }

        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
        }

        // if possible, repair closest friendly building
        while (closestRepairLocation != null && Memory.rc.canRepair(closestRepairLocation)) {
            Memory.rc.repair(closestRepairLocation);
        }

        // build watchtowers sometimes randomly
        if (Memory.rc.senseNearbyRobots(4, Memory.rc.getTeam()).length == 0) {
            Utils.tryBuild(RobotType.WATCHTOWER);
        }
    }

}
