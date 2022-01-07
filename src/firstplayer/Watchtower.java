package firstplayer;

import battlecode.common.*;

public class Watchtower {
    public static void run(RobotController rc) throws GameActionException {
        RobotInfo closestEnemyRobot = Utils.getClosestEnemyRobot(rc);
        if (closestEnemyRobot != null && rc.canAttack(closestEnemyRobot.getLocation())) {
            rc.attack(closestEnemyRobot.getLocation());
        }
    }
}