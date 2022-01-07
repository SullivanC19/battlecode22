package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon {
<<<<<<< HEAD
    public static void run(RobotController rc) throws GameActionException {
        int archonCount = rc.getArchonCount();
        Communication.updateFood(rc);
=======
    public static void run() throws GameActionException {
        Communication.updateLead();
>>>>>>> 9e5ff408d22963fd2c15bf9aa91f15fcbc5b5e63

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (Communication.isLead(x, y)) {
                    for (int r = 0; r < 4; r++) for (int c = 0; c < 4; c++) Memory.rc.setIndicatorDot(new MapLocation(x*4+r, y*4+c), 0, 255, 0);
                }
            }
        }

<<<<<<< HEAD
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
          
=======
        if (Memory.rc.getRoundNum() < 20) {
            Utils.tryBuild(RobotType.MINER);
        } else if (Memory.rc.getRoundNum() < 100 && Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
            if (Utils.rng.nextInt(3) == 0) {
                Utils.tryBuild(RobotType.SOLDIER);
            } else {
                Utils.tryBuild(RobotType.MINER);
            }
        } else if (Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.WATCHTOWER.buildCostLead) {
            Utils.tryBuild(Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)]);
>>>>>>> 9e5ff408d22963fd2c15bf9aa91f15fcbc5b5e63
        }
    }
}