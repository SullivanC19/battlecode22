package cole2;

import battlecode.common.*;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static float pnx=0.0f;
    static float pny=0.0f;
    static float pnc=0.0f;
    static int made=0;

    static float pcnx=0.0f;
    static float pcny=0.0f;
    static float pcnc=0.0f;

    static float pmx=0.0f;
    static float pmy=0.0f;
    static int archonCount=1;
    static int aCount = 0;


    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
//        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount=rc.getRoundNum()-1;
if(turnCount%3==0){
    rc.writeSharedArray(0, 0);
}
if(turnCount%3==1 && rc.getType()==RobotType.ARCHON){
    aCount=rc.readSharedArray(0);
    rc.writeSharedArray(0, rc.readSharedArray(0)+1);
}
if(turnCount%3==2){
archonCount=rc.readSharedArray(0);
}
//            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());
            runFlock(rc);

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc);  break;
                    case MINER:      runMiner(rc);   break;
                    case SOLDIER:    runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:
                    case SAGE:       break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                pnx=pcnx;
                pny=pcny;
                pnc=pcnc;
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    static void move(RobotController rc,Direction d) throws GameActionException {
        // Try to mine on squares around us.
        

        if (rc.canMove(d)) {
            pmx=d.dx;
            pmy=d.dy;
            rc.move(d);
        }
    }
    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {
        // aCount=rc.readSharedArray(1);
        // rc.writeSharedArray(1, rc.readSharedArray(1)+1);
        // Pick a direction to build in.
        int i=rng.nextInt(directions.length);
        int built=0;
        // made=1;
        int max_m=rc.getTeamLeadAmount(rc.getTeam());
        int ex=0;
        float mcount=((float) max_m)/(50.0f+rng.nextFloat()*25.0f)/((float) archonCount-(float)aCount);
        rc.setIndicatorString("Trying to build " + mcount+ ", "+aCount+" / "+archonCount+" ");
        if(rng.nextFloat()<mcount){
        for(int j=0;j<directions.length;j++){
            if((float)built<mcount+(turnCount<2?1.0f:0.0f)){
        Direction dir = directions[(i+j)%directions.length];
        if((float)ex<mcount/2.0f && turnCount>10){
         rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                built+=1;
                made=0;
                ex+=1;
                continue;
            }
        }
        if (made==0 || turnCount<10) {
            // Let's try to build a miner.
            // rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
                built+=1;
                made=1;
            }
        } else {
            // Let's try to build a soldier.
            // rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                built+=1;
                made=0;
            }
        }
            }
         
        }
    }
    }
    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runFlock(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        RobotInfo[] rr=rc.senseNearbyRobots();
        pcnx=0.0f;
        pcny=0.0f;
        pcnc=0.0f;
        for(int i=0;i<rr.length;i++){
            RobotInfo r=rr[i];
            if((r.type==rc.getType()) && r.team==rc.getTeam()){
            pcnx+=(float)(r.location.x-me.x);
            pcny+=(float)(r.location.y-me.y);
            pcnc+=1.0f;
            }
        }
        
    }

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        boolean mnd=false;
        float farmx=0.0f;
        float farmy=0.0f;
        float farmc=0.0f;
        MapLocation[] hh=rc.senseNearbyLocationsWithLead();
        for (int i=0; i <hh.length; i++) {
            MapLocation mp=hh[i];
                int dx=mp.x-me.x;
                int dy=mp.y-me.y;
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
               if(	rc.senseRobotAtLocation(mineLocation)==null){

               
                if(rc.senseLead(mineLocation)>0) {
                    farmc+=1.0f;
                    farmx+=(float)dx;
                    farmy+=(float)dy;
                }
               }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation)>0) {
                    rc.mineGold(mineLocation);
                    mnd=true;
                }
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation)>1) {
                    rc.mineLead(mineLocation);
                    mnd=true;
                }
            }
        }
        RobotInfo[] rr=rc.senseNearbyRobots();
        float arx=0.0f;
        float ary=0.0f;
        float arc=0.0f;
        float armx=0.0f;
        float army=0.0f;
        float armc=0.0f;
        for(int i=0;i<rr.length;i++){
            RobotInfo r=rr[i];
            if(r.team!=rc.getTeam()){
                float m=r.type==RobotType.ARCHON?1.0f:1.0f;
            arx+=(float)(r.location.x-me.x)*m;
            ary+=(float)(r.location.y-me.y)*m;
            arc+=1.0f*m;
            }else{
    float m=r.type==RobotType.ARCHON?1.0f:0.0f;
            armx+=(float)(r.location.x-me.x)*m;
            army+=(float)(r.location.y-me.y)*m;
            armc+=1.0f*m;
            }
        }
        // Also try to move randomly.
        float[] w=new float[8];
        float mxS=-100000000.0f;
        Direction bestD=directions[0];
        for(int i=0;i<directions.length;i++){
            Direction dir = directions[i];
            if (rc.canMove(dir)) {
            float scoree=0.0f;
            scoree=rng.nextFloat()*0.0001f;
            float a=0.0f;//3.14159265f/3.0f;
            if(pnc>0.0f && pcnc>0.0f && false){
                //*(pcnc+pnc)/2.0f
                float oxc=(pcnx/pcnc*1.0f-pnx/pnc)+pmx;
                float oyc=(pcny/pcnc*1.0f-pny/pnc)+pmy;
                float xc=(float)(-Math.sin(a)*oyc+Math.cos(a)*oxc)+pmx;
                float yc=(float)(Math.sin(a)*oxc+Math.cos(a)*oyc)+pmy;
                
            scoree+=((float) dir.dx)*xc+((float) dir.dy)*yc;
            }else{
                float oxc=pmx;
                float oyc=pmy;
                float xc=(float)(-Math.sin(a)*oyc+Math.cos(a)*oxc);
                float yc=(float)(Math.sin(a)*oxc+Math.cos(a)*oyc);
            scoree+=((float) dir.dx)*xc+((float) dir.dy)*yc;
            }
            if(arc>0.0f){
                float xc=(float)(arx/arc);
                float yc=(float)(ary/arc);
            //scoree+=-(((float) dir.dx)*xc+((float) dir.dy)*yc)*1.0f;
            }
            if(armc>0.0f){
                float xc=(float)(armx/armc);
                float yc=(float)(army/armc);
                float dg=0.0f;
                if(((float) Math.sqrt(xc*xc+yc*yc))<4.0f){
                    dg+=10.0f;
                }
            scoree+=-(((float) dir.dx)*xc+((float) dir.dy)*yc)*dg/((float) Math.sqrt(xc*xc+yc*yc))/((float) Math.sqrt(xc*xc+yc*yc))*armc;
            }
            if(farmc>0.0f){
                float xc=(float)(farmx);
                float yc=(float)(farmy);
            scoree+=(((float) dir.dx)*xc+((float) dir.dy)*yc)*1.0f/((float) Math.sqrt(xc*xc+yc*yc))*farmc;
            }
            //scoree*=rng.nextFloat()*0.5f+0.5f;
            w[i]=scoree;
            if(scoree>mxS){
                mxS=scoree;
                bestD=dir;
            }
            }
        }
        Direction dir = bestD;
        if(!mnd || rng.nextFloat()>0.9f){
        if (rc.canMove(dir)) {
            move(rc,dir);
//            System.out.println("I moved!");
        }
        }
    }

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
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
        float armx=0.0f;
        float army=0.0f;
        float armc=0.0f;

        MapLocation me = rc.getLocation();
        RobotInfo[] rr=rc.senseNearbyRobots();
        float arx=0.0f;
        float ary=0.0f;
        float arc=0.0f;
        for(int i=0;i<rr.length;i++){
            RobotInfo r=rr[i];
            if(r.team!=rc.getTeam()){
                float m=r.type==RobotType.ARCHON?10.0f:1.0f;
            arx+=(float)(r.location.x-me.x)*m;
            ary+=(float)(r.location.y-me.y)*m;
            arc+=1.0f*m;
            }else{
                float m=r.type==RobotType.ARCHON?1.0f:0.0f;
            armx+=(float)(r.location.x-me.x)*m;
            army+=(float)(r.location.y-me.y)*m;
            armc+=1.0f*m;
            }
        }

        // Also try to move randomly.
        float[] w=new float[8];
        float mxS=-100000000.0f;
        Direction bestD=directions[0];
        for(int i=0;i<directions.length;i++){
            Direction dir = directions[i];
            if (rc.canMove(dir)) {
            float scoree=0.0f;
            scoree=rng.nextFloat()*0.0001f;
            float a=0.0f;//3.14159265f/3.0f;
            if(pnc>0.0f && pcnc>0.0f){
                //*(pcnc+pnc)/2.0f
                float oxc=(pcnx/pcnc*1.0f-pnx/pnc)+pmx;
                float oyc=(pcny/pcnc*1.0f-pny/pnc)+pmy;
                float xc=(float)(-Math.sin(a)*oyc+Math.cos(a)*oxc)+pmx;
                float yc=(float)(Math.sin(a)*oxc+Math.cos(a)*oyc)+pmy;
                
            scoree+=((float) dir.dx)*xc+((float) dir.dy)*yc;
            }else{
                float oxc=pmx;
                float oyc=pmy;
                float xc=(float)(-Math.sin(a)*oyc+Math.cos(a)*oxc);
                float yc=(float)(Math.sin(a)*oxc+Math.cos(a)*oyc);
            scoree+=((float) dir.dx)*xc+((float) dir.dy)*yc;
            }
            if(arc>0.0f){
                float xc=(float)(arx/arc);
                float yc=(float)(ary/arc);
            scoree+=(((float) dir.dx)*xc+((float) dir.dy)*yc)*4.0f;
            }
            //scoree*=rng.nextFloat()*0.5f+0.5f;
            w[i]=scoree;
            if(scoree>mxS){
                mxS=scoree;
                bestD=dir;
            }
            }
        }
        Direction dir = bestD;
float mdg=0.0f;
            if(pnc>0.0f && pcnc>0.0f){
                float oxc=(pcnx/pcnc-pnx/pnc)+pmx;
                float oyc=(pcny/pcnc-pny/pnc)+pmy;
                mdg=(float) Math.sqrt(oxc*oxc+oyc*oyc);
            }
        if(arc>0.0f || mdg>0.0f || ((float) Math.sqrt(pmx*pmx+pmy*pmy)>0.0f) || armc>0.0f){
        if (rc.canMove(dir)) {
            move(rc,dir);
//            System.out.println("I moved!");
        }
        }
    }
}
