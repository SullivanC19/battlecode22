package firstplayer;

import battlecode.common.*;

public class Archon {
    public static void run(RobotController rc) throws GameActionException {
        if (rc.getRoundNum() < 100) {
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