package entity;

import main.GamePanel;
import java.util.*;

/**
 * BFS Pathfinding Algorithm Implementation
 * 
 * PRESENTATION POINT:
 * - BREADTH-FIRST SEARCH (BFS) - Finds shortest path from Ghost to Pacman
 * - Treats the game map as an unweighted graph where:
 *   - Empty tiles are NODES
 *   - Adjacent tiles are EDGES
 *   - Walls are BLOCKED nodes
 * 
 * Algorithm Flow:
 * 1. Start at Ghost position (Source node)
 * 2. Explore neighbors layer by layer using a Queue
 * 3. Stop when Pacman position is found (Target node)
 * 4. Backtrack from Pacman to Ghost to determine the path
 */
public class BFSPathfinder {
    private GamePanel gp;

    public BFSPathfinder(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Performs BFS to find the shortest path from Ghost to Pacman.
     * 
     * @param ghostX - Ghost's current X position
     * @param ghostY - Ghost's current Y position
     * @param pacmanX - Pacman's current X position
     * @param pacmanY - Pacman's current Y position
     * @return PathLinkedList containing the sequence of directions to follow
     */
    public PathLinkedList findPath(int ghostX, int ghostY, int pacmanX, int pacmanY) {
        // Convert pixel coordinates to tile coordinates
        int startTileX = ghostX / gp.tileSize;
        int startTileY = ghostY / gp.tileSize;
        int targetTileX = pacmanX / gp.tileSize;
        int targetTileY = pacmanY / gp.tileSize;

        // Queue for BFS exploration
        Queue<PathfindingNode> queue = new LinkedList<>();
        // Track visited nodes to avoid cycles
        HashSet<String> visited = new HashSet<>();

        // Start node
        PathfindingNode start = new PathfindingNode(startTileX, startTileY, null);
        queue.add(start);
        visited.add(startTileX + "," + startTileY);

        PathfindingNode targetNode = null;

        // BFS exploration - expand layer by layer like ripples in water
        while (!queue.isEmpty()) {
            PathfindingNode current = queue.poll();

            // Check if we reached Pacman
            if (current.x == targetTileX && current.y == targetTileY) {
                targetNode = current;
                break;
            }

            // Explore 4 neighboring tiles: Up, Down, Left, Right
            int[][] directions = {
                {0, -1}, // Up
                {0, 1},  // Down
                {-1, 0}, // Left
                {1, 0}   // Right
            };

            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                String key = newX + "," + newY;

                // Check if the new position is valid
                if (!visited.contains(key) && isWalkable(newX, newY)) {
                    PathfindingNode neighbor = new PathfindingNode(newX, newY, current);
                    queue.add(neighbor);
                    visited.add(key);
                }
            }
        }

        // Backtrack from target to start to build the path
        PathLinkedList path = new PathLinkedList();
        if (targetNode != null) {
            PathfindingNode node = targetNode;
            while (node.parent != null) {
                PathfindingNode parent = node.parent;
                char direction = getDirection(parent.x, parent.y, node.x, node.y);
                path.addLast(direction);
                node = parent;
            }
        }

        // Reverse the path (we built it backwards)
        return reversePath(path);
    }

    /**
     * Checks if a tile position is walkable (not a wall).
     */
    private boolean isWalkable(int tileX, int tileY) {
        // Check bounds
        if (tileX < 0 || tileX >= gp.columnCount || tileY < 0 || tileY >= gp.rowCount) {
            return false;
        }

        // Check if there's a wall at this position
        int pixelX = tileX * gp.tileSize;
        int pixelY = tileY * gp.tileSize;

        for (Entity wall : gp.walls) {
            if (wall.x == pixelX && wall.y == pixelY) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines the direction from parent to child node.
     */
    private char getDirection(int fromX, int fromY, int toX, int toY) {
        if (toX < fromX) return 'L'; // Left
        if (toX > fromX) return 'R'; // Right
        if (toY < fromY) return 'U'; // Up
        if (toY > fromY) return 'D'; // Down
        return ' '; // No movement
    }

    /**
     * Reverses the path since we built it backwards during backtracking.
     */
    private PathLinkedList reversePath(PathLinkedList original) {
        PathLinkedList reversed = new PathLinkedList();
        Stack<Character> stack = new Stack<>();
        
        while (!original.isEmpty()) {
            stack.push(original.removeFirst());
        }
        
        while (!stack.isEmpty()) {
            reversed.addLast(stack.pop());
        }
        
        return reversed;
    }
}
