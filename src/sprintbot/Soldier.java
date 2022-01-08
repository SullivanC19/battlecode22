package sprintbot;

import battlecode.common.*;

import java.nio.file.Path;

public class Soldier {
    public static void run() throws GameActionException {
        Communication.updateLead();
        Communication.updateDroidCount();

        // Try to attack someone
        int radius = Memory.rc.getType().actionRadiusSquared;
        Team opponent = Memory.rc.getTeam().opponent();
        RobotInfo[] enemies = Memory.rc.senseNearbyRobots(radius, opponent);
        RobotInfo target = enemies[0]
        if (enemies.length > 0) {
            for(int i = enemies.length - 1; i >= 0; i--){
                if(enemies[i].type == RobotType.ARCHON){
                    target = enemies[i]
                }
            }
            MapLocation toAttack = target.location;
            if (Memory.rc.canAttack(toAttack)) {
                Memory.rc.attack(toAttack);
            }
            else if(Memory.rc.canMove(Memory.rc.getLocation().directionTo(toAttack))){
                Memory.rc.move(Memory.rc.getLocation().directionTo(toAttack));
            }
        }

        Pathfinder.explore();
    }
}