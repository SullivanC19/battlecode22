package qualificationbot_2_beta;

import battlecode.common.GameActionException;

public class Sage {
    public static void run() throws GameActionException {
//        Memory.update();
//
//        Communication.updateLead();
//        Communication.updateDroidCountAndArchonIdx();
//
//        RobotInfo closestEnemyRobot = Utils.getClosestEnemyRobot();
//
//        Direction dir = Utils.randomDirection();
//        if (closestEnemyRobot != null) {
//            dir = Memory.rc.getLocation().directionTo(closestEnemyRobot.getLocation());
//        }
//
//        if (Memory.rc.canMove(dir)) {
//            Memory.rc.move(dir);
//        }
//
//        if (Memory.rc.getActionCooldownTurns() > 0) return;
//
//        RobotInfo[] enemyRobotsInActionRange =
//                Memory.rc.senseNearbyRobots(Memory.rc.getType().actionRadiusSquared,
//                        Memory.rc.getTeam().opponent());
//
//        if (enemyRobotsInActionRange.length == 0) return;
//
//        int damage = Memory.rc.getType().getDamage(Memory.rc.getLevel());
//
//        int aoeDroidDamage = 0;
//        int aoeBuildingDamage = 0;
//
//        RobotInfo[] nearbyRobots =
//                Memory.rc.senseNearbyRobots(Memory.rc.getType().actionRadiusSquared,
//                        Memory.rc.getTeam().opponent());
//
//        for (RobotInfo ri : nearbyRobots) {
//            int dmg = ri.getType().getMaxHealth(ri.getLevel()) / 10;
//            if (Utils.isDroidType(ri.getType())) {
//                aoeDroidDamage += dmg;
//            } else {
//                aoeBuildingDamage += dmg;
//            }
//        }
//
//        if (damage > aoeDroidDamage && damage > aoeBuildingDamage) {
//            Memory.rc.attack(enemyRobotsInActionRange[0].getLocation());
//        } else if (aoeDroidDamage > aoeBuildingDamage) {
//            Memory.rc.envision(AnomalyType.CHARGE);
//        } else {
//            Memory.rc.envision(AnomalyType.FURY);
//        }
    }
}