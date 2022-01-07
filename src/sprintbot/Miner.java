package sprintbot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.List;
import java.util.Map;

public class Miner {
    public static void run() throws GameActionException {
        Communication.updateLead();
        Communication.updateDroidCount();

        // move toward closest visible lead location if one exists
        int visionRadiusSquared = Memory.rc.getType().visionRadiusSquared;
        MapLocation[] leadLocations = Memory.rc.senseNearbyLocationsWithLead(visionRadiusSquared);
        if (leadLocations.length != 0) {
            MapLocation closestLeadSquare = null;
            for (MapLocation leadSquare : leadLocations) {
                if (closestLeadSquare == null ||
                        Memory.rc.getLocation().distanceSquaredTo(leadSquare)
                                < Memory.rc.getLocation().distanceSquaredTo(closestLeadSquare)) {
                    closestLeadSquare = leadSquare;
                }
            }

            Direction dir = Memory.rc.getLocation().directionTo(closestLeadSquare);
            if (Memory.rc.canMove(dir)) {
                Memory.rc.move(dir);
            }

            if (Memory.rc.canMineLead(closestLeadSquare)) {
                Memory.rc.mineLead(closestLeadSquare);
            }

            return;
        }

        // move toward closest lead block if one exists
        List<MapLocation> leadBlocks = Communication.getAllLeadBlocks();
        if (!leadBlocks.isEmpty()) {
            MapLocation loc = Memory.rc.getLocation();
            MapLocation block = new MapLocation(loc.x / Utils.LEAD_BLOCK_SIZE, loc.y / Utils.LEAD_BLOCK_SIZE);
            int closestDist = Integer.MAX_VALUE;
            for (MapLocation leadBlock : leadBlocks) {
                closestDist = Math.min(closestDist, block.distanceSquaredTo(leadBlock));
            }

            int cnt = 0;
            MapLocation targetBlock = null;
            for (MapLocation leadBlock : leadBlocks) {
                int dist = block.distanceSquaredTo(leadBlock);
                if (dist + 5 >= closestDist) {
                    cnt++;
                    if (Utils.rng.nextInt(cnt) == 0) {
                        targetBlock = leadBlock;
                    }
                }
            }

            MapLocation targetLoc =
                    new MapLocation((2 * targetBlock.x + 1) * Utils.LEAD_BLOCK_SIZE / 2,
                                    (2 * targetBlock.y + 1) * Utils.LEAD_BLOCK_SIZE / 2);
            if (Memory.rc.canMove(loc.directionTo(targetLoc))) {
                Memory.rc.move(loc.directionTo(targetLoc));
            }
        }

        // explore
        Direction randomDir = Utils.randomDirection();
        if (Memory.rc.canMove(randomDir)) {
            Memory.rc.move(randomDir);
        }

    }

}