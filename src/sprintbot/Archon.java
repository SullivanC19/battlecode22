package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon {
    public static void run(RobotController rc) throws GameActionException {
        int archonCount = rc.getArchonCount();
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
            if (Utils.rng.nextInt(2) == 0) {
                if(rc.getTeamLeadAmount(rc.getTeam()) / archonCount > RobotType.SOLDIER.buildCostLead)
                    Utils.tryBuild(RobotType.SOLDIER, rc);
            } else {
                if(rc.getTeamLeadAmount(rc.getTeam()) / archonCount > RobotType.MINER.buildCostLead)
                    Utils.tryBuild(RobotType.MINER, rc);
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.WATCHTOWER.buildCostLead) {
            //RobotType droidType = Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)];
            if(Communication.getDroidCount(RobotType.SOLDIER) < 10) {
                Utils.tryBuild(RobotType.SOLDIER, rc);
            }
            else if(Communication.getDroidCount(RobotType.MINER) < 10) {
                Utils.tryBuild(RobotType.MINER, rc);
            }
            else if (Communication.getDroidCount(RobotType.BUILDER) < 10) {
                Utils.tryBuild(RobotType.BUILDER, rc);
            }
            else {
                RobotType randDroidType = Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)];
                if(rc.getTeamLeadAmount(rc.getTeam()) / archonCount > randDroidType.buildCostLead) {
                    Utils.tryBuild(randDroidType, rc);
                }

            }
          
        }
    }
}