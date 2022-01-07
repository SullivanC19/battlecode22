package firstplayer;

import battlecode.common.*;

public class Archon {

    public static void run(RobotController rc) throws GameActionException {
        if (rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
            Utils.tryBuild(RobotType.SAGE, rc);
        } else if (rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.MINER.buildCostLead) {
            if (Utils.rng.nextInt(3) < 2) {
                Utils.tryBuild(RobotType.MINER, rc);
            } else if (Utils.rng.nextInt(3) < 2) {
                Utils.tryBuild(RobotType.SOLDIER, rc);
            } else {
                Utils.tryBuild(RobotType.BUILDER, rc);
            }
        }
    }
}