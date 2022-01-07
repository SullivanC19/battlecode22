package firstplayer;

import battlecode.common.*;

public class Miner {
    private static void moveInRange(RobotController rc, MapLocation square) throws GameActionException {
        while(!rc.getLocation().isAdjacentTo(square)) {
            if(rc.canMove(rc.getLocation().directionTo(square))) {
                rc.move(rc.getLocation().directionTo(square));
            }
        }
    }
    public static void run2(RobotController rc) throws GameActionException {
        
        MapLocation[] visibleSquares = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        MapLocation bestGoldSquare = null;
        int maxGoldCount = 0;
        MapLocation bestLeadSquare = null;
        int maxLeadCount = 0;

        for(int i = visibleSquares.length - 1; i > 0; i--) {
            int curGold = rc.senseGold(visibleSquares[i]);
            int curLead = rc.senseLead(visibleSquares[i]);
            if(curGold > maxGoldCount) {
                maxGoldCount = curGold;
                bestGoldSquare = visibleSquares[i];
            }
            if(curLead > maxLeadCount) {
                maxLeadCount = curLead;
                bestLeadSquare = visibleSquares[i];
            }
        }
        //no resources in range
        if(bestGoldSquare == null && bestLeadSquare == null) {
            int choice = Utils.rng.nextInt(Utils.directions.length);
            Direction dir = Utils.directions[choice];
            if(rc.canMove(dir)) rc.move(dir);
        }
        else if (bestGoldSquare != null && bestLeadSquare != null) {
            int choice = Utils.rng.nextInt(2);
            if(choice == 0) { //goto best lead
                moveInRange(rc, bestLeadSquare);
                while(rc.canMineLead(bestLeadSquare)){
                    rc.mineLead(bestLeadSquare);
                }
            }
            else { //goto best gold
                moveInRange(rc, bestGoldSquare);
                while(rc.canMineGold(bestGoldSquare)){
                    rc.mineGold(bestGoldSquare);
                }

            }
        }
        else if (bestGoldSquare != null) { //goto best gold
            moveInRange(rc, bestGoldSquare);
            while(rc.canMineGold(bestGoldSquare)){
                rc.mineGold(bestGoldSquare);
            }
        }
        else { //goto best lead
            moveInRange(rc, bestLeadSquare);
            while(rc.canMineLead(bestLeadSquare)){
                rc.mineLead(bestLeadSquare);
            }
        }
    }

    public static void run(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        // Also try to move randomly.
        Direction dir = Utils.randomDirection();
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}