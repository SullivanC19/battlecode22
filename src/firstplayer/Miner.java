package firstplayer;
    static final Random rng = new Random(6147);
    static final int VISION_RADIUS = 20;
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    void moveInRange(RobotController rc, MapLocation square) {
        while(!rc.getLocation().isAdjacentTo(square)) {
            if(rc.canMove(rc.getLocation().directionTo(square))) {
                rc.move(rc.getLocation().directionTo(square));
            }
        }
    }
    public static void run(RobotController rc) throws GameActionException {
        
        MapLocation[] visibleSquares = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), VISION_RADIUS);
        bestGoldSquare = null;
        maxGoldCount = 0;
        bestLeadSquare = null;
        maxLeadCount = 0;
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
            int choice = rng.nextInt(directions.length);
            Direction dir = directions[choice];
            if(rc.canMove(dir)) rc.move(dir);
        }
        else if (bestGoldSquare != null && bestLeadSquare != null) {
            int choice = rng.nextInt(2);
            if(choice == 0) { //goto best lead
                moveInRange(bestLeadSquare);
                while(rc.canMineLead(bestLeadSquare)){
                    rc.mineLead(bestLeadSquare);
                }
            }
            else { //goto best gold
                moveInRange(bestGoldSquare);
                while(rc.canMineGold(bestGoldSquare)){
                    rc.mineGold(bestGoldSquare);
                }

            }
        }
        else if (bestGoldSquare != null) { //goto best gold
            moveInRange(bestGoldSquare);
            while(rc.canMineGold(bestGoldSquare)){
                rc.mineGold(bestGoldSquare);
            }
        }
        else { //goto best lead
            moveInRange(bestLeadSquare);
            while(rc.canMineLead(bestLeadSquare)){
                rc.mineLead(bestLeadSquare);
            }
        }

            
        }
    }
}