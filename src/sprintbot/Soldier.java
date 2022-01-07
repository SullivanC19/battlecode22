package sprintbot;

import battlecode.common.*;

public class Soldier {
    public static void run() throws GameActionException {
        Communication.updateLead();

        // Try to attack someone
        int radius = Memory.rc.getType().actionRadiusSquared;
        Team opponent = Memory.rc.getTeam().opponent();
        RobotInfo[] enemies = Memory.rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (Memory.rc.canAttack(toAttack)) {
                Memory.rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = Utils.randomDirection();
        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
        }
    }
}