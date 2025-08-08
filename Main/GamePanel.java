package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import Entity.Player;
import tiles.TilesManager;

public class GamePanel extends JPanel implements Runnable {
    // setting screen
    final int pixel = 16;
    final int scale = 3;
    public final int realPixel = pixel * scale;
    public final int maxRowPixel = 12;
    public final int maxColPixel = 16;
    public final int width = realPixel * maxColPixel; // 768
    public final int depth = realPixel * maxRowPixel;// 576

    Thread gameThread;
    Move keyB = new Move();
    Player player = new Player(this, keyB);
    TilesManager tilesManager = new TilesManager(this);

    // contrustor
    public GamePanel() {
        this.setPreferredSize(new Dimension(width, depth));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyB);
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / 60; // 60 FPS
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        player.update(); // Update other game logic here if needed
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        tilesManager.draw(g2);
        player.draw(g2);
        g2.dispose();
    }
}
