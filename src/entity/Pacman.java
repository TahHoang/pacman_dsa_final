package entity;

import java.awt.*;
import java.awt.event.KeyEvent;
import main.GamePanel;

public class Pacman extends Entity {

    public char direction = 'R';
    private char nextDirection = 'R';
    public int xVelocity = 0;
    public int yVelocity = 0;
    private int speed = 8; // tileSize / 4

private Image up, down, left, right;

/**
 * Handle keyboard input
 * 
 * PRESENTATION POINT:
 * - USER INPUT HANDLING - Converts key presses to direction changes
 * - Supports both arrow keys and WASD controls
 * 
 * @param e - KeyEvent from keyboard
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

    public Pacman(GamePanel gp, int x, int y, int width, int height) {
        super(gp, x, y, width, height);

        this.up = gp.pacmanUpImg;
        this.down = gp.pacmanDownImg;
        this.left = gp.pacmanLeftImg;
        this.right = gp.pacmanRightImg;
        this.img = right;
        this.speed = gp.tileSize / 4;
    }

    public void setDirection(char dir) {
        this.nextDirection = dir;
    }

    public void update() {
        // Change the dir if valid (no collision with wall)
        updateVelocity(nextDirection);
        // Check if new dir is valid
        x += xVelocity;
        y += yVelocity;
        boolean colWithNewDir = false;
        for (Entity wall : gp.walls) {
            if (gp.collision(this, wall)) {
                colWithNewDir = true;
                break;
            }
        }
        // Undo move test
        x -= xVelocity;
        y -= yVelocity;

        if (!colWithNewDir) {
            this.direction = nextDirection;
        }

        // move with current dir
        updateVelocity(this.direction);
        this.x += xVelocity;
        this.y += yVelocity;

        // check for collision --> happen --> step back
        for (Entity wall : gp.walls) {
            if (gp.collision(this, wall)) {
                this.x -= xVelocity;
                this.y -= yVelocity;
                break;
            }
        }
        updateImage();
    }

    private void updateVelocity(char dir) {
        switch (dir) {
            case 'U': xVelocity = 0; yVelocity = -speed; break;
            case 'D': xVelocity = 0; yVelocity = speed; break;
            case 'L': xVelocity = -speed; yVelocity = 0; break;
            case 'R': xVelocity = speed; yVelocity = 0; break;
        }
    }

    private void updateImage() {
        switch (direction) {
            case 'U': img = up; break;
            case 'D': img = down; break;
            case 'L': img = left; break;
            case 'R': img = right; break;
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.direction = 'R';
        this.nextDirection = 'R';
        this.img = right;
        this.xVelocity = 0;
        this.yVelocity = 0;
    }
}