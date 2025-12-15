package entity;

import java.awt.*;
import java.util.Random;
import main.GamePanel;

/**
 * Ghost - Basic enemy AI with RANDOM movement
 * 
 * PRESENTATION POINTS:
 * 1. INHERITANCE - Extends Entity base class
 * 2. RANDOM AI - Unpredictable movement pattern
 * 3. COLLISION DETECTION - Bounces off walls
 * 
 * AI BEHAVIOR:
 * - RANDOM MODE: Moves randomly in four directions
 *   - Changes direction when hitting walls
 *   - Unpredictable but not intelligent
 *   - Basic ghost behavior
 */
public class Ghost extends Entity {

    // Current direction the ghost is moving ('U'=Up, 'D'=Down, 'L'=Left, 'R'=Right)
    public char direction = 'U';
    
    // Velocity in pixels per frame
    public int xVelocity = 0;  // Horizontal speed
    public int yVelocity = 0;  // Vertical speed
    
    // Movement speed (pixels per frame)
    protected int speed = 8;
    
    // Array of possible directions for random selection
    protected char[] directions = {'U', 'D', 'L', 'R'};
    
    // Random number generator for AI decision-making
    protected Random random = new Random();

    /**
     * Constructor - Create a new ghost with random movement
     * 
     * PRESENTATION POINTS:
     * - Calls parent constructor using super()
     * - Speed calculated as tileSize/4 for smooth grid movement
     * - Starts with random direction
     * 
     * @param gp - Game panel reference
     * @param x - Starting X position
     * @param y - Starting Y position
     * @param width - Ghost width
     * @param height - Ghost height
     * @param img - Ghost image (different colors for each ghost)
     */
    public Ghost(GamePanel gp, int x, int y, int width, int height, Image img) {
        super(gp, x, y, width, height);  // Call parent constructor
        this.img = img;
        this.speed = gp.tileSize / 4;  // Speed = 8 pixels (32/4) per frame
        randomDir();  // Start moving in random direction
    }

    /**
     * Update ghost position with random AI
     * 
     * PRESENTATION POINTS:
     * 1. RANDOM MOVEMENT - Move in current direction
     * 2. COLLISION DETECTION - Handle walls and boundaries
     * 
     * ALGORITHM FLOW:
     * Step 1: Apply velocity to position
     * Step 2: Handle special spawn area rules
     * Step 3: Check for collisions and pick new direction if needed
     */
    public void update() {
        
        // === STEP 1: APPLY MOVEMENT ===
        this.x += xVelocity;
        this.y += yVelocity;

        // === STEP 2: SPECIAL RULES ===
        // Force downward movement at spawn area
        if (direction != 'U' && direction != 'D' && y == 32 * 9) {
            updateDir('D');
        }
        
        // === STEP 3: COLLISION DETECTION ===
        for (Entity wall : gp.walls) {
            if (gp.collision(wall, this) || this.x <= 0 || this.x + this.width >= gp.WIDTH) {
                // Backtrack on collision
                this.x -= xVelocity;
                this.y -= yVelocity;
                
                // Pick new random direction
                randomDir();
                break;
            }
        }
    }

    /**
     * Choose a random direction
     * 
     * PRESENTATION POINTS:
     * 1. RANDOM ALGORITHM - random.nextInt(4) generates 0-3
     * 2. ARRAY INDEXING - Use random number to pick from array
     * 3. Creates unpredictable ghost behavior
     * 
     * DATA STRUCTURE: Array {'U', 'D', 'L', 'R'}
     * - Index 0 = 'U' (Up)
     * - Index 1 = 'D' (Down)
     * - Index 2 = 'L' (Left)
     * - Index 3 = 'R' (Right)
     */
    public void randomDir() {
        char newDir = directions[random.nextInt(4)];  // Random index: 0, 1, 2, or 3
        updateDir(newDir);  // Apply the new direction
    }

    /**
     * Update ghost's direction and velocity
     * 
     * PRESENTATION POINTS:
     * 1. SWITCH STATEMENT - Clean way to handle multiple cases
     * 2. VECTOR MATHEMATICS - Velocity as (x, y) components
     * 3. Direction mapping:
     *    - Up: (0, -speed) - negative Y moves upward
     *    - Down: (0, +speed) - positive Y moves downward
     *    - Left: (-speed, 0) - negative X moves left
     *    - Right: (+speed, 0) - positive X moves right
     * 
     * @param newDir - The new direction character
     */
    public void updateDir(char newDir) {
        this.direction = newDir;
        switch (direction) {
            case 'U': xVelocity = 0; yVelocity = -speed; break;  // Move up
            case 'D': xVelocity = 0; yVelocity = speed; break;   // Move down
            case 'L': xVelocity = -speed; yVelocity = 0; break;  // Move left
            case 'R': xVelocity = speed; yVelocity = 0; break;   // Move right
        }
    }

    /**
     * Reset ghost to starting position and state
     * 
     * PRESENTATION POINT:
     * - METHOD OVERRIDING - Extends parent's reset() method
     * - Calls super.reset() first, then adds ghost-specific behavior
     */
    @Override
    public void reset() {
        super.reset();           // Reset position (from Entity class)
        randomDir();             // Choose new random direction
    }
}
