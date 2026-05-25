package objects;

import java.awt.*;
import Main.GamePanel;

/**
 * Cổng dịch chuyển sang màn tiếp theo.
 * Vẽ procedurally – xoáy tím/cyan nhấp nháy.
 */
public class Portal extends SuperObject {

    public int targetLevel;
    private long birthTime = System.currentTimeMillis();

    public Portal(int targetLevel) {
        this.targetLevel = targetLevel;
        name        = "Portal";
        description = "Cổng dịch chuyển → Màn " + targetLevel;
        collision   = false;
        solidArea   = new java.awt.Rectangle(4, 4, 40, 40);
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        int sx = worldX - gp.cameraX;
        int sy = worldY - gp.cameraY;
        int ts = gp.tileSize;
        if (sx + ts < 0 || sx > gp.width || sy + ts < 0 || sy > gp.depth) return;

        double t    = (System.currentTimeMillis() - birthTime) / 1000.0;
        float pulse = (float)(Math.sin(t * 3) * 0.2 + 0.8);
        float spin  = (float)(t * 80);  // degrees

        // Outer glow
        g2.setColor(new Color(80, 0, 180, (int)(60 * pulse)));
        g2.fillOval(sx - 2, sy - 2, ts + 4, ts + 4);

        // Body
        g2.setColor(new Color(60, 0, 160, (int)(200 * pulse)));
        g2.fillOval(sx + 4, sy + 4, 40, 40);

        // Rotating arc
        Graphics2D g3 = (Graphics2D) g2.create();
        g3.rotate(Math.toRadians(spin), sx + 24, sy + 24);
        g3.setColor(new Color(0, 210, 255, (int)(160 * pulse)));
        g3.setStroke(new BasicStroke(3));
        g3.drawArc(sx + 8, sy + 8, 32, 32, 0, 270);
        g3.setColor(new Color(200, 100, 255, (int)(140 * pulse)));
        g3.drawArc(sx + 8, sy + 8, 32, 32, 90, 200);
        g3.dispose();

        // Center
        g2.setColor(new Color(180, 140, 255, (int)(220 * pulse)));
        g2.fillOval(sx + 16, sy + 16, 16, 16);

        // Label
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String label = "Màn " + targetLevel;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, sx + (ts - fm.stringWidth(label)) / 2, sy - 4);
    }
}
