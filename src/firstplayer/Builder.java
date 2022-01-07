package firstplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

public class Builder {

    public static void run(RobotController rc) throws GameActionException {
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

        // build buildings sometimes randomly
        if (Utils.rng.nextInt() % 100 == 0 && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.LABORATORY.buildCostLead) {
            RobotType buildingType = Utils.buildableTypes[Utils.rng.nextInt(Utils.buildableTypes.length)];
            Utils.tryBuild(buildingType, rc);
        }
    }

}
