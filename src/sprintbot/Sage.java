package sprintbot;

import battlecode.common.*;

public class Sage {
    public static void run(RobotController rc) throws GameActionException {
        RobotInfo closestEnemyRobot = Utils.getClosestEnemyRobot(rc);

        Direction dir = Utils.randomDirection();
        if (closestEnemyRobot != null) {
            dir = rc.getLocation().directionTo(closestEnemyRobot.getLocation());
        }

        if (rc.canMove(dir)) {
            rc.move(dir);
        }

        if (rc.getActionCooldownTurns() > 0) return;

        RobotInfo[] enemyRobotsInActionRange =
                rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());

        if (enemyRobotsInActionRange.length == 0) return;

        int damage = rc.getType().getDamage(rc.getLevel());

        int aoeDroidDamage = 0;
        int aoeBuildingDamage = 0;
        for (RobotInfo ri : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent())) {
            int dmg = ri.getType().getMaxHealth(ri.getLevel()) / 10;
            if (Utils.isDroidType(ri.getType())) {
                aoeDroidDamage += dmg;
            } else {
                aoeBuildingDamage += dmg;
            }
        }

        if (damage > aoeDroidDamage && damage > aoeBuildingDamage) {
            rc.attack(enemyRobotsInActionRange[0].getLocation());
        } else if (aoeDroidDamage > aoeBuildingDamage) {
            rc.envision(AnomalyType.CHARGE);
        } else {
            rc.envision(AnomalyType.FURY);
        }
    }
}