package Main;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Overlay tối quanh màn hình, chỉ sáng vùng xung quanh player.
 * Khi trang bị Lantern, bán kính ánh sáng tăng lên.
 */
public class LightingSystem {

    private final GamePanel gp;
    private BufferedImage overlay;   // tái sử dụng mỗi frame
    public boolean active = true;

    private static final int BASE_RADIUS    = 180;
    private static final int LANTERN_BONUS  = 160;
    private static final int DARKNESS_ALPHA = 210;

    public LightingSystem(GamePanel gp) {
        this.gp = gp;
        overlay = new BufferedImage(gp.width, gp.depth, BufferedImage.TYPE_INT_ARGB);
    }

    public int getLightRadius() {
        boolean hasLantern = gp.player.equippedLantern != null;
        return BASE_RADIUS + (hasLantern ? LANTERN_BONUS : 0);
    }

    public void draw(Graphics2D g2) {
        if (!active) return;

        int cx = gp.player.worldX - gp.cameraX + gp.tileSize / 2;
        int cy = gp.player.worldY - gp.cameraY + gp.tileSize / 2;
        int r  = getLightRadius();

        Graphics2D og = overlay.createGraphics();
        og.setComposite(AlphaComposite.Src);
        og.setColor(new Color(0, 0, 0, DARKNESS_ALPHA));
        og.fillRect(0, 0, gp.width, gp.depth);

        // Punch-out: tạo vùng sáng với gradient trong suốt
        og.setComposite(AlphaComposite.DstOut);
        float[] fracs  = {0.0f, 0.55f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, DARKNESS_ALPHA),   // trung tâm: xóa hết alpha
            new Color(0, 0, 0, 120),
            new Color(0, 0, 0, 0)                  // rìa: không xóa
        };
        RadialGradientPaint paint = new RadialGradientPaint(cx, cy, r, fracs, colors);
        og.setPaint(paint);
        og.fillOval(cx - r, cy - r, r * 2, r * 2);
        og.dispose();

        g2.drawImage(overlay, 0, 0, null);
    }
}
