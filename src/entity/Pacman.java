package entity;

import java.awt.*;
import main.GamePanel;
import java.awt.event.KeyEvent;

/**
 * Pacman - Player-controlled character
 * 
 * PRESENTATION POINTS:
 * 1. INHERITANCE - Extends Entity base class
 * 2. USER INPUT HANDLING - Responds to keyboard controls
 * 3. SMOOTH TURNING - nextDirection allows queued turns
 * 4. ANIMATION - Different images for each direction
 * 5. COLLISION PREVENTION - Can't move through walls
 * 
 * KEY DIFFERENCE FROM GHOST:
 * - Pacman is controlled by player input
 * - Ghosts use random/AI algorithms
 */
public class Pacman extends Entity {

    // Current direction Pacman is moving
    public char direction = 'R';

    // Next direction requested by player (allows buffering inputs)
    private char nextDirection = 'R';

    // Velocity components (pixels per frame)
    public int xVelocity = 0;
    public int yVelocity = 0;

    // Movement speed
    private int speed = 8; // tileSize / 4

// Animation images for each direction
private Image up, down, left, right;

/**
 * Handle keyboard input for Pacman movement
 * 
 * @param e - KeyEvent containing the pressed key
 */
public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
        case KeyEvent.VK_W:
            setDirection('U');
            break;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S:
            setDirection('D');
            break;
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
            setDirection('L');
            break;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
            setDirection('R');
            break;
    }
}

    /**
     * Constructor - Create Pacman
     * 
     * PRESENTATION POINT:
     * - Loads all 4 directional images from GamePanel
     * - Starts facing right (classic Pacman)
     * - Speed calculated for smooth grid movement
     * 
     * @param gp     - Game panel reference
     * @param x      - Starting X position
     * @param y      - Starting Y position
     * @param width  - Pacman width
     * @param height - Pacman height
     */
    public Pacman(GamePanel gp, int x, int y, int width, int height) {
        super(gp, x, y, width, height);

        // Load all directional images for animation
        this.up = gp.pacmanUpImg;
        this.down = gp.pacmanDownImg;
        this.left = gp.pacmanLeftImg;
        this.right = gp.pacmanRightImg;

        // Start with right-facing image (default)
        this.img = right;

        // Calculate speed based on tile size for grid alignment
        this.speed = gp.tileSize / 4;
    }

    /**
     * Set the next direction for Pacman to turn
     * 
     * PRESENTATION POINT:
     * - INPUT BUFFERING - Stores player's input for next valid turn
     * - Called by KeyboardInputs class when player presses arrow key
     * - Allows smooth cornering even if button pressed slightly early
     * 
     * @param dir - Direction character from keyboard input
     */
    public void setDirection(char dir) {
        this.nextDirection = dir;
    }

    /**
     * Update Pacman's position each frame
     * 
     * PRESENTATION POINTS:
     * 1. TWO-PHASE COLLISION DETECTION
     * Phase 1: Test if next direction is valid
     * Phase 2: Move in current direction with collision check
     * 
     * 2. LOOK-AHEAD TESTING - Tests movement before committing
     * 
     * 3. BACKTRACKING - Undoes invalid moves
     * 
     * ALGORITHM FLOW:
     * Step 1: Try to turn in the nextDirection
     * Step 2: Test if that direction hits a wall (look-ahead)
     * Step 3: If valid, accept the turn; otherwise keep current direction
     * Step 4: Move in current direction
     * Step 5: If hit wall, backtrack
     * Step 6: Update animation image
     */
    public void update() {
        // === PHASE 1: Test if we can turn in the requested direction ===

        // Calculate velocity for the requested next direction
        updateVelocity(nextDirection);

        // TEST MOVE: Temporarily move in that direction
        x += xVelocity;
        y += yVelocity;

        // Check for collisions with this test move
        boolean colWithNewDir = false;
        for (Entity wall : gp.walls) {
            if (gp.collision(this, wall)) {
                colWithNewDir = true; // Would hit a wall!
                break;
            }
        }

        // UNDO the test move (backtrack to original position)
        x -= xVelocity;
        y -= yVelocity;

        // If no collision detected, accept the turn
        if (!colWithNewDir) {
            this.direction = nextDirection;
        }

        // === PHASE 2: Move in the current direction ===

        // Set velocity based on current direction
        updateVelocity(this.direction);

        // Actually move Pacman
        this.x += xVelocity;
        this.y += yVelocity;

        // Check for collision with walls
        for (Entity wall : gp.walls) {
            if (gp.collision(this, wall)) {
                // Hit a wall! Step back
                this.x -= xVelocity;
                this.y -= yVelocity;
                break;
            }
        }

        // Update the displayed image based on current direction
        updateImage();
    }

    /**
     * Update velocity based on direction
     * 
     * PRESENTATION POINT:
     * - VECTOR MATHEMATICS - Converts direction to velocity components
     * - 2D movement represented as (x, y) vector
     * - Coordinate system: (0,0) at top-left, Y increases downward
     * 
     * @param dir - Direction character
     */
    private void updateVelocity(char dir) {
        switch (dir) {
            case 'U':
                xVelocity = 0;
                yVelocity = -speed;
                break; // Up: negative Y
            case 'D':
                xVelocity = 0;
                yVelocity = speed;
                break; // Down: positive Y
            case 'L':
                xVelocity = -speed;
                yVelocity = 0;
                break; // Left: negative X
            case 'R':
                xVelocity = speed;
                yVelocity = 0;
                break; // Right: positive X
        }
    }

    /**
     * Update the displayed image based on direction
     * 
     * PRESENTATION POINT:
     * - SPRITE ANIMATION - Change image to show direction
     * - Provides visual feedback to player
     * - Makes game more polished and intuitive
     */
    private void updateImage() {
        switch (direction) {
            case 'U':
                img = up;
                break; // Show up-facing Pacman
            case 'D':
                img = down;
                break; // Show down-facing Pacman
            case 'L':
                img = left;
                break; // Show left-facing Pacman
            case 'R':
                img = right;
                break; // Show right-facing Pacman
        }
    }

    /**
     * Reset Pacman to starting state
     * 
     * PRESENTATION POINT:
     * - METHOD OVERRIDING - Extends parent reset() with Pacman-specific resets
     * - Resets position, direction, velocity, and animation
     * - Called when player loses a life or restarts game
     */
    @Override
    public void reset() {
        super.reset(); // Reset position (from Entity)
        this.direction = 'R'; // Face right (default)
        this.nextDirection = 'R'; // Clear buffered input
        this.img = right; // Show right-facing image
        this.xVelocity = 0; // Stop moving
        this.yVelocity = 0; // Stop moving
    }
}