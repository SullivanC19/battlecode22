package firstplayer;

import battlecode.common.*;

public class Miner {
    private static void moveInRange(RobotController rc, MapLocation square) throws GameActionException {
        if(!rc.getLocation().isAdjacentTo(square) && rc.canMove(rc.getLocation().directionTo(square))) {
            rc.move(rc.getLocation().directionTo(square));
        }
    }
    public static void run(RobotController rc) throws GameActionException {
        MapLocation[] leadSquares = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
        if (leadSquares.length == 0) {
            Direction dir = Utils.randomDirection();
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        } else {
            MapLocation closestLeadSquare = null;
            for (MapLocation leadSquare : leadSquares) {
                if (closestLeadSquare == null ||
                        rc.getLocation().distanceSquaredTo(leadSquare) < rc.getLocation().distanceSquaredTo(closestLeadSquare)) {
                    closestLeadSquare = leadSquare;
                }
            }

            if (rc.canMineLead(closestLeadSquare)) {
                rc.mineLead(closestLeadSquare);
                Direction dir = Utils.randomDirection();
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }

            Direction dir = rc.getLocation().directionTo(closestLeadSquare);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }
}