package qualificationbot_2;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pathfinder {
    private static Direction exploreDir = Direction.CENTER;
    private static MapLocation exploreLoc = null;
    private static MapLocation exploreBlock = null;

    private static MapLocation lastExploreBlockCenter = null;

    private static final List<MapLocation> potentialEnemyArchonLocations = new ArrayList<>(Arrays.asList(
            new MapLocation(
                    Memory.rc.getMapWidth() - 1 - Memory.archonLocation.x,
                    Memory.rc.getMapHeight() - 1 - Memory.archonLocation.y),
            new MapLocation(
                    Memory.archonLocation.x,
                    Memory.rc.getMapHeight() - 1 - Memory.archonLocation.y),
            new MapLocation(
                    Memory.rc.getMapWidth() - 1 - Memory.archonLocation.x,
                    Memory.archonLocation.y)
    ));

    public static void exploreRandomly() throws GameActionException {
        MapLocation myBlock = Utils.getMyBlock();
        MapLocation archonBlock = Utils.getBlock(Memory.archonLocation);

        int mapBlockWidth = Memory.rc.getMapWidth() / Utils.BLOCK_SIZE;
        int mapBlockHeight = Memory.rc.getMapHeight() / Utils.BLOCK_SIZE;

        if (lastExploreBlockCenter == null
                || !lastExploreBlockCenter.isAdjacentTo(myBlock)
                || exploreBlock == null
                || exploreBlock.equals(myBlock)) {
            exploreBlock = null;
            lastExploreBlockCenter = myBlock;

            int furthestDistFromArchon = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                int dx = (int) (Utils.rng.nextGaussian() * mapBlockWidth / 4);
                int dy = (int) (Utils.rng.nextGaussian() * mapBlockHeight / 4);
                MapLocation block = new MapLocation(
                        Math.max(0, Math.min(mapBlockWidth - 1, myBlock.x + dx)),
                        Math.max(0, Math.min(mapBlockHeight - 1, myBlock.y + dy)));
                int distFromArchon = Math.max(Math.abs(block.x - archonBlock.x), Math.abs(block.y - archonBlock.y));
                if (distFromArchon > furthestDistFromArchon) {
                    exploreBlock = block;
                    furthestDistFromArchon = distFromArchon;
                }
            }
        }

        Memory.rc.setIndicatorLine(Memory.rc.getLocation(), Utils.getCenterOfBlock(exploreBlock), 0, 0, 100);

        Pathfinder.moveTo(Utils.getCenterOfBlock(exploreBlock));
        lastExploreBlockCenter = myBlock;
    }

    public static void exploreEnemyArchons() throws GameActionException {
        if (exploreLoc == null) {
            if (potentialEnemyArchonLocations.isEmpty()) {
                exploreRandomly();
                return;
            }

            MapLocation myLoc = Memory.rc.getLocation();

            MapLocation closestArchonLoc = potentialEnemyArchonLocations.get(0);
            for (int i = 1; i < potentialEnemyArchonLocations.size(); i++) {
                if (myLoc.distanceSquaredTo(potentialEnemyArchonLocations.get(i)) < myLoc.distanceSquaredTo(closestArchonLoc)) {
                    closestArchonLoc = potentialEnemyArchonLocations.get(i);
                }
            }

            potentialEnemyArchonLocations.remove(closestArchonLoc);
            exploreLoc = closestArchonLoc;
        }

        moveTo(exploreLoc);

        if (Memory.rc.canSenseLocation(exploreLoc)) {
            RobotInfo robotAtLocation = Memory.rc.senseRobotAtLocation(exploreLoc);
            if (robotAtLocation == null
                    || robotAtLocation.getTeam() == Memory.rc.getTeam()
                    || robotAtLocation.getType() != RobotType.ARCHON) {
                exploreLoc = null;
            }
        }
    }

    public static boolean moveTo(MapLocation targetLoc) throws GameActionException {
        if (Memory.rc.getMovementCooldownTurns() >= GameConstants.COOLDOWN_LIMIT) return true;
        Direction dir = directionTo(targetLoc);
        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
            return true;
        }
        else {
            boolean rotateLeft = Utils.rng.nextBoolean();
            Direction rotated1 = rotateLeft ? dir.rotateLeft().rotateLeft() : dir.rotateRight().rotateRight();
            Direction rotated2 = rotated1.opposite();

            if (Memory.rc.canMove(rotated1)) {
                Memory.rc.move(rotated1);
                return true;
            } else if (Memory.rc.canMove(rotated2)) {
                Memory.rc.move(rotated2);
                return true;
            }
        }

        return false;
    }

    public static final int MAX_SENSE_RANGE = 5;

    private static final int MAP_SIZE = 64;
    private static final int MAP_MASK = MAP_SIZE - 1;
    private static double[][] map = new double[MAP_SIZE][MAP_SIZE];

    private static double[][] dist = new double[MAX_SENSE_RANGE + 1][MAX_SENSE_RANGE * 2 + 1];
    private static MapLocation[][] mapLoc = new MapLocation[MAX_SENSE_RANGE + 1][MAX_SENSE_RANGE * 2 + 1];
    private static int[][] par = new int[MAX_SENSE_RANGE + 1][MAX_SENSE_RANGE * 2 + 1];

    private static final int[][][][] parent = new int[][][][] {
            // cardinal
            new int[][][] {
                    // level 0
                    new int[][] {},
                    // level 1
                    new int[][] {
                            new int[] {0},
                            new int[] {0},
                            new int[] {0},
                    },
                    // level 2
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 0, 2},
                            new int[] {2, 1},
                            new int[] {2},
                    },
                    // level 3
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 0, 2},
                            new int[] {2, 1, 3},
                            new int[] {3, 2, 4},
                            new int[] {4, 3},
                            new int[] {4},
                    },
                    // level 4
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 0, 2},
                            new int[] {2, 1, 3},
                            new int[] {3, 2, 4},
                            new int[] {4, 3, 5},
                            new int[] {5, 4, 6},
                            new int[] {6, 5},
                            new int[] {6},
                    },
                    // level 5
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 0, 2},
                            new int[] {2, 1, 3},
                            new int[] {3, 2, 4},
                            new int[] {4, 3, 5},
                            new int[] {5, 4, 6},
                            new int[] {6, 5, 7},
                            new int[] {7, 6, 8},
                            new int[] {8, 7},
                            new int[] {8},
                    }
            },
            // diagonal
            new int[][][] {
                    // level 0
                    new int[][] {},
                    // level 1
                    new int[][] {
                            new int[] {0},
                            new int[] {0},
                            new int[] {0},
                    },
                    // level 2
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1},
                            new int[] {2, 1},
                            new int[] {2},
                    },
                    // level 3
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 2},
                            new int[] {2},
                            new int[] {3, 2},
                            new int[] {4, 3},
                            new int[] {4}
                    },
                    // level 4
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 2},
                            new int[] {2, 3},
                            new int[] {3},
                            new int[] {4, 3},
                            new int[] {5, 4},
                            new int[] {6, 5},
                            new int[] {6}
                    },
                    // level 5
                    new int[][] {
                            new int[] {0},
                            new int[] {0, 1},
                            new int[] {1, 2},
                            new int[] {2, 3},
                            new int[] {3, 4},
                            new int[] {4},
                            new int[] {5, 4},
                            new int[] {6, 5},
                            new int[] {7, 6},
                            new int[] {8, 7},
                            new int[] {8}
                    },
            }
    };

    private static final int[][] dirFromFirstParent = new int[][] {
            // level 0
            new int[] {},
            // level 1
            new int[] {-1, 0, 1},
            // level 2
            new int[] {-1, 0, 0, 0, 1},
            // level 3
            new int[] {-1, 0, 0, 0, 0, 0, 1},
            // level 4
            new int[] {-1, 0, 0, 0, 0, 0, 0, 0, 1},
            // level 5
            new int[] {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
    };

    private static final int DIAG_IDX = 1;
    private static final int CARD_IDX = 0;

    private static int getDirectionIdx(Direction dir) {
        switch (dir) {
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                return CARD_IDX;
            case NORTHEAST:
            case NORTHWEST:
            case SOUTHEAST:
            case SOUTHWEST:
                return DIAG_IDX;
            default:
                return -1;
        }
    }

    private static double AVERAGE_COOLDOWN = 2;

    private static double getEstimatedDist(MapLocation loc1, MapLocation loc2) {
        return AVERAGE_COOLDOWN * (Math.abs(loc1.x - loc2.x) + Math.abs(loc1.y - loc2.y));
    }

    private static boolean inSensorRange(MapLocation loc) {
        return Memory.rc.canSenseRadiusSquared(loc.distanceSquaredTo(Memory.rc.getLocation()));
    }

    private static double getDistTo(MapLocation loc, double prevDist) throws GameActionException {
        int xModded = loc.x & MAP_MASK;
        int yModded = loc.y & MAP_MASK;
        if (inSensorRange(loc)) {
            if (!Memory.rc.onTheMap(loc)) return Double.MAX_VALUE;
            map[xModded][yModded] = map[xModded][yModded] == 0 ? (1 + Memory.rc.senseRubble(loc) / 10.0) : map[xModded][yModded];
            if (Memory.rc.isLocationOccupied(loc)) {
                return Double.MAX_VALUE;
            }
        } else {
            return prevDist + AVERAGE_COOLDOWN;
        }
        return prevDist + map[xModded][yModded];
    }

    private static Direction directionTo(MapLocation targetLoc) throws GameActionException {
        MapLocation startLoc = Memory.rc.getLocation();
        if (startLoc.equals(targetLoc)) return Direction.CENTER;

        Direction straightDir = startLoc.directionTo(targetLoc);
        Direction leftDir = straightDir.rotateLeft();
        Direction rightDir = straightDir.rotateRight();

        int dirIdx = getDirectionIdx(straightDir);

        int level = MAX_SENSE_RANGE;

        // compute distances
        dist[0][0] = 0;
        mapLoc[0][0] = startLoc;
        for (int l = 1; l <= level; ++l) {
            int prevLevel = l - 1;
            for (int i = 0; i <= l * 2; ++i) {
                // find best parent
                int[] pars = parent[dirIdx][l][i];
                double prevDist = dist[prevLevel][pars[0]];
                par[l][i] = pars[0];
                for (int p = 1; p < pars.length; ++p) {
                    if (dist[prevLevel][pars[p]] < prevDist) {
                        prevDist = dist[prevLevel][pars[p]];
                        par[l][i] = pars[p];
                    }
                }

                // find direction from 0th parent
                int dirRotateIdx = dirFromFirstParent[l][i];
                Direction dir;
                switch (dirRotateIdx) {
                    case -1:
                        dir = leftDir;
                        break;
                    case 1:
                        dir = rightDir;
                        break;
                    default:
                        dir = straightDir;
                        break;
                }

                // set map location using 0th parent loc and dir and get distance
                mapLoc[l][i] = mapLoc[prevLevel][pars[0]].add(dir);
                dist[l][i] = getDistTo(mapLoc[l][i], prevDist);

                // stop the search if target loc is reached
                if (mapLoc[l][i].equals(targetLoc)) {
                    level = l;
                }
            }
        }

        int bestIdx = -1;
        double closestTotDist = Double.MAX_VALUE;
        for (int i = 0; i <= level * 2; ++i) {
            if (mapLoc[level][i].equals(targetLoc)) {
                bestIdx = i;
                break;
            }
            double totDist = dist[level][i] + getEstimatedDist(mapLoc[level][i], targetLoc);
            if (totDist < closestTotDist) {
                bestIdx = i;
                closestTotDist = totDist;
            }
        }

        // if none of the edge locations are reachable, pick the middle one
        if (bestIdx == -1) bestIdx = level;

        // backtrack to find direction and display path for debugging
        int idx = bestIdx;
        for (int l = level; l > 1; --l) {
//            Memory.rc.setIndicatorLine(mapLoc[l][idx], mapLoc[l - 1][par[l][idx]], 255, 0, 255);
            idx = par[l][idx];
        }

        switch (idx) {
            case 0:
                return leftDir;
            case 1:
                return straightDir;
            case 2:
                return rightDir;
            default:
                return null;
        }
    }

    public static void moveAway(MapLocation loc) throws GameActionException {
        MapLocation myLoc = Memory.rc.getLocation();

        Memory.rc.setIndicatorLine(myLoc, loc, 120, 0, 0);

        Direction initDir = loc.directionTo(myLoc);
        Direction bestDir = initDir;
        int lowestRubble = Integer.MAX_VALUE;
        for (Direction dir = initDir.rotateLeft(); dir != initDir.rotateRight().rotateRight(); dir = dir.rotateRight()) {
            MapLocation adjLoc = loc.add(dir);
            if (!Memory.rc.canSenseLocation(adjLoc) || Memory.rc.isLocationOccupied(adjLoc)) continue;

            int rubble = Memory.rc.senseRubble(adjLoc);
            if (rubble < lowestRubble) {
                bestDir = dir;
                lowestRubble = rubble;
            }
        }

        if (Memory.rc.canMove(bestDir)) Memory.rc.move(bestDir);
    }

    public static void shuffleRandomly() throws GameActionException {
        Direction dir = Utils.randomDirection();
        if (Memory.rc.canMove(dir)) {
            Memory.rc.move(dir);
        }
    }
}
