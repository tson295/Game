package Entity;

import java.awt.*;
import Main.GamePanel;
import Main.Sound;

/**
 * Mũi tên / đạn phép bắn từ kẻ thù, bay theo đường thẳng.
 */
public class Projectile {

    public double worldX, worldY;
    public double velX, velY;
    public int damage;
    public boolean alive = true;
    public int lifeTimer = 200; // tự biến mất sau n frame
    public Color color;
    public int radius;

    // Trail effect
    private double prevX, prevY;

    public Projectile(int fromX, int fromY, int toX, int toY,
                      int speed, int damage, Color color, int radius) {
        this.worldX  = fromX;
        this.worldY  = fromY;
        this.damage  = damage;
        this.color   = color;
        this.radius  = radius;
        double angle = Math.atan2(toY - fromY, toX - fromX);
        this.velX    = Math.cos(angle) * speed;
        this.velY    = Math.sin(angle) * speed;
        this.prevX   = fromX;
        this.prevY   = fromY;
    }

    public void update(GamePanel gp) {
        if (!alive) return;

        prevX = worldX;
        prevY = worldY;
        worldX += velX;
        worldY += velY;
        lifeTimer--;

        if (lifeTimer <= 0) { alive = false; return; }

        // Va chạm tile
        Rectangle box = getHitbox();
        if (gp.collisionCheck.isBlocked(box)) { alive = false; return; }

        // Va chạm với player
        if (gp.player.alive && gp.player.invincibleTimer == 0) {
            Rectangle pb = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width, gp.player.solidArea.height);
            if (box.intersects(pb)) {
                int dmg = Math.max(1, damage - gp.player.effectiveDefense());
                gp.player.hp -= dmg;
                gp.player.invincibleTimer = 60;
                Sound.play("hit");
                alive = false;
            }
        }
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        int sx = (int) worldX - gp.cameraX;
        int sy = (int) worldY - gp.cameraY;
        if (sx < -20 || sx > gp.width + 20 || sy < -20 || sy > gp.depth + 20) return;

        // Trail
        int tx = (int) prevX - gp.cameraX;
        int ty = (int) prevY - gp.cameraY;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2.setStroke(new BasicStroke(radius));
        g2.drawLine(tx, ty, sx, sy);
        g2.setStroke(new BasicStroke(1));

        // Core
        g2.setColor(color);
        g2.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);

        // Glow
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2.fillOval(sx - radius - 3, sy - radius - 3, radius * 2 + 6, radius * 2 + 6);
    }

    public Rectangle getHitbox() {
        return new Rectangle((int) worldX - radius, (int) worldY - radius,
                radius * 2, radius * 2);
    }
}
