package qualificationbot_0;

import battlecode.common.*;

public class Soldier {
    public static void run() throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();

        RobotInfo[] enemyRobots = Memory.rc.senseNearbyRobots(
                RobotType.SOLDIER.visionRadiusSquared, Memory.rc.getTeam().opponent());

        if (enemyRobots.length > 0) {
            micro(enemyRobots);

            Communication.addBlockInfo(Communication.BLOCK_TYPE_THREAT, myBlock);
            Communication.updateDroidCount();

            return;
        }

        MapLocation[] enemyBlocks = Communication.getAllTargetBlocks(false);

        int closestEnemyBlockDist = Integer.MAX_VALUE;
        for (MapLocation enemyBlock : enemyBlocks) {
            if (enemyBlock == null) continue;
            int dist = myBlock.distanceSquaredTo(enemyBlock);
            closestEnemyBlockDist = Math.min(closestEnemyBlockDist, dist);
        }

        MapLocation closestEnemyBlock = null;
        for (MapLocation enemyBlock : enemyBlocks) {
            if (enemyBlock == null) continue;
            if (myBlock.distanceSquaredTo(enemyBlock) == closestEnemyBlockDist) {
                closestEnemyBlock = enemyBlock;
                break;
            }
        }



        if (closestEnemyBlock != null && closestEnemyBlock.equals(myBlock)) {
            Communication.addBlockInfo(Communication.BLOCK_TYPE_NO_ENEMY, myBlock);
        }

        if (closestEnemyBlock != null) {
            Pathfinder.moveTo(Utils.getCenterOfBlock(closestEnemyBlock));
        } else {
            Pathfinder.exploreEnemyArchons();
        }

        Communication.updateDroidCount();
    }

    // priorities for micro attack
    // - in range to attack someone (who?)
    // - low rubble
    // - close to non-threatening units (miner, builder, archon, laboratory)
    // - end up in a square safe from attack or tanking for a soldier behind
    private static void micro(RobotInfo[] enemyRobots) throws GameActionException {
        MapLocation myLoc = Memory.rc.getLocation();

        // ignore safety if there are friendly robots adjacent
        boolean friendlyRobotsAdjacent = Memory.rc.senseNearbyRobots(2, Memory.rc.getTeam()).length > 0;

        // find the highest ordinal enemy in action radius if one exists
        RobotInfo firstTarget = null;
        RobotInfo[] enemyRobotsInActionRadius = Memory.rc.senseNearbyRobots(
                RobotType.SOLDIER.actionRadiusSquared, Memory.rc.getTeam().opponent());
        for (RobotInfo ri : enemyRobotsInActionRadius) {
            if (firstTarget == null || ri.getType().ordinal() >= firstTarget.getType().ordinal()) {
                firstTarget = ri;
            }
        }

        int bestScore = Integer.MIN_VALUE;
        RobotInfo bestTarget = firstTarget;
        Direction bestDir = null;

        // pick best of all adjacent locations
        for (Direction dir : Direction.allDirections()) {
            MapLocation adjLoc = Memory.rc.adjacentLocation(dir);
            if (dir != Direction.CENTER && (!Memory.rc.canSenseLocation(adjLoc) || Memory.rc.isLocationOccupied(adjLoc))) continue;

            RobotInfo target = firstTarget;
            int targetOrdinal = firstTarget == null ? -1 : firstTarget.getType().ordinal();
            int distToNonthreatDroid = 400;
            int potentialDamageTaken = 0;
            int rubble = Memory.rc.senseRubble(adjLoc);

            // find closest non-threatening droid to loc and check if loc is out of range of enemy units
            for (RobotInfo ri : enemyRobots) {
                int dist = adjLoc.distanceSquaredTo(ri.getLocation());
                switch (ri.getType()) {
                    case MINER:
                    case BUILDER:
                    case ARCHON:
                        distToNonthreatDroid = Math.min(distToNonthreatDroid, dist);
                        break;
                    case SOLDIER:
                    case SAGE:
                    case WATCHTOWER:
                        potentialDamageTaken += dist <= ri.getType().actionRadiusSquared ? ri.getType().damage : 0;
                        break;
                }
            }

            // choose highest ordinal target from those in range
            RobotInfo[] eriar = Memory.rc.senseNearbyRobots(
                    adjLoc, RobotType.SOLDIER.actionRadiusSquared, Memory.rc.getTeam().opponent());
            for (RobotInfo ri : eriar) {
                if (ri.getType().ordinal() >= targetOrdinal) {
                    target = ri;
                    targetOrdinal = ri.getType().ordinal();
                }
            }

            int targetScore = (firstTarget == null && target != null) ? 30 + targetOrdinal : 0;
            int rubbleScore = -rubble;
            int distToNonthreatScore = -distToNonthreatDroid / 5;
            int safeOrInGroupScore = (friendlyRobotsAdjacent || potentialDamageTaken == 0) ? 100 : 0;
            int potentialDamageScore = -potentialDamageTaken / 3;

            int score = targetScore + rubbleScore + distToNonthreatScore + safeOrInGroupScore + potentialDamageScore;
            if (score > bestScore) {
                bestScore = score;
                bestDir = dir;

                if (bestTarget == null || (targetOrdinal > bestTarget.getType().ordinal())) {
                    bestTarget = target;
                }
            }
        }

        if (bestTarget != null && Memory.rc.canAttack(bestTarget.getLocation())) {
            Memory.rc.attack(bestTarget.getLocation());
        }

        if (bestDir != null && Memory.rc.canMove(bestDir)) {
            Memory.rc.move(bestDir);
        }

        if (bestTarget != null && Memory.rc.canAttack(bestTarget.getLocation())) {
            Memory.rc.attack(bestTarget.getLocation());
        }
    }
}