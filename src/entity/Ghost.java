package entity;

import java.awt.*;
import java.util.Random;
import main.GamePanel;

public class Ghost extends Entity {

    public char direction = 'U';
    public int xVelocity = 0;
    public int yVelocity = 0;
    private int speed = 8;
    private char[] directions = {'U', 'D', 'L', 'R'};
    private Random random = new Random();
    
    // BFS pathfinding for red ghost
    private boolean isRedGhost = false;
    private BFSPathfinder pathfinder;
    private PathLinkedList currentPath;
    private int pathUpdateCounter = 0;
    private static final int PATH_UPDATE_INTERVAL = 20; // Recalculate path every 20 frames

    public Ghost(GamePanel gp, int x, int y, int width, int height, Image img) {
        super(gp, x, y, width, height);
        this.img = img;
        this.speed = gp.tileSize / 4;
        randomDir();
    }
    
    public Ghost(GamePanel gp, int x, int y, int width, int height, Image img, boolean isRed) {
        super(gp, x, y, width, height);
        this.img = img;
        this.isRedGhost = isRed;

        // Base speed for ghosts
        int baseSpeed = gp.tileSize / 4;
        // Make red ghost noticeably slower (exact divisor of tileSize to stay on grid)
        this.speed = isRed ? Math.max(2, gp.tileSize / 8) : baseSpeed; // 0.5x base
        
        if (isRedGhost) {
            pathfinder = new BFSPathfinder(gp);
            currentPath = new PathLinkedList();
            // Start with initial direction
            updateDir('R');
        } else {
            randomDir();
        }
    }

    /**
     * Update ghost position and behavior.
     * 
     * PRESENTATION POINT:
     * - RED GHOST uses BFS pathfinding to intelligently chase Pacman
     * - Other ghosts use random movement
     */
    public void update() {
        // All ghosts: snap slightly to grid along the axis they're travelling to avoid corner jitter
        int snapThreshold = 2; // pixels
        if (direction == 'L' || direction == 'R') {
            int modY = this.y % GamePanel.tileSize;
            if (modY <= snapThreshold || modY >= GamePanel.tileSize - snapThreshold) {
                this.y = (this.y / GamePanel.tileSize) * GamePanel.tileSize;
            }
        } else if (direction == 'U' || direction == 'D') {
            int modX = this.x % GamePanel.tileSize;
            if (modX <= snapThreshold || modX >= GamePanel.tileSize - snapThreshold) {
                this.x = (this.x / GamePanel.tileSize) * GamePanel.tileSize;
            }
        }

        // Red ghost uses BFS pathfinding
        if (isRedGhost && gp.pacman != null) {
            PathLinkedList path = pathfinder.findPath(this.x, this.y, gp.pacman.x, gp.pacman.y);
            if (!path.isEmpty()) {
                updateDir(path.removeFirst());
            } else if (xVelocity == 0 && yVelocity == 0) {
                randomDir();
            }
        }
        
        // Move the ghost
        this.x += xVelocity;
        this.y += yVelocity;

        if (direction != 'U' && direction != 'D' && y == 32 * 9) {
            updateDir('D');
        }
        
        // Collision detection with walls
        for (Entity wall : gp.walls) {
            if (gp.collision(wall, this) || this.x <= 0 || this.x + this.width >= gp.WIDTH) {
                // step back
                this.x -= xVelocity;
                this.y -= yVelocity;
                
                if (!isRedGhost) {
                    randomDir();
                } else {
                    // Snap back to grid on collision then recalc path to avoid sticking
                    this.x = (this.x / GamePanel.tileSize) * GamePanel.tileSize;
                    this.y = (this.y / GamePanel.tileSize) * GamePanel.tileSize;
                    PathLinkedList path = pathfinder.findPath(this.x, this.y, gp.pacman.x, gp.pacman.y);
                    if (!path.isEmpty()) {
                        updateDir(path.removeFirst());
                    } else {
                        randomDir();
                    }
                }
                break;
            }
        }
    }

    public void randomDir() {
        char newDir = directions[random.nextInt(4)];
        updateDir(newDir);
    }

    public void updateDir(char newDir) {
        this.direction = newDir;
        switch (direction) {
            case 'U': xVelocity = 0; yVelocity = -speed; break;
            case 'D': xVelocity = 0; yVelocity = speed; break;
            case 'L': xVelocity = -speed; yVelocity = 0; break;
            case 'R': xVelocity = speed; yVelocity = 0; break;
        }
    }

    @Override
    public void reset() {
        super.reset();
        
        // Reset BFS pathfinding state for red ghost
        if (isRedGhost) {
            currentPath.clear();
            pathUpdateCounter = 0;
            updateDir('R'); // Start with right direction
        } else {
            randomDir();
        }
    }
}