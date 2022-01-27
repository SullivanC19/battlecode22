package qualificationbot_2;

import battlecode.common.*;

public class Builder {
    private static MapLocation targetLeadBlock;

    public static void run() throws GameActionException {
        Communication.updateDroidCount();
        Communication.updateArchonIdx();

        if (Communication.getLaboratoryCount() < 1) {
            buildLaboratory();
        }

        for (RobotInfo ri : Memory.rc.senseNearbyRobots(RobotType.BUILDER.visionRadiusSquared, Memory.rc.getTeam())) {
            if (ri.getType().isBuilding() && ri.getHealth() < ri.getType().getMaxHealth(ri.getLevel())) {
                Pathfinder.moveTo(ri.getLocation());
                if (Memory.rc.canRepair(ri.getLocation())) Memory.rc.repair(ri.getLocation());
                break;
            }
        }
    }

    public static void buildLaboratory() throws GameActionException {
        int mapWidth = Memory.rc.getMapWidth();
        int mapHeight = Memory.rc.getMapWidth();

        MapLocation myLoc = Memory.rc.getLocation();

        MapLocation[] allLocs = Memory.rc.getAllLocationsWithinRadiusSquared(Memory.rc.getLocation(), 8);

        MapLocation targetLoc = null;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < allLocs.length; i++) {
            if (Memory.rc.isLocationOccupied(allLocs[i])) continue;

            int rubble = Memory.rc.senseRubble(allLocs[i]);
            int distToEdge = Math.min(allLocs[i].x, Math.min(allLocs[i].y, Math.min(mapWidth - allLocs[i].x - 1, mapHeight - allLocs[i].y - 1)));
            int distToBuilder = myLoc.distanceSquaredTo(allLocs[i]);

            int score = -rubble * 10 - distToEdge * 5 - distToBuilder;
            if (score > bestScore) {
                bestScore = score;
                targetLoc = allLocs[i];
            }
        }

        Memory.rc.setIndicatorLine(myLoc, targetLoc, 0, 0, 255);

        if (targetLoc != null && Memory.rc.canBuildRobot(RobotType.LABORATORY, myLoc.directionTo(targetLoc))) {
            Memory.rc.buildRobot(RobotType.LABORATORY, myLoc.directionTo(targetLoc));
            Communication.incrementLaboratoryCount();
        }

        if (targetLoc != null && !Memory.rc.getLocation().isAdjacentTo(targetLoc)) Pathfinder.moveTo(targetLoc);
    }

}
