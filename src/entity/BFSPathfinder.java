package entity;

import main.GamePanel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;

/**
 * BFSPathfinder - Breadth-First Search algorithm for ghost pathfinding
 * 
 * ============================================================================
 * GRAPH THEORY FOUNDATION
 * ============================================================================
 * 
 * To enable the Ghost to intelligently track Pacman, we treat the game map 
 * as an UNWEIGHTED GRAPH:
 * 
 * • NODES (Vertices): Every empty tile in the game is a Node
 * • EDGES: The connections between adjacent tiles (up, down, left, right)
 * • BLOCKED NODES: Walls are treated as unreachable/blocked nodes
 * • UNWEIGHTED: All edges have equal cost (1 step = 1 tile movement)
 * 
 * ============================================================================
 * BFS ALGORITHM PROCESS
 * ============================================================================
 * 
 * The BFS process works as follows:
 * 
 * 1. SOURCE (G): The algorithm starts at the Ghost's current position
 *    - Represented as the RED NODE in visualization
 *    - This is our starting point in the graph
 * 
 * 2. EXPLORATION: It explores neighbor nodes LAYER BY LAYER
 *    - Similar to a ripple expanding in water
 *    - Level 0: Ghost's position
 *    - Level 1: All tiles 1 step away
 *    - Level 2: All tiles 2 steps away
 *    - And so on...
 * 
 * 3. QUEUE: We use a QUEUE data structure (FIFO) to store valid nodes
 *    - Ensures breadth-first exploration (closest nodes first)
 *    - Guarantees shortest path is found first
 * 
 * 4. TARGET (P): The expansion stops immediately when the 'wave' hits Pacman
 *    - Represented as the YELLOW NODE in visualization
 *    - This is our destination in the graph
 * 
 * 5. BACKTRACKING: The algorithm traces the path BACKWARDS from Pacman to Ghost
 *    - Uses parent links stored in each node
 *    - Determines the exact next step the Ghost must take
 *    - Path is reversed to get Ghost → Pacman direction sequence
 * 
 * ============================================================================
 * ALGORITHM GUARANTEES
 * ============================================================================
 * 
 * Thanks to BFS, the Ghost ALWAYS calculates the SHORTEST PATH to the player,
 * making the game challenging and intelligent.
 * 
 * ============================================================================
 * COMPLEXITY ANALYSIS
 * ============================================================================
 * 
 * TIME COMPLEXITY: O(V + E)
 *    - V = number of vertices (grid cells): rows × cols
 *    - E = number of edges (connections): ≈ 4V in a grid
 *    - Final: O(rows × cols) = O(21 × 19) = O(399)
 * 
 * SPACE COMPLEXITY: O(V)
 *    - Queue stores at most all vertices
 *    - HashSet stores visited vertices
 *    - Total: O(rows × cols)
 * 
 * ============================================================================
 * WHY BFS FOR PACMAN?
 * ============================================================================
 * 
 * ✓ Finds SHORTEST PATH (optimal ghost movement)
 * ✓ Handles OBSTACLES (walls) automatically
 * ✓ EFFICIENT for grid-based games
 * ✓ Creates REALISTIC and CHALLENGING AI behavior
 * ✓ GUARANTEED to find path if one exists
 */
public class BFSPathfinder {
    
    private GamePanel gp;
    
    /**
     * Constructor - Initialize pathfinder with game reference
     * 
     * @param gp - Game panel containing walls and grid info
     */
    public BFSPathfinder(GamePanel gp) {
        this.gp = gp;
    }
    
    /**
     * Find shortest path from start to target using BFS
     * 
     * ========================================================================
     * ALGORITHM OVERVIEW - GRAPH TRAVERSAL
     * ========================================================================
     * 
     * INPUT: 
     *   - Start position: Ghost's location (SOURCE NODE - G)
     *   - Target position: Pacman's location (TARGET NODE - P)
     * 
     * OUTPUT: 
     *   - PathLinkedList with step-by-step directions (U/D/L/R)
     *   - null if no path exists (Pacman unreachable)
     * 
     * ========================================================================
     * BFS STEPS - THE "RIPPLE EFFECT"
     * ========================================================================
     * 
     * STEP 1: COORDINATE TRANSFORMATION
     *   - Convert pixel coordinates → grid coordinates (nodes)
     *   - Validate bounds and ensure target isn't a wall
     * 
     * STEP 2: INITIALIZATION
     *   - Create QUEUE (FIFO) with starting node (Ghost position)
     *   - Create VISITED SET to track explored nodes
     *   - Mark start as visited
     * 
     * STEP 3: BREADTH-FIRST EXPLORATION (The Ripple)
     *   While queue is not empty:
     *     a. DEQUEUE current node (explore closest unexplored node)
     *     b. CHECK if we reached Pacman → SUCCESS!
     *     c. EXPLORE all 4 neighbors (Up, Down, Left, Right)
     *     d. For each valid neighbor:
     *        - Skip if wall or out of bounds
     *        - Skip if already visited
     *        - Add to queue and mark visited
     *        - Link parent (for backtracking)
     * 
     * STEP 4: BACKTRACKING
     *   - Trace path from Pacman back to Ghost using parent links
     *   - Reverse the path to get Ghost → Pacman directions
     * 
     * STEP 5: RETURN RESULT
     *   - Return path as sequence of directions
     *   - Return null if queue empties (no path exists)
     * 
     * ========================================================================
     * VISUAL EXPLANATION
     * ========================================================================
     * 
     * Level 0: [G]              ← Ghost starts here (RED NODE)
     * Level 1: [·][·][·]        ← Explore immediate neighbors
     * Level 2: [·][·][·][·][·]  ← Expand outward (ripple effect)
     * Level 3: [·][P][·]        ← Found Pacman! (YELLOW NODE)
     * 
     * The "wave" expands until it hits Pacman, guaranteeing shortest path!
     * 
     * ========================================================================
     * 
     * @param startX - Ghost's current X position (pixels)
     * @param startY - Ghost's current Y position (pixels)
     * @param targetX - Pacman's X position (pixels)
     * @param targetY - Pacman's Y position (pixels)
     * @return PathLinkedList with directions, or null if no path exists
     */
    public PathLinkedList findPath(int startX, int startY, int targetX, int targetY) {
        
        // === STEP 1: Convert pixel coordinates to grid coordinates ===
        // PRESENTATION POINT: Coordinate system transformation
        // GRAPH THEORY: Convert pixel positions to NODES in the graph
        // Pixels → Tiles: divide by tileSize
        // Round to nearest tile for better accuracy
        int startRow = Math.round((float)startY / gp.tileSize);
        int startCol = Math.round((float)startX / gp.tileSize);
        int targetRow = Math.round((float)targetY / gp.tileSize);
        int targetCol = Math.round((float)targetX / gp.tileSize);
        
        // Clamp to valid grid bounds
        startRow = Math.max(0, Math.min(GamePanel.rowCount - 1, startRow));
        startCol = Math.max(0, Math.min(GamePanel.columnCount - 1, startCol));
        targetRow = Math.max(0, Math.min(GamePanel.rowCount - 1, targetRow));
        targetCol = Math.max(0, Math.min(GamePanel.columnCount - 1, targetCol));
        
        // Edge case: Already at target
        if (startRow == targetRow && startCol == targetCol) {
            return new PathLinkedList();  // Return empty path
        }
        
        // Edge case: Target is a wall (shouldn't happen but be safe)
        if (isWall(targetRow, targetCol)) {
            return null;
        }
        
        // === STEP 2: Initialize BFS data structures ===
        
        // QUEUE: Stores nodes to explore (FIFO - First In First Out)
        // PRESENTATION POINT: Queue is KEY to BFS algorithm
        // GRAPH THEORY: Queue ensures BREADTH-FIRST exploration
        // - Ensures level-by-level exploration (ripple effect)
        // - Guarantees shortest path found first
        Queue<PathfindingNode> queue = new LinkedList<>();
        
        // VISITED SET: Tracks explored positions (prevents infinite loops)
        // PRESENTATION POINT: HashSet for O(1) lookup
        // GRAPH THEORY: Prevents revisiting the same NODE
        // - Prevents revisiting same position
        // - Critical for algorithm termination
        HashSet<String> visited = new HashSet<>();
        
        // Create starting node (no parent)
        // GRAPH THEORY: This is the SOURCE NODE (G) - Red Node
        PathfindingNode startNode = new PathfindingNode(startRow, startCol, null);
        
        // Add start to queue and mark as visited
        queue.offer(startNode);
        visited.add(startRow + "," + startCol);
        
        // === STEP 3: BFS MAIN LOOP - THE RIPPLE EFFECT ===
        // PRESENTATION POINT: This is the heart of BFS
        // GRAPH THEORY: Layer-by-layer exploration like water ripples
        // Continue until queue is empty (all reachable nodes explored)
        
        while (!queue.isEmpty()) {
            
            // DEQUEUE: Remove and get first node from queue
            // PRESENTATION POINT: FIFO order ensures breadth-first
            // GRAPH THEORY: Explore closest unexplored node first
            PathfindingNode current = queue.poll();
            
            // === STEP 4: Check if we reached the target ===
            // GRAPH THEORY: Did our ripple reach the TARGET NODE (P) - Yellow Node?
            if (current.row == targetRow && current.col == targetCol) {
                // SUCCESS! We found Pacman
                // Now BACKTRACK the path from target back to start
                return reconstructPath(current);
            }
            
            // === STEP 5: Explore all 4 neighbors (expand the ripple) ===
            // PRESENTATION POINT: Grid has 4-connectivity (Up, Down, Left, Right)
            // GRAPH THEORY: Explore all EDGES from current NODE
            // We explore in all directions to find shortest path
            
            // Try moving UP (row - 1)
            exploreNeighbor(current, current.row - 1, current.col, queue, visited);
            
            // Try moving DOWN (row + 1)
            exploreNeighbor(current, current.row + 1, current.col, queue, visited);
            
            // Try moving LEFT (col - 1)
            exploreNeighbor(current, current.row, current.col - 1, queue, visited);
            
            // Try moving RIGHT (col + 1)
            exploreNeighbor(current, current.row, current.col + 1, queue, visited);
        }
        
        // Queue is empty and target not found
        // GRAPH THEORY: No path exists in the graph (Pacman unreachable)
        // This means Pacman is unreachable from ghost's position
        return null;
    }
    
    /**
     * Explore a neighboring grid cell
     * 
     * PRESENTATION POINTS:
     * 1. VALIDATION - Check bounds, walls, and visited status
     * 2. NODE CREATION - Create new node with parent link
     * 3. ENQUEUE - Add to queue for future exploration
     * 4. MARK VISITED - Prevent revisiting
     * 
     * GRAPH THEORY:
     * - This explores an EDGE from current node to neighbor
     * - Validates the neighbor is a valid NODE (not wall, in bounds)
     * - Adds valid neighbors to the exploration frontier (queue)
     * - Parent link enables BACKTRACKING to reconstruct path
     * 
     * ALGORITHM STEPS:
     * Step 1: Check if position is within grid bounds
     * Step 2: Check if position is a wall (obstacle)
     * Step 3: Check if already visited (prevents cycles)
     * Step 4: If valid, create node, add to queue and mark visited
     * 
     * @param current - Current node we're expanding from
     * @param newRow - Row of neighbor to explore
     * @param newCol - Column of neighbor to explore
     * @param queue - The BFS queue
     * @param visited - Set of already visited nodes
     */
    private void exploreNeighbor(PathfindingNode current, int newRow, int newCol,
                                  Queue<PathfindingNode> queue, HashSet<String> visited) {
        
        // STEP 1: Bounds checking
        // PRESENTATION POINT: Prevent array out of bounds errors
        // GRAPH THEORY: Ensure node is within graph boundaries
        if (newRow < 0 || newRow >= GamePanel.rowCount || 
            newCol < 0 || newCol >= GamePanel.columnCount) {
            return;  // Out of bounds, skip this neighbor
        }
        
        // STEP 2: Wall checking
        // PRESENTATION POINT: Obstacle avoidance
        // GRAPH THEORY: Skip BLOCKED NODES (walls)
        if (isWall(newRow, newCol)) {
            return;  // Can't move through walls, skip
        }
        
        // STEP 3: Check if already visited
        // PRESENTATION POINT: String-based visited check for better reliability
        // GRAPH THEORY: Prevent cycles - don't explore same node twice
        String posKey = newRow + "," + newCol;
        if (visited.contains(posKey)) {
            return;  // Already explored, skip
        }
        
        // STEP 4: Valid neighbor found! Create node, add to queue and mark visited
        // PRESENTATION POINT: This expands the search frontier (the ripple)
        // GRAPH THEORY: Add new node to queue with parent link for backtracking
        PathfindingNode neighbor = new PathfindingNode(newRow, newCol, current);
        queue.offer(neighbor);      // Add to end of queue (expand the ripple)
        visited.add(posKey);        // Mark as visited (don't revisit)
    }
    
    /**
     * Check if a grid position contains a wall
     * 
     * PRESENTATION POINT:
     * - Converts grid coordinates back to pixel coordinates
     * - Checks all walls in game
     * - Linear search: O(number of walls)
     * 
     * POTENTIAL OPTIMIZATION:
     * - Could use 2D boolean array for O(1) lookup
     * - Trade memory for speed
     * 
     * @param row - Grid row to check
     * @param col - Grid column to check
     * @return true if wall exists at position
     */
    private boolean isWall(int row, int col) {
        // Convert grid coordinates to pixel coordinates
        int pixelX = col * gp.tileSize;
        int pixelY = row * gp.tileSize;
        
        // Check all walls in the game
        for (Entity wall : gp.walls) {
            if (wall.x == pixelX && wall.y == pixelY) {
                return true;  // Wall found
            }
        }
        return false;  // No wall at this position
    }
    
    /**
     * Reconstruct path by following parent links backward
     * 
     * PRESENTATION POINTS:
     * 1. BACKTRACKING - Follow parent links from target to start
     * 2. PATH REVERSAL - Build path backward, then reverse
     * 3. DIRECTION CALCULATION - Convert position changes to directions
     * 
     * GRAPH THEORY:
     * - Each node stores its PARENT (where we came from)
     * - Following parent links traces the SHORTEST PATH backward
     * - This is the BACKTRACKING phase mentioned in BFS description
     * - We traverse from TARGET NODE (P - Yellow) back to SOURCE NODE (G - Red)
     * 
     * ALGORITHM STEPS:
     * Step 1: Start at target node (Pacman - Yellow Node)
     * Step 2: Follow parent link to previous node
     * Step 3: Calculate direction moved (current - parent)
     * Step 4: Add direction to front of list (builds path in reverse)
     * Step 5: Repeat until reaching start node (Ghost - Red Node, parent = null)
     * 
     * WHY THIS WORKS:
     * - Each node stores parent (where we came from in the BFS search)
     * - Following parents traces shortest path backward
     * - Adding to front reverses the path automatically
     * - Result: Path from Ghost to Pacman with optimal directions
     * 
     * @param targetNode - The node representing Pacman's position (Yellow Node)
     * @return PathLinkedList with directions from Ghost to Pacman
     */
    private PathLinkedList reconstructPath(PathfindingNode targetNode) {
        PathLinkedList path = new PathLinkedList();
        PathfindingNode current = targetNode;
        
        // Traverse backward through parent links
        while (current.parent != null) {
            PathfindingNode parent = current.parent;
            
            // Calculate which direction we moved from parent to current
            char direction = getDirection(parent, current);
            
            // Add to FRONT of list (since we're going backward)
            // PRESENTATION POINT: This reverses the path automatically
            path.addFirst(direction);
            
            // Move to parent
            current = parent;
        }
        
        return path;
    }
    
    /**
     * Calculate direction moved between two adjacent nodes
     * 
     * PRESENTATION POINT:
     * - Vector subtraction: to - from = direction
     * - Row/Col differences map to U/D/L/R
     * 
     * MAPPING:
     * - Row decreased (-1): Moved Up
     * - Row increased (+1): Moved Down
     * - Col decreased (-1): Moved Left
     * - Col increased (+1): Moved Right
     * 
     * @param from - Starting node
     * @param to - Destination node
     * @return Direction character ('U', 'D', 'L', or 'R')
     */
    private char getDirection(PathfindingNode from, PathfindingNode to) {
        int rowDiff = to.row - from.row;
        int colDiff = to.col - from.col;
        
        if (rowDiff == -1) return 'U';  // Moved up
        if (rowDiff == 1) return 'D';   // Moved down
        if (colDiff == -1) return 'L';  // Moved left
        if (colDiff == 1) return 'R';   // Moved right
        
        return 'R';  // Default (should never reach here)
    }
}
