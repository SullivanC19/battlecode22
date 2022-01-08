package sprintbot;

import battlecode.common.*;

import java.util.List;

public class Builder {
    private static MapLocation targetLeadBlock;

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

        MapLocation archonBlock = new MapLocation(
                Memory.archonLocation.x / Utils.LEAD_BLOCK_SIZE,
                Memory.archonLocation.y / Utils.LEAD_BLOCK_SIZE);

        // find closest lead block
        if (targetLeadBlock == null) {
            List<MapLocation> leadBlocks = Communication.getAllLeadBlocks();
            if (!leadBlocks.isEmpty()) {
                MapLocation loc = Memory.rc.getLocation();
                MapLocation block = new MapLocation(loc.x / Utils.LEAD_BLOCK_SIZE, loc.y / Utils.LEAD_BLOCK_SIZE);
                int closestDist = Integer.MAX_VALUE;
                for (MapLocation leadBlock : leadBlocks) {
                    if (leadBlock.equals(archonBlock)) continue;
                    int dist = block.distanceSquaredTo(leadBlock);
                    if (dist < closestDist) {
                        closestDist = dist;
                        targetLeadBlock = leadBlock;
                    }
                }
            }
        }

        // move toward closest friendly building or explore
        if (closestRepairLocation != null) {
            Pathfinder.moveToward(closestRepairLocation);
        } else if (targetLeadBlock != null) {
            MapLocation targetLoc =
                    new MapLocation((2 * targetLeadBlock.x + 1) * Utils.LEAD_BLOCK_SIZE / 2,
                            (2 * targetLeadBlock.y + 1) * Utils.LEAD_BLOCK_SIZE / 2);

            Pathfinder.moveToward(targetLoc);
        } else {
            Pathfinder.explore();
        }

        // if possible, repair closest friendly building
        if (closestRepairLocation != null && Memory.rc.canRepair(closestRepairLocation)) {
            Memory.rc.repair(closestRepairLocation);
        }

        // build watchtower
        int blockX = Memory.rc.getLocation().x / Utils.LEAD_BLOCK_SIZE;
        int blockY = Memory.rc.getLocation().y / Utils.LEAD_BLOCK_SIZE;
        if (targetLeadBlock != null && blockX == targetLeadBlock.x && blockY == targetLeadBlock.y) {
            boolean shouldPlaceWatchtower = Communication.isLead(blockX, blockY);
            for (int x = 1; x <= 2; x++) {
                for (int y = 1; y <= 2; y++) {
                    MapLocation loc = new MapLocation(blockX * Utils.LEAD_BLOCK_SIZE + x,blockY * Utils.LEAD_BLOCK_SIZE + y);
                    RobotInfo ri = Memory.rc.senseRobotAtLocation(loc);
                    shouldPlaceWatchtower &= ri == null
                            || ri.getTeam() != Memory.rc.getTeam()
                            || ri.getType() != RobotType.WATCHTOWER;
                }
            }

            if (!shouldPlaceWatchtower) {
                targetLeadBlock = null;
                return;
            }

            Direction buildDir = null;
            for (Direction dir : Direction.allDirections()) {
                MapLocation adjLoc = Memory.rc.getLocation().add(dir);
                int modX = adjLoc.x % Utils.LEAD_BLOCK_SIZE;
                int modY = adjLoc.y % Utils.LEAD_BLOCK_SIZE;
                if (!Memory.rc.canSenseRobotAtLocation(adjLoc)
                        && (modX == 1 || modX == 2)
                        && (modX == modY)) {
                    buildDir = dir;
                    break;
                }
            }

            if (buildDir != null && Memory.rc.canBuildRobot(RobotType.WATCHTOWER, buildDir)) {
                Memory.rc.buildRobot(RobotType.WATCHTOWER, buildDir);
                targetLeadBlock = null;
            }
        }
    }

}
