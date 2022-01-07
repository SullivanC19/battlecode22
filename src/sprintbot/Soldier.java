package sprintbot;

import battlecode.common.*;

public class Soldier {
    public static void run(RobotController rc) throws GameActionException {
        Communication.updateFood(rc);

        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = Utils.randomDirection();
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}