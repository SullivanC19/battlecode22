package firstplayer;

import battlecode.common.*;
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
static final VISION_RADIUS = 20
static final Random rng = new Random(6147);

public class Soldier {
    public static void run(RobotController rc) throws GameActionException {
         // Try to attack someone
         int radius = rc.getType().actionRadiusSquared;
         Team opponent = rc.getTeam().opponent();
         RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
         if (enemies.length > 0) {
             MapLocation toAttack = enemies[0].location;
             if (rc.canAttack(toAttack)) {
                 rc.attack(toAttack);
             }
         }
 
         // Also try to move randomly.
         Direction dir = directions[rng.nextInt(directions.length)];
         if (rc.canMove(dir)) {
             rc.move(dir);
            
         }
     }
    }
}