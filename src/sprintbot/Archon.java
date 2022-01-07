package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon {
    public static void run() throws GameActionException {
        Communication.updateLead();

        int archonCount = Memory.rc.getArchonCount();
        if (Memory.rc.getRoundNum() < 20) {
            Utils.tryBuild(RobotType.MINER);
        } else if (Memory.rc.getRoundNum() < 100 && Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
            if (Utils.rng.nextInt(2) == 0) {
                if(Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) / archonCount > RobotType.SOLDIER.buildCostLead)
                    Utils.tryBuild(RobotType.SOLDIER);
            } else {
                if(Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) / archonCount > RobotType.MINER.buildCostLead)
                    Utils.tryBuild(RobotType.MINER);
            }
        } else if (Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.WATCHTOWER.buildCostLead) {
            //RobotType droidType = Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)];
            if (Communication.getDroidCount(RobotType.SOLDIER) < 10) {
                Utils.tryBuild(RobotType.SOLDIER);
            } else if (Communication.getDroidCount(RobotType.MINER) < 10) {
                Utils.tryBuild(RobotType.MINER);
            } else if (Communication.getDroidCount(RobotType.BUILDER) < 10) {
                Utils.tryBuild(RobotType.BUILDER);
            } else {
                RobotType randDroidType = Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)];
                if (Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) / archonCount > randDroidType.buildCostLead) {
                    Utils.tryBuild(randDroidType);
                }

            }
        }

        // archon will reset the droid count for the turn so we put it last
        Communication.updateDroidCount();
    }
}