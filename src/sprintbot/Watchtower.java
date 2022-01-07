package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Watchtower {
    public static void run(RobotController rc) throws GameActionException {
        Communication.updateFood(rc);

        RobotInfo closestEnemyRobot = Utils.getClosestEnemyRobot(rc);
        if (closestEnemyRobot != null && rc.canAttack(closestEnemyRobot.getLocation())) {
            rc.attack(closestEnemyRobot.getLocation());
        }
    }
}