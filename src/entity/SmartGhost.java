package entity;

import java.awt.*;
import main.GamePanel;

/**
 * SmartGhost - Intelligent enemy AI with PROXIMITY-BASED BFS pathfinding
 * 
 * PRESENTATION POINTS:
 * 1. INHERITANCE - Extends Ghost base class
 * 2. HYBRID AI - Combines random movement and intelligent pathfinding
 * 3. PROXIMITY DETECTION - Switches behavior based on distance to Pacman
 * 4. BFS ALGORITHM - Finds shortest path when chasing
 * 5. STATE-BASED BEHAVIOR - Two modes: RANDOM and CHASE
 * 
 * AI BEHAVIOR:
 * - RANDOM MODE: When Pacman is far (> 5 tiles away)
 *   - Moves randomly for unpredictability
 *   - Less threatening, gives player breathing room
 * 
 * - CHASE MODE: When Pacman is near (≤ 5 tiles away)
 *   - Uses BFS to find shortest path
 *   - Actively hunts player
 *   - Creates tension and challenge
 * 
 * GAME DESIGN BENEFIT:
 * - Dynamic difficulty adjustment
 * - Balanced gameplay (not too easy or hard)
 * - Rewards player for keeping distance
 */
public class SmartGhost extends Ghost {
    
    // === BFS PATHFINDING COMPONENTS ===
    
    // PRESENTATION POINT: These enable intelligent ghost behavior
    
    // The BFS algorithm instance
    private BFSPathfinder pathfinder;
    
    // Current path being followed (from BFS)
    private PathLinkedList currentPath;
    
    // Counter for when to recalculate path
    private int pathRecalculateCounter = 0;
    
    // Recalculate path every N frames (prevents constant recalculation)
    private static final int RECALCULATE_INTERVAL = 15;
    
    // === PROXIMITY DETECTION THRESHOLDS ===
    
    // PRESENTATION POINT: These control when ghost switches modes
    
    // Distance to START chasing (5 tiles = 160 pixels)
    // When Pacman gets this close, ghost starts BFS
    private static final int CHASE_DISTANCE = 5 * 32;  // 5 tiles
    
    // Distance to STOP chasing (6 tiles = 192 pixels)
    // When Pacman gets this far, ghost returns to random movement
    private static final int STOP_CHASE_DISTANCE = 6 * 32;  // 6 tiles
    
    // Current AI mode
    private boolean isChasing = false;
    
    /**
     * Constructor - Create a smart ghost with BFS pathfinding
     * 
     * PRESENTATION POINTS:
     * - Calls parent constructor using super()
     * - Initializes BFS pathfinder
     * - Starts in RANDOM mode (not chasing)
     * 
     * @param gp - Game panel reference
     * @param x - Starting X position
     * @param y - Starting Y position
     * @param width - Ghost width
     * @param height - Ghost height
     * @param img - Ghost image
     */
    public SmartGhost(GamePanel gp, int x, int y, int width, int height, Image img) {
        super(gp, x, y, width, height, img);  // Call parent constructor
        
        // Initialize BFS pathfinding system
        this.pathfinder = new BFSPathfinder(gp);
        this.currentPath = new PathLinkedList();
        this.isChasing = false;  // Start in random mode
    }
    
    /**
     * Update ghost position with proximity-based AI
     * 
     * PRESENTATION POINTS:
     * 1. PROXIMITY DETECTION - Calculate distance to Pacman
     * 2. MODE SWITCHING - Change between RANDOM and CHASE modes
     * 3. BFS PATHFINDING - When chasing, use shortest path
     * 4. COLLISION DETECTION - Handle walls and boundaries
     * 
     * ALGORITHM FLOW:
     * Step 1: Calculate distance to Pacman
     * Step 2: Update AI mode based on distance (chase or random)
     * Step 3: If chasing, use BFS path; otherwise move randomly
     * Step 4: Apply velocity to position
     * Step 5: Handle collisions and boundaries
     */
    @Override
    public void update() {
        
        // === STEP 1: PROXIMITY DETECTION ===
        // PRESENTATION POINT: Distance calculation using Pythagorean theorem
        
        if (gp.pacman != null) {
            double distanceToPacman = calculateDistance(gp.pacman);
            
            // === STEP 2: MODE SWITCHING ===
            // PRESENTATION POINT: Hysteresis prevents rapid mode switching
            
            if (!isChasing && distanceToPacman <= CHASE_DISTANCE) {
                // Pacman got close! Start chasing
                isChasing = true;
                currentPath.clear();  // Clear old path
                pathRecalculateCounter = RECALCULATE_INTERVAL;  // Force immediate recalculation
            }
            else if (isChasing && distanceToPacman > STOP_CHASE_DISTANCE) {
                // Pacman escaped! Stop chasing
                isChasing = false;
                currentPath.clear();  // Clear BFS path
                randomDir();  // Switch to random movement
            }
        }
        
        // === STEP 3: AI DECISION MAKING ===
        
        if (isChasing && gp.pacman != null) {
            // CHASE MODE: Use BFS pathfinding
            // PRESENTATION POINT: This is where BFS algorithm is used
            updateChaseMode();
        }
        // else: RANDOM MODE uses parent's random movement
        
        // === STEP 4: APPLY MOVEMENT ===
        this.x += xVelocity;
        this.y += yVelocity;

        // === STEP 5: SPECIAL RULES ===
        // Force downward movement at spawn area
        if (direction != 'U' && direction != 'D' && y == 32 * 9) {
            updateDir('D');
        }
        
        // === STEP 6: COLLISION DETECTION ===
        for (Entity wall : gp.walls) {
            if (gp.collision(wall, this) || this.x <= 0 || this.x + this.width >= gp.WIDTH) {
                // Backtrack on collision
                this.x -= xVelocity;
                this.y -= yVelocity;
                
                if (isChasing) {
                    // Path blocked! Recalculate immediately
                    currentPath.clear();
                    pathRecalculateCounter = RECALCULATE_INTERVAL;
                } else {
                    // Random mode: just pick new direction
                    randomDir();
                }
                break;
            }
        }
    }
    
    /**
     * Update ghost behavior when in CHASE mode (using BFS)
     * 
     * PRESENTATION POINTS:
     * 1. PERIODIC RECALCULATION - Don't recalculate every frame (expensive)
     * 2. PATH FOLLOWING - Use directions from BFS result
     * 3. SMOOTH MOVEMENT - Follow path step by step
     * 
     * ALGORITHM FLOW:
     * Step 1: Check if need to recalculate path (timer or empty path)
     * Step 2: If yes, run BFS to find new path
     * Step 3: Follow current path one step at a time
     */
    private void updateChaseMode() {
        pathRecalculateCounter++;
        
        // STEP 1: Decide when to recalculate path
        // PRESENTATION POINT: Trade-off between accuracy and performance
        
        if (pathRecalculateCounter >= RECALCULATE_INTERVAL || currentPath.isEmpty()) {
            
            // STEP 2: Run BFS algorithm
            // PRESENTATION POINT: This is the BFS function call
            
            PathLinkedList newPath = pathfinder.findPath(
                this.x, this.y,           // Ghost position (start)
                gp.pacman.x, gp.pacman.y  // Pacman position (target)
            );
            
            if (newPath != null && !newPath.isEmpty()) {
                currentPath = newPath;
            } else {
                // No path found (Pacman unreachable)
                // Fall back to random movement
                isChasing = false;
                randomDir();
            }
            
            pathRecalculateCounter = 0;  // Reset counter
        }
        
        // STEP 3: Follow the path
        // PRESENTATION POINT: Path is a sequence of directions
        
        if (!currentPath.isEmpty()) {
            // Get next direction from path
            char nextDir = currentPath.peek();
            
            // Check if we should change direction (grid-aligned or need to turn)
            if (isAlignedToGrid() || needsDirectionChange(nextDir)) {
                // Remove the current step and get next
                currentPath.removeFirst();
                if (!currentPath.isEmpty()) {
                    nextDir = currentPath.peek();
                    updateDir(nextDir);
                }
            }
        }
    }
    
    /**
     * Check if ghost needs to change direction based on next step in path
     * 
     * @param nextDir - Next direction from BFS path
     * @return true if current direction doesn't match next direction
     */
    private boolean needsDirectionChange(char nextDir) {
        return this.direction != nextDir;
    }
    
    /**
     * Calculate Euclidean distance to Pacman
     * 
     * PRESENTATION POINT:
     * - PYTHAGOREAN THEOREM: distance = √(dx² + dy²)
     * - Used for proximity detection
     * - Returns distance in pixels
     * 
     * FORMULA:
     * distance = √[(x₂ - x₁)² + (y₂ - y₁)²]
     * 
     * @param pacman - Pacman entity
     * @return Distance in pixels
     */
    private double calculateDistance(Pacman pacman) {
        int dx = pacman.x - this.x;  // Horizontal distance
        int dy = pacman.y - this.y;  // Vertical distance
        return Math.sqrt(dx * dx + dy * dy);  // Pythagorean theorem
    }
    
    /**
     * Check if ghost is aligned to grid
     * 
     * PRESENTATION POINT:
     * - Grid alignment means position is exactly on tile boundary
     * - Ensures smooth turns at intersections
     * - Both X and Y must be multiples of tileSize
     * 
     * @return true if at exact grid position
     */
    private boolean isAlignedToGrid() {
        return (this.x % gp.tileSize == 0) && (this.y % gp.tileSize == 0);
    }
    
    /**
     * Reset ghost to starting position and state
     * 
     * PRESENTATION POINT:
     * - METHOD OVERRIDING - Extends parent's reset() method
     * - Resets AI state (back to random mode)
     * - Clears any existing BFS path
     */
    @Override
    public void reset() {
        super.reset();           // Reset position (from Ghost class)
        this.isChasing = false;  // Return to random mode
        this.currentPath.clear(); // Clear any BFS path
        this.pathRecalculateCounter = 0;  // Reset counter
    }
}
