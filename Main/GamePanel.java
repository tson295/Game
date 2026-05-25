package Main;

import java.awt.*;
import javax.swing.JPanel;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectile;
import objects.SetObjects;
import objects.SuperObject;
import tiles.TilesManager;

public class GamePanel extends JPanel implements Runnable {

    // ── Screen / world constants ──────────────────────────────────────
    public static final int PIXEL  = 16;
    public static final int SCALE  = 3;
    public final int realPixel     = PIXEL * SCALE;  // 48 px
    public final int tileSize      = realPixel;
    public final int maxColPixel   = 16;
    public final int maxRowPixel   = 12;
    public final int width         = tileSize * maxColPixel;   // 768
    public final int depth         = tileSize * maxRowPixel;   // 576
    public final int maxWorldCol   = 50;
    public final int maxWorldRow   = 50;
    public final int worldWidth    = tileSize * maxWorldCol;
    public final int worldDepth    = tileSize * maxWorldRow;
    public static final int MAX_LEVEL = 3;

    // ── Camera ────────────────────────────────────────────────────────
    public int cameraX, cameraY;

    // ── Systems ───────────────────────────────────────────────────────
    Thread gameThread;
    public Move            keyB           = new Move();
    public CollisionCheck  collisionCheck = new CollisionCheck(this);
    public TilesManager    tilesManager   = new TilesManager(this);
    public SetObjects      setObjs        = new SetObjects(this);

    // ── Game objects ─────────────────────────────────────────────────
    public Player       player;
    public SuperObject[] obj         = new SuperObject[40];
    public Enemy[]       enemies     = new Enemy[30];
    public Projectile[]  projectiles = new Projectile[60];

    // ── UI systems ───────────────────────────────────────────────────
    public UI             ui;
    public ShopUI         shopUI;
    public MiniMap        miniMap;
    public LightingSystem lighting;

    // ── State ────────────────────────────────────────────────────────
    public GameState gameState   = GameState.TITLE;
    public int       currentLevel = 1;

    // ─────────────────────────────────────────────────────────────────
    public GamePanel() {
        this.setPreferredSize(new Dimension(width, depth));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyB);
        this.setFocusable(true);

        player   = new Player(this, keyB);
        ui       = new UI(this);
        shopUI   = new ShopUI(this);
        miniMap  = new MiniMap(this);
        lighting = new LightingSystem(this);
    }

    public void setupGame() {
        setObjs.setObjectsForLevel(currentLevel);
        setObjs.setEnemiesForLevel(currentLevel);
    }

    /** Đặt lại hoàn toàn (new game) */
    public void resetGame() {
        currentLevel = 1;
        obj          = new SuperObject[40];
        enemies      = new Enemy[30];
        projectiles  = new Projectile[60];
        player       = new Player(this, keyB);
        ui           = new UI(this);
        shopUI       = new ShopUI(this);
        miniMap      = new MiniMap(this);
        lighting     = new LightingSystem(this);
        tilesManager.loadMap("/map/test.txt");
        setupGame();
        gameState = GameState.PLAYING;
    }

    /** Chuyển màn – giữ nguyên player stats */
    public void loadLevel(int level) {
        currentLevel = level;
        obj          = new SuperObject[40];
        enemies      = new Enemy[30];
        projectiles  = new Projectile[60];
        miniMap.markDirty();

        String mapFile = switch (level) {
            case 2  -> "/map/level2.txt";
            case 3  -> "/map/level3.txt";
            default -> "/map/test.txt";
        };
        tilesManager.loadMap(mapFile);

        setObjs.setObjectsForLevel(level);
        setObjs.setEnemiesForLevel(level);

        // Spawn lại player ở điểm xuất phát
        player.worldX = tileSize * 5;
        player.worldY = tileSize * 5;
        player.direction = "down";
        gameState = GameState.PLAYING;
    }

    // ── Projectile factory ───────────────────────────────────────────
    public void addProjectile(Projectile p) {
        for (int i = 0; i < projectiles.length; i++) {
            if (projectiles[i] == null || !projectiles[i].alive) {
                projectiles[i] = p;
                return;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double NS = 1_000_000_000.0 / 60;
        double delta = 0;
        long last    = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - last) / NS;
            last   = now;
            if (delta >= 1) { update(); repaint(); delta--; }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    public void update() {
        ui.update();

        switch (gameState) {
            case TITLE -> {
                if (keyB.enterPressed) {
                    keyB.enterPressed = false;
                    String[] opts = SaveManager.hasSave()
                        ? new String[]{"new", "continue", "quit"}
                        : new String[]{"new", "quit"};
                    int c = ui.getTitleCursor();
                    String action = (c < opts.length) ? opts[c] : "quit";
                    switch (action) {
                        case "new"      -> resetGame();
                        case "continue" -> SaveManager.load(this);
                        case "quit"     -> System.exit(0);
                    }
                }
                if (keyB.escPressed) { keyB.escPressed = false; System.exit(0); }
            }

            case PLAYING -> {
                player.update();
                for (Enemy e : enemies) { if (e != null && e.alive) e.update(); }
                for (int i = 0; i < projectiles.length; i++) {
                    if (projectiles[i] != null) {
                        if (projectiles[i].alive) projectiles[i].update(this);
                        else                      projectiles[i] = null;
                    }
                }

                // Camera
                int dcx = player.worldX - width  / 2 + tileSize / 2;
                int dcy = player.worldY - depth  / 2 + tileSize / 2;
                cameraX = Math.max(0, Math.min(dcx, worldWidth  - width));
                cameraY = Math.max(0, Math.min(dcy, worldDepth - depth));

                // Keys
                if (keyB.escPressed)  { keyB.escPressed  = false; gameState = GameState.PAUSED; }
                if (keyB.iPressed)    { keyB.iPressed     = false; gameState = GameState.INVENTORY; }
                if (keyB.mPressed)    { keyB.mPressed     = false; miniMap.visible = !miniMap.visible; }
            }

            case PAUSED -> {
                if (keyB.escPressed) { keyB.escPressed = false; gameState = GameState.PLAYING; }
                if (keyB.qPressed)   { keyB.qPressed   = false; gameState = GameState.TITLE; }
                if (keyB.enterPressed) {
                    keyB.enterPressed = false;
                    SaveManager.save(this);
                    ui.showMessage("Game đã được lưu! 💾");
                    Sound.play("save");
                    gameState = GameState.PLAYING;
                }
            }

            case INVENTORY -> {
                if (keyB.escPressed) { keyB.escPressed = false; gameState = GameState.PLAYING; }
                if (keyB.iPressed)   { keyB.iPressed   = false; gameState = GameState.PLAYING; }
            }

            case SHOP -> {
                // handled by shopUI.update() inside ui.update()
            }

            case GAME_OVER -> {
                if (keyB.restartPressed) { keyB.restartPressed = false; SaveManager.deleteSave(); resetGame(); }
                if (keyB.escPressed)     { keyB.escPressed     = false; gameState = GameState.TITLE; }
            }

            case WIN -> {
                if (keyB.restartPressed) { keyB.restartPressed = false; SaveManager.deleteSave(); resetGame(); }
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
            ui.draw(g2);
        } else {
            // 1. Tiles
            tilesManager.draw(g2);

            // 2. Objects
            for (SuperObject o : obj) { if (o != null) o.draw(g2, this); }

            // 3. Projectiles
            for (Projectile p : projectiles) { if (p != null && p.alive) p.draw(g2, this); }

            // 4. Enemies
            for (Enemy e : enemies) { if (e != null && e.alive) e.draw(g2); }

            // 5. Player
            player.draw(g2);

            // 6. Lighting overlay (darkness with light hole)
            lighting.draw(g2);

            // 7. Mini-map (above lighting)
            miniMap.draw(g2);

            // 8. HUD / UI (always on top)
            ui.draw(g2);
        }

        g2.dispose();
    }
}
