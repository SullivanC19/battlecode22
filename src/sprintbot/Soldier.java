package sprintbot;

import battlecode.common.*;

public class Soldier {
    public static void run() throws GameActionException {
        RobotInfo[] enemyRobots = Memory.rc.senseNearbyRobots(
                RobotType.SOLDIER.visionRadiusSquared, Memory.rc.getTeam().opponent());

        if (enemyRobots.length > 0) {
            micro(enemyRobots);

            Communication.addBlockInfo(Communication.BLOCK_TYPE_THREAT, Utils.getMyBlock());
            Communication.updateDroidCount();

            return;
        }

        Communication.addBlockInfo(Communication.BLOCK_TYPE_NO_ENEMY, Utils.getMyBlock());

        MapLocation myBlock = Utils.getMyBlock();
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

        if (closestEnemyBlock != null) {
            Pathfinder.moveToward(Utils.getCenterOfBlock(closestEnemyBlock));
        } else {
            Pathfinder.exploreEnemyArchons();
        }



//        Memory.update();
//
//        Communication.updateLead();
//        Communication.updateDroidCountAndArchonIdx();
//
//        // Try to attack someone
//        int radius = Memory.rc.getType().actionRadiusSquared;
//        Team opponent = Memory.rc.getTeam().opponent();
//        RobotInfo[] enemies = Memory.rc.senseNearbyRobots(radius, opponent);
//        if (enemies.length > 0) {
//            RobotInfo target = enemies[0];
//            for(int i = enemies.length - 1; i >= 0; i--){
//                if(enemies[i].type == RobotType.ARCHON){
//                    target = enemies[i];
//                }
//            }
//            MapLocation toAttack = target.location;
//            if (Memory.rc.canAttack(toAttack)) {
//                Memory.rc.attack(toAttack);
//            }
//            else if(Memory.rc.canMove(Memory.rc.getLocation().directionTo(toAttack))){
//                Memory.rc.move(Memory.rc.getLocation().directionTo(toAttack));
//            }
//        }
//
//        Pathfinder.exploreEnemyArchons();

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
            int distToThreatDroid = 400;
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
                        distToThreatDroid = Math.min(distToThreatDroid, dist);
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
            int safeOrInGroupScore = (potentialDamageTaken == 0) ? 100 : (friendlyRobotsAdjacent ? 50 : distToNonthreatScore);
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