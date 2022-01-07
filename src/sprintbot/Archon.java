package sprintbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon {
    public static void run() throws GameActionException {
        Communication.updateLead();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (Communication.isLead(x, y)) {
                    for (int r = 0; r < 4; r++) for (int c = 0; c < 4; c++) Memory.rc.setIndicatorDot(new MapLocation(x*4+r, y*4+c), 0, 255, 0);
                }
            }
        }

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
        }
    }
}