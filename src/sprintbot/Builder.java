package sprintbot;

import battlecode.common.*;

public class Builder {

    public static void run(RobotController rc) throws GameActionException {
        Communication.updateFood(rc);

        // find closest friendly repairable building
        MapLocation closestRepairLocation = null;
        int closestRepairDist = Integer.MAX_VALUE;
        for (RobotInfo ri : rc.senseNearbyRobots()) {
            if (ri.getTeam() == rc.getTeam()
            && ri.getHealth() < ri.getType().getMaxHealth(ri.getLevel() )
            && Utils.isBuildingType(ri.getType())
            && ri.getLocation().distanceSquaredTo(rc.getLocation()) < closestRepairDist) {
                closestRepairLocation = ri.getLocation();
                closestRepairDist = closestRepairLocation.distanceSquaredTo(rc.getLocation());
            }
        }

        // move toward closest friendly building or move randomly
        Direction dir = Utils.randomDirection();
        if (closestRepairLocation != null) {
            dir = rc.getLocation().directionTo(closestRepairLocation);
        }

        if (rc.canMove(dir)) {
            rc.move(dir);
        }

        // if possible, repair closest friendly building
        while (closestRepairLocation != null && rc.canRepair(closestRepairLocation)) {
            rc.repair(closestRepairLocation);
        }

        // build watchtowers sometimes randomly
        if (rc.senseNearbyRobots(8, rc.getTeam()).length == 0) {
            Utils.tryBuild(RobotType.WATCHTOWER, rc);
        }
    }

}
