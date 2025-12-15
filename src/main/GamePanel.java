package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

import entity.Entity;
import entity.Ghost;
import entity.Pacman;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements ActionListener {

    HighScoreManager scoreManager = new HighScoreManager();
    boolean isScoreSaved = false;

    public static final int rowCount = 21;
    public static final int columnCount = 19;
    public static final int tileSize = 32;
    public static final int WIDTH = columnCount * tileSize;
    public static final int HEIGHT = rowCount * tileSize;

    private final Image wallImageLvl1;
    private final Image wallImageLvl2Normal;
    private final Image wallImageLvl2Tu;

    public Image pacmanUpImg, pacmanDownImg, pacmanLeftImg, pacmanRightImg;
    private final Image blueGhostImg, redGhostImg, pinkGhostImg, orangeGhostImg;

    public HashSet<Entity> walls;
    public HashSet<Entity> foods;
    public HashSet<Ghost> ghosts;
    public HashSet<Entity> wallTu;

    public Pacman pacman;

    Timer gameLoop;
    public Random random = new Random();

    int score = 0;
    int lives = 3;

    public int currentLevel = 1;
    public static boolean gameOver = false;
    public static boolean gameWon = false;

    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X X r X X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    private String[] tileMap2 = {
            "XXXXXXXXXXXXXXXXXXX",
            "XP      XOOOX     X",
            "X XXXXX XOOOX XXX X",
            "X XXXXX XXXXX XXX X",
            "X             X   X",
            "X XXXXX XXX X X X O",
            "X   X   XXX X X X X",
            "X X X X     X X X X",
            "X X X X X X X   X X",
            "X X X X X X XXXXX X",
            "X     X X  opr    X",
            "X X X X XXX b X   X",
            "X X       X XXXXX X",
            "X X XXXXX X       X",
            "X   X     X XXX X X",
            "X X X X X X     X X",
            "X X X X X X XXXXX X",
            "X X   X           O",
            "X X XXXXX XX XXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        wallImageLvl1 = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/wall.png"))).getImage();
        wallImageLvl2Normal = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/wallLvl2Normal.png"))).getImage();
        wallImageLvl2Tu = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/wallLvl2Tu.png"))).getImage();
        pacmanUpImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacmanUp.png"))).getImage();
        pacmanDownImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacmanDown.png"))).getImage();
        pacmanLeftImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacmanLeft.png"))).getImage();
        pacmanRightImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacmanRight.png"))).getImage();
        blueGhostImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/blueGhost.png"))).getImage();
        redGhostImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/redGhost.png"))).getImage();
        pinkGhostImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pinkGhost.png"))).getImage();
        orangeGhostImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/orangeGhost.png"))).getImage();

        loadMap();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    resetGame();
                } else if (!gameOver && pacman != null) {
                    pacman.keyPressed(e);
                }
            }
        });
        this.setFocusable(true);
        this.requestFocusInWindow();

        gameLoop = new Timer(50, this);
    }

    public void loadMap() {
        loadMapData(tileMap, wallImageLvl1, null);
    }

    public void loadMap2() {
        loadMapData(tileMap2, wallImageLvl2Normal, wallImageLvl2Tu);
    }

    private void loadMapData(String[] mapData, Image wallImg, Image specialWallImg) {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();
        wallTu = new HashSet<>();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                String rows = mapData[row];
                char tileMapChar = rows.charAt(col);
                int x = col * tileSize;
                int y = row * tileSize;

                if (tileMapChar == 'X') {
                    Entity wall = new Entity(this, x, y, tileSize, tileSize);
                    wall.img = wallImg;
                    walls.add(wall);
                } else if (tileMapChar == 'Y') {
                    Entity wall = new Entity(this, x, y, tileSize, tileSize);
                    wall.img = specialWallImg;
                    walls.add(wall);
                } else if (tileMapChar == 'b') {
                    ghosts.add(new Ghost(this, x, y, tileSize, tileSize, blueGhostImg));
                } else if (tileMapChar == 'r') {
                    ghosts.add(new Ghost(this, x, y, tileSize, tileSize, redGhostImg));
                } else if (tileMapChar == 'o') {
                    ghosts.add(new Ghost(this, x, y, tileSize, tileSize, orangeGhostImg));
                } else if (tileMapChar == 'p') {
                    ghosts.add(new Ghost(this, x, y, tileSize, tileSize, pinkGhostImg));
                } else if (tileMapChar == 'P') {
                    pacman = new Pacman(this, x, y, tileSize, tileSize);
                } else if (tileMapChar == ' ') {
                    Entity food = new Entity(this, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (gameOver) { // both win and lose

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setFont(new Font("Arial", Font.BOLD, 40));
            String text = ""; // placeholder

            if (gameWon) {
                g.setColor(Color.GREEN);
                text = "YOU WIN!";
            } else {
                g.setColor(Color.RED);
                text = "GAME OVER";
            }

            g.drawString(text, 180, HEIGHT / 2 - 100);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String subText = "Press SPACE to play again";
            g.drawString(subText, 200, HEIGHT / 2 - 50);

            g.setColor(Color.YELLOW);
            g.drawString("Score: " + score, 200, HEIGHT / 2); // x=200, y=336

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("--- TOP HIGHSCORES ---", 170, HEIGHT / 2 + 50);

            int y = HEIGHT / 2 + 80;
            for (Score s : scoreManager.scoreList) {
                g.drawString(s.name + " : " + s.point, 200, y);
                y += 30;
            }

        } else {
            pacman.draw(g);
            for (Ghost ghost : ghosts) ghost.draw(g);
            for (Entity wall : walls) wall.draw(g);
            g.setColor(Color.YELLOW);
            for (Entity food : foods) g.fillRect(food.x, food.y, food.width, food.height);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.WHITE);
            g.drawString("Level: " + currentLevel + "  Lives: " + lives + "  Score: " + score, tileSize / 2, tileSize / 2);
        }
    }

    public void update() {
        pacman.update();
        for (Ghost ghost : ghosts) {
            ghost.update();
            if (collision(ghost, pacman)) {
                lives--;
                if (lives == 0) {
                    gameOver = true;
                    gameWon = false;
                    return;
                }
                resetPosition();
            }
        }

        checkTeleport();

        Entity foodEaten = null;
        for (Entity food : foods) {
            if (collision(food, pacman)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            if (currentLevel == 1) {
                currentLevel = 2;
                loadMap2();
                resetPosition();
            } else if (currentLevel == 2) {
                gameOver = true;
                gameWon = true;
            }
        }
    }

    private void checkTeleport() {
        if (pacman.direction == 'R' && pacman.y == tileSize * 9 && pacman.x + pacman.width >= WIDTH) {
            pacman.x = 0;
        } else if (pacman.direction == 'L' && pacman.y == tileSize * 9 && pacman.x <= 0) {
            pacman.x = tileSize * 19;
        } else if (pacman.direction == 'R' && pacman.y == tileSize * 5 && pacman.x + pacman.width >= WIDTH) {
            pacman.x = tileSize * 19;
            pacman.y = tileSize * 17;
            pacman.setDirection('L');
        } else if (pacman.direction == 'R' && pacman.y == tileSize * 17 && pacman.x + pacman.width >= WIDTH) {
            pacman.x = tileSize * 19;
            pacman.y = tileSize * 5;
            pacman.setDirection('L');
        }
    }

    public void resetPosition() {
        pacman.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
    }

    public void resetGame() {
        score = 0;
        lives = 3;
        currentLevel = 1;
        gameOver = false;
        gameWon = false;
        isScoreSaved = false;
        loadMap();
        gameLoop.start();
    }

    public boolean collision(Entity a, Entity b) {
        return a.getBounds().intersects(b.getBounds());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            update();
            repaint();
        } else {
            checkAndSaveScore();
            repaint();
        }
    }

    public void startGame() {
        if (gameLoop != null && !gameLoop.isRunning()) {
            gameLoop.start();
        }
        requestFocusInWindow();
    }

    private void checkAndSaveScore() {
        if (gameOver && !isScoreSaved) {

            String name = JOptionPane.showInputDialog(this, "Game Over! Score: " + score + "\nEnter your name:");

            if (name == null || name.trim().isEmpty()) {
                name = "Null";
            }
            // add score to score manager
            scoreManager.addScore(name, score);

            // set to true --> not save again
            isScoreSaved = true;
        }
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setGameOver(boolean state) {
        gameOver = state;
    }

    public Timer getGameLoop() {
        return gameLoop;
    }

    public Pacman getPacman() {
        return pacman;
    }
}