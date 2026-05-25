package Main;

import java.awt.*;
import javax.swing.JPanel;
import Entity.Enemy;
import Entity.Player;
import objects.SetObjects;
import objects.SuperObject;
import tiles.TilesManager;

public class GamePanel extends JPanel implements Runnable {

    // ── Cài đặt màn hình ──
    public static final int PIXEL     = 16;
    public static final int SCALE     = 3;
    public final int realPixel        = PIXEL * SCALE;   // 48 px
    public final int tileSize         = realPixel;
    public final int maxColPixel      = 16;
    public final int maxRowPixel      = 12;
    public final int width            = tileSize * maxColPixel;  // 768
    public final int depth            = tileSize * maxRowPixel;  // 576
    public final int maxWorldCol      = 50;
    public final int maxWorldRow      = 50;
    public final int worldWidth       = tileSize * maxWorldCol;
    public final int worldDepth       = tileSize * maxWorldRow;

    // ── Camera ──
    public int cameraX, cameraY;

    // ── Hệ thống ──
    Thread gameThread;
    public Move           keyB          = new Move();
    public CollisionCheck collisionCheck = new CollisionCheck(this);
    public TilesManager   tilesManager  = new TilesManager(this);
    public SetObjects     setObjs       = new SetObjects(this);

    // ── Thực thể ──
    public Player       player;
    public SuperObject[] obj     = new SuperObject[30];
    public Enemy[]       enemies = new Enemy[20];

    // ── UI ──
    public UI ui;

    // ── Trạng thái game ──
    public GameState gameState = GameState.TITLE;

    // ─────────────────────────────────────────────────────────────────
    public GamePanel() {
        this.setPreferredSize(new Dimension(width, depth));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyB);
        this.setFocusable(true);

        player = new Player(this, keyB);
        ui     = new UI(this);
    }

    public void setupGame() {
        setObjs.setObject();
        setObjs.setEnemies();
    }

    /** Khởi động lại game từ đầu */
    public void resetGame() {
        obj     = new SuperObject[30];
        enemies = new Enemy[20];
        player  = new Player(this, keyB);
        ui      = new UI(this);
        setupGame();
        gameState = GameState.PLAYING;
    }

    // ─────────────────────────────────────────────────────────────────
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double NS_PER_FRAME = 1_000_000_000.0 / 60;
        double delta = 0;
        long last    = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - last) / NS_PER_FRAME;
            last   = now;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    public void update() {
        ui.update();

        switch (gameState) {

            case TITLE -> {
                if (keyB.enterPressed) {
                    keyB.enterPressed = false;
                    if (ui.getTitleCursor() == 0) {
                        resetGame();
                    } else {
                        System.exit(0);
                    }
                }
                if (keyB.escPressed) { keyB.escPressed = false; System.exit(0); }
            }

            case PLAYING -> {
                player.update();
                for (Enemy e : enemies) { if (e != null && e.alive) e.update(); }

                // Camera theo player
                int dcx = player.worldX - width  / 2 + tileSize / 2;
                int dcy = player.worldY - depth  / 2 + tileSize / 2;
                cameraX = Math.max(0, Math.min(dcx, worldWidth  - width));
                cameraY = Math.max(0, Math.min(dcy, worldDepth - depth));

                if (keyB.escPressed) { keyB.escPressed = false; gameState = GameState.PAUSED; }
            }

            case PAUSED -> {
                if (keyB.escPressed) { keyB.escPressed = false; gameState = GameState.PLAYING; }
                if (keyB.qPressed)   { keyB.qPressed   = false; gameState = GameState.TITLE;   }
            }

            case GAME_OVER -> {
                if (keyB.restartPressed) { keyB.restartPressed = false; resetGame(); }
                if (keyB.escPressed)     { keyB.escPressed     = false; gameState = GameState.TITLE; }
            }

            case WIN -> {
                if (keyB.restartPressed) { keyB.restartPressed = false; resetGame(); }
                if (keyB.escPressed)     { keyB.escPressed     = false; gameState = GameState.TITLE; }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == GameState.TITLE) {
            // Chỉ vẽ UI title
            ui.draw(g2);
        } else {
            // Vẽ thế giới
            tilesManager.draw(g2);

            // Objects
            for (SuperObject o : obj) { if (o != null) o.draw(g2, this); }

            // Kẻ thù
            for (Enemy e : enemies) { if (e != null && e.alive) e.draw(g2); }

            // Player
            player.draw(g2);

            // UI chồng lên
            ui.draw(g2);
        }

        g2.dispose();
    }
}
