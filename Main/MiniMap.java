package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import objects.*;

/**
 * Bản đồ thu nhỏ hiển thị ở góc trên-phải.
 * Toggle bằng phím M.
 */
public class MiniMap {

    private final GamePanel gp;
    private BufferedImage tileCache;   // cache tile colors – rebuild khi đổi level
    private boolean dirty = true;

    public boolean visible = false;

    private static final int MM_W = 160;  // pixel trên màn hình
    private static final int MM_H = 120;

    public MiniMap(GamePanel gp) { this.gp = gp; }

    public void markDirty() { dirty = true; }

    // ── Build tile color cache ────────────────────────────────────────
    private void buildCache() {
        int cols = gp.maxWorldCol, rows = gp.maxWorldRow;
        tileCache = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = tileCache.createGraphics();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int id = gp.tilesManager.mapTile[r][c];
                cg.setColor(tileColor(id));
                cg.fillRect(c, r, 1, 1);
            }
        }
        cg.dispose();
        dirty = false;
    }

    private Color tileColor(int id) {
        return switch (id) {
            case 1       -> new Color(80, 70, 50);
            case 3, 5    -> new Color(100, 95, 85);
            case 16      -> new Color(55, 55, 55);
            case 18, 19  -> new Color(30, 55, 160);
            case 32      -> new Color(65, 60, 55);
            case 38      -> new Color(20, 90, 20);
            default      -> new Color(60, 55, 48);
        };
    }

    // ── Draw ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        if (!visible) return;
        if (dirty) buildCache();

        int mx = gp.width - MM_W - 8;
        int my = 8;

        // Background
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(mx - 3, my - 3, MM_W + 6, MM_H + 22, 8, 8);

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("MAP  [M]", mx + 3, my + MM_H + 13);

        // Tiles (stretched)
        g2.drawImage(tileCache, mx, my, MM_W, MM_H, null);

        float sx = (float) MM_W / gp.maxWorldCol;
        float sy = (float) MM_H / gp.maxWorldRow;

        // Objects
        for (SuperObject o : gp.obj) {
            if (o == null) continue;
            float ox = mx + (float) o.worldX / gp.tileSize * sx;
            float oy = my + (float) o.worldY / gp.tileSize * sy;
            if      (o instanceof Portal)  g2.setColor(Color.CYAN);
            else if (o instanceof Chest)   g2.setColor(Color.YELLOW);
            else if (o instanceof NPC)     g2.setColor(new Color(100, 200, 255));
            else if (o instanceof Key)     g2.setColor(new Color(255, 220, 0));
            else continue;
            g2.fillRect((int) ox - 1, (int) oy - 1, 3, 3);
        }

        // Enemies
        g2.setColor(new Color(220, 50, 50));
        for (Entity.Enemy e : gp.enemies) {
            if (e == null || !e.alive) continue;
            float ex = mx + (float) e.worldX / gp.tileSize * sx;
            float ey = my + (float) e.worldY / gp.tileSize * sy;
            if (e.type == 3) { g2.setColor(new Color(180, 0, 255)); g2.fillRect((int)ex-2,(int)ey-2,5,5); g2.setColor(new Color(220,50,50)); }
            else              g2.fillRect((int)ex-1, (int)ey-1, 3, 3);
        }

        // Player (white blinking dot)
        if ((System.currentTimeMillis() / 400) % 2 == 0) {
            float px = mx + (float) gp.player.worldX / gp.tileSize * sx;
            float py = my + (float) gp.player.worldY / gp.tileSize * sy;
            g2.setColor(Color.WHITE);
            g2.fillOval((int) px - 3, (int) py - 3, 6, 6);
        }

        // Border
        g2.setColor(new Color(100, 100, 120));
        g2.drawRect(mx, my, MM_W, MM_H);
    }
}
