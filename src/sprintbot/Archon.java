package sprintbot;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;


public class Archon {
    static final double[] s1 = new double[] {.45,.45,.1};
    static final double[] s2 = new double[] {.35,.55,.1};
    static final double[] s3 = new double[] {.25,.65,.1};

    public static void run2() throws GameActionException {
        Communication.updateLead();

        int archonCount = Memory.rc.getArchonCount();

        if (Memory.rc.getRoundNum() < 100 && Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
            if (Utils.rng.nextInt(2) == 0) {
                if(Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) / 2 > RobotType.SOLDIER.buildCostLead)
                    Utils.tryBuild(RobotType.SOLDIER);
            } else {
                if(Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) / 2 > RobotType.MINER.buildCostLead)
                    Utils.tryBuild(RobotType.MINER);
            }
        } else if (Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.WATCHTOWER.buildCostLead) {
            double[] stage = Memory.rc.getRoundNum() < 500 ? s1 : (Memory.rc.getRoundNum() < 1000 ? s2 : s3);
            //RobotType droidType = Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)];
           // if (Communication.getDroidCount(RobotType.MINER) / (double)Memory.rc.getRobotCount() < stage[0]) {
            if(Communication.getTotalLeadArea() * 3 > Communication.getDroidCount(RobotType.MINER)) {
                Utils.tryBuild(RobotType.MINER);
            } else if (Communication.getDroidCount(RobotType.SOLDIER) / (double)Memory.rc.getRobotCount() < stage[1]) {
                Utils.tryBuild(RobotType.SOLDIER);
            } else if (Communication.getDroidCount(RobotType.BUILDER) / (double)Memory.rc.getRobotCount() < stage[2]) {
                Utils.tryBuild(RobotType.BUILDER);
            } else {
                RobotType randDroidType = Utils.leadDroidTypes[Utils.rng.nextInt(Utils.leadDroidTypes.length)];
                if (Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) / archonCount > randDroidType.buildCostLead) {
                    Utils.tryBuild(randDroidType);
                }

            }
        }

        Communication.updateDroidCount();
    }

    public static void run() throws GameActionException {
        Communication.updateLead();

        int minerCount = Communication.getDroidCount(RobotType.MINER);
        int builderCount = Communication.getDroidCount(RobotType.BUILDER);
        int soldierCount = Communication.getDroidCount(RobotType.SOLDIER);
        int totalDroids = minerCount + builderCount + soldierCount;

        int totalLeadArea = Communication.getTotalLeadArea();
        if (Memory.rc.getRoundNum() < 15) {
            Utils.tryBuild(RobotType.MINER);
        } else if (Memory.rc.getRoundNum() < 50) {
            Utils.tryBuild(Utils.rng.nextBoolean() ? RobotType.MINER : RobotType.SOLDIER);
        } else if (Memory.rc.getTeamLeadAmount(Memory.rc.getTeam()) >= RobotType.WATCHTOWER.buildCostLead) {
            if (builderCount < (int) (totalDroids * .2)) {
                Utils.tryBuild(RobotType.BUILDER);
            } else if (minerCount < 60 && minerCount < totalLeadArea * 3) {
                Utils.tryBuild(RobotType.MINER);
            } else {
                Utils.tryBuild(RobotType.SOLDIER);
            }
        }

        // archon will reset the droid count for the turn so we put it last
        Communication.updateDroidCount();
    }
}