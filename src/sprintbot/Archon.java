package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon {
    public static void run(RobotController rc) throws GameActionException {
        Communication.updateFood(rc);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (Communication.isFood(x, y, rc)) {
                    for (int r = 0; r < 4; r++) for (int c = 0; c < 4; c++) rc.setIndicatorDot(new MapLocation(x*4+r, y*4+c), 0, 255, 0);
                }
            }
        }

        if (rc.getRoundNum() < 20) {
            Utils.tryBuild(RobotType.MINER, rc);
        } else if (rc.getRoundNum() < 100 && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
            if (Utils.rng.nextInt(3) == 0) {
                Utils.tryBuild(RobotType.SOLDIER, rc);
            } else {
                Utils.tryBuild(RobotType.MINER, rc);
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.WATCHTOWER.buildCostLead) {
            Utils.tryBuild(Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)], rc);
        }
    }
}