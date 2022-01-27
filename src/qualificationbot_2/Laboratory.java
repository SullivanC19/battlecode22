package qualificationbot_2;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class Laboratory {
    public static void run() throws GameActionException {

        int budget = Communication.getBudget();

        int roundNum = Memory.rc.getRoundNum();

        int teamGold = Memory.rc.getTeamGoldAmount(Memory.rc.getTeam());

        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;

        int mapBlockArea = mapBlockWidth * mapBlockHeight;

        int minNumMiners = mapBlockArea / 24;

        if (Memory.rc.canTransmute()
                && teamGold < RobotType.SAGE.buildCostGold
                /*&& Communication.getDroidCount(RobotType.MINER) >= minNumMiners*/) {
            Memory.rc.transmute();
        }

        Communication.updateDroidCount();

    }
}