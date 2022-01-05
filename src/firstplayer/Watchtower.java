package firstplayer;

import battlecode.common.*;

public class Watchtower {
    public static void run(RobotController rc) throws GameActionException {
        int range = rc.getType().actionRadiusSquared;
        RobotInfo[] enemies = rc.senseNearbyRobots(range, rc.getTeam().opponent());
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
    }
}