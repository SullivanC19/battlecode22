package sprintbot;

import battlecode.common.*;

public class Soldier {
    public static void run() throws GameActionException {
        MapLocation myLoc = Memory.rc.getLocation();
        RobotInfo[] enemyRobots = Memory.rc.senseNearbyRobots(
                RobotType.SOLDIER.visionRadiusSquared, Memory.rc.getTeam().opponent());

        if (enemyRobots.length > 0) {
            // priorities for micro attack
            // - low rubble
            // - in range to attack someone (who?)
            // - close to non-threatening units (miner, builder, archon, laboratory)
            // - end up in a square safe from attack or tanking for a soldier behind

            // as moving toward non-threatening units want to prioritize rubble

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

            Direction bestDir = null;
            RobotInfo bestTarget = null;
            int bestDistToNonthreatDroid = Integer.MAX_VALUE;
            int rubble = Integer.MAX_VALUE;
            boolean isSafe = false;

            for (Direction dir : Direction.allDirections()) {
                MapLocation adjLoc = Memory.rc.adjacentLocation(dir);
                RobotInfo target = firstTarget;
                int distToNonthreatDroid = Integer.MAX_VALUE;
                boolean inEnemyActionRange = false;

                for (RobotInfo ri : enemyRobots) {
                    int dist = myLoc.distanceSquaredTo(ri.getLocation());
                    switch (ri.getType()) {
                        case MINER:
                        case BUILDER:
                            distToNonthreatDroid = Math.min(distToNonthreatDroid, dist);
                            break;
                        case SOLDIER:
                        case SAGE:
                        case WATCHTOWER:
                            inEnemyActionRange |= dist <= ri.getType().actionRadiusSquared;
                            break;
                    }
                }

                RobotInfo[] eriar = Memory.rc.senseNearbyRobots(
                        adjLoc, RobotType.SOLDIER.actionRadiusSquared, Memory.rc.getTeam().opponent());


            }

            MapLocation myLoc = Memory.rc.getLocation();
            int closestDist = Integer.MAX_VALUE;
            for (int i = 0; i < enemyRobots.length; i++) {
                closestDist = Math.min(closestDist, myLoc.distanceSquaredTo(enemyRobots[i].getLocation()));
            }

            RobotInfo closestEnemy = null;
            for (int i = 0; i < enemyRobots.length; i++) {
                if (closestDist == myLoc.distanceSquaredTo(enemyRobots[i].getLocation())) {
                    closestEnemy = enemyRobots[i];
                    break;
                }
            }

            MapLocation enemyLoc = closestEnemy.getLocation();
            if (closestDist > RobotType.SOLDIER.actionRadiusSquared) {
                Pathfinder.moveToward(enemyLoc);
                if (Memory.rc.canAttack(enemyLoc)) {
                    Memory.rc.attack(enemyLoc);
                }
            } else {
                if (Memory.rc.canAttack(enemyLoc)) {
                    Memory.rc.attack(enemyLoc);
                }
                if (Memory.rc.canMove(enemyLoc.directionTo(myLoc))) {
                    Memory.rc.move(enemyLoc.directionTo(myLoc));
                }
            }

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
}