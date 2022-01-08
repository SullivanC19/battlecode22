package sprintbot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import java.util.List;

public class Miner {
    private static MapLocation targetLeadBlock;

    public static void run() throws GameActionException {
        Communication.updateLead();
        Communication.updateDroidCount();

        // find closest visible non-crowded mining location if one exists
        MapLocation targetMiningLocation = null;
        int closestDist = Integer.MAX_VALUE;
        MapLocation[] nearbyLeadLocations =
                Memory.rc.senseNearbyLocationsWithLead(Memory.rc.getType().visionRadiusSquared);
        for (MapLocation loc : nearbyLeadLocations) {
            int dist = Memory.rc.getLocation().distanceSquaredTo(loc);
            if (dist < closestDist) {
                closestDist = dist;
                targetMiningLocation = loc;
            }
        }

        // if on target, switch target to the least crowded adjacent cell
        if (targetMiningLocation != null && Memory.rc.getLocation().equals(targetMiningLocation)) {
            MapLocation newTargetLoc = targetMiningLocation;
            int lowestSum = 0;
            for (Direction d2 : Direction.cardinalDirections()) {
                MapLocation adjLoc = Memory.rc.getLocation().add(d2);
                lowestSum += Memory.rc.canSenseRobotAtLocation(adjLoc) ? 1 : 0;
            }
            for (Direction d1 : Direction.allDirections()) {
                MapLocation loc = Memory.rc.getLocation().add(d1);
                if (Memory.rc.canSenseRobotAtLocation(loc)) continue;
                int sum = 0;
                boolean canMine = false;
                for (Direction d2 : Direction.cardinalDirections()) {
                    MapLocation adjLoc = loc.add(d2);
                    sum += Memory.rc.canSenseRobotAtLocation(adjLoc) ? 1 : 0;
                    canMine |= Memory.rc.canSenseLocation(adjLoc) && Memory.rc.senseLead(adjLoc) > 0;
                }

                if (canMine && sum < lowestSum) {
                    newTargetLoc = loc;
                    lowestSum = sum;
                }
            }

            targetMiningLocation = newTargetLoc;
        }

        // move toward mining location
        if (targetMiningLocation != null) {
            Pathfinder.moveToward(targetMiningLocation);
            targetLeadBlock = null;
        } else {
            MapLocation myLoc = Memory.rc.getLocation();
            if (targetLeadBlock != null
                    && myLoc.x / Utils.LEAD_BLOCK_SIZE == targetLeadBlock.x
                    && myLoc.y / Utils.LEAD_BLOCK_SIZE == targetLeadBlock.y) {
                targetLeadBlock = null;
            }

            // find a new target lead block from comms
            if (targetLeadBlock == null) {
                List<MapLocation> leadBlocks = Communication.getAllLeadBlocks();

                if (!leadBlocks.isEmpty()) {
                    MapLocation loc = Memory.rc.getLocation();
                    MapLocation block = new MapLocation(loc.x / Utils.LEAD_BLOCK_SIZE, loc.y / Utils.LEAD_BLOCK_SIZE);
                    closestDist = Integer.MAX_VALUE;
                    for (MapLocation leadBlock : leadBlocks) {
                        int dist = block.distanceSquaredTo(leadBlock);
                        if (dist < closestDist) {
                            closestDist = dist;
                            targetLeadBlock = leadBlock;
                        }
                    }
                }
            }

            // if a target isn't found, then explore
            if (targetLeadBlock == null) {
                Pathfinder.explore();
            } else {
                MapLocation targetLoc =
                        new MapLocation((2 * targetLeadBlock.x + 1) * Utils.LEAD_BLOCK_SIZE / 2,
                                (2 * targetLeadBlock.y + 1) * Utils.LEAD_BLOCK_SIZE / 2);

                Pathfinder.moveToward(targetLoc);
            }
        }

        // mine
        for (Direction dir : Direction.values()) {
            MapLocation loc = Memory.rc.getLocation().add(dir);
            while (Memory.rc.canMineLead(loc)) {
                Memory.rc.mineLead(loc);
            }
        }

    }

}