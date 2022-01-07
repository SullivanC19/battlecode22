package sprintbot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.List;
import java.util.Map;

public class Miner {
    private static void moveInRange(RobotController rc, MapLocation square) throws GameActionException {
        if(!rc.getLocation().isAdjacentTo(square) && rc.canMove(rc.getLocation().directionTo(square))) {
            rc.move(rc.getLocation().directionTo(square));
        }
    }
    public static void run(RobotController rc) throws GameActionException {
        Communication.updateFood(rc);

        // move toward closest visible lead location if one exists
        MapLocation[] leadLocations = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
        if (leadLocations.length != 0) {
            MapLocation closestLeadSquare = null;
            for (MapLocation leadSquare : leadLocations) {
                if (closestLeadSquare == null ||
                        rc.getLocation().distanceSquaredTo(leadSquare) < rc.getLocation().distanceSquaredTo(closestLeadSquare)) {
                    closestLeadSquare = leadSquare;
                }
            }

            Direction dir = rc.getLocation().directionTo(closestLeadSquare);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }

            if (rc.canMineLead(closestLeadSquare)) {
                rc.mineLead(closestLeadSquare);
            }

            return;
        }

        // move toward closest food block if one exists
        List<MapLocation> foodBlocks = Communication.getAllFoodBlocks(rc);
        if (!foodBlocks.isEmpty()) {
            MapLocation loc = rc.getLocation();
            MapLocation block = new MapLocation(loc.x / Communication.BLOCK_SIZE, loc.y / Communication.BLOCK_SIZE);
            int closestDist = Integer.MAX_VALUE;
            for (MapLocation foodBlock : foodBlocks) {
                closestDist = Math.min(closestDist, block.distanceSquaredTo(foodBlock));
            }

            int cnt = 0;
            MapLocation targetBlock = null;
            for (MapLocation foodBlock : foodBlocks) {
                int dist = block.distanceSquaredTo(foodBlock);
                if (dist + 5 >= closestDist) {
                    cnt++;
                    if (Utils.rng.nextInt(cnt) == 0) {
                        targetBlock = foodBlock;
                    }
                }
            }

            MapLocation targetLoc =
                    new MapLocation((2 * targetBlock.x + 1) * Communication.BLOCK_SIZE / 2,
                                    (2 * targetBlock.y + 1) * Communication.BLOCK_SIZE / 2);
            if (rc.canMove(loc.directionTo(targetLoc))) {
                rc.move(loc.directionTo(targetLoc));
            }
        }

        // explore
        Direction randomDir = Utils.randomDirection();
        if (rc.canMove(randomDir)) {
            rc.move(randomDir);
        }

    }



}