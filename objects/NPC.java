package objects;

import java.awt.*;
import Main.GamePanel;

/**
 * Thương nhân NPC – nhấn E để mở cửa hàng.
 * Vẽ procedurally (không cần sprite).
 */
public class NPC extends SuperObject {

    private long birthTime = System.currentTimeMillis();

    public NPC() {
        name        = "Merchant";
        description = "Thương nhân – [E] Mua đồ";
        collision   = false;
        solidArea   = new Rectangle(8, 8, 32, 32);
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        int sx = worldX - gp.cameraX;
        int sy = worldY - gp.cameraY;
        int ts = gp.tileSize;
        if (sx + ts < 0 || sx > gp.width || sy + ts < 0 || sy > gp.depth) return;

        double t      = (System.currentTimeMillis() - birthTime) / 1000.0;
        int    bounce = (int)(Math.sin(t * 2.5) * 3);

        // Áo choàng (nâu đỏ)
        g2.setColor(new Color(120, 60, 30));
        int[] rx = {sx + 10, sx + 38, sx + 34, sx + 14};
        int[] ry = {sy + 22 - bounce, sy + 22 - bounce, sy + 44 - bounce, sy + 44 - bounce};
        g2.fillPolygon(rx, ry, 4);

        // Đầu (da)
        g2.setColor(new Color(240, 195, 145));
        g2.fillOval(sx + 13, sy + 4 - bounce, 22, 20);

        // Mũ nhọn (tím)
        g2.setColor(new Color(90, 30, 130));
        int[] hx = {sx + 11, sx + 24, sx + 37};
        int[] hy = {sy + 16 - bounce, sy - 6 - bounce, sy + 16 - bounce};
        g2.fillPolygon(hx, hy, 3);
        g2.setColor(new Color(60, 0, 100));
        g2.drawPolygon(hx, hy, 3);

        // Mắt
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 17, sy + 10 - bounce, 3, 3);
        g2.fillOval(sx + 28, sy + 10 - bounce, 3, 3);

        // Nụ cười
        g2.setColor(new Color(160, 90, 70));
        g2.drawArc(sx + 18, sy + 16 - bounce, 12, 5, 0, -180);

        // "!" nhấp nháy
        float alpha = (float)(Math.sin(t * 4) * 0.3 + 0.7);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(Color.YELLOW);
        g2.drawString("!", sx + 22, sy - 8 - bounce);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
