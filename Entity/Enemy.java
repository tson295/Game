package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import Main.GamePanel;

/**
 * Kẻ thù đơn giản với AI đuổi theo người chơi.
 * type 0 = Slime (xanh lá), type 1 = Skeleton (xám)
 */
public class Enemy extends Entity {

    public GamePanel gp;
    public int type;
    public int hitTimer = 0;       // Nháy đỏ khi bị đánh
    public int attackCooldown = 0; // Tránh dame liên tục

    // Bộ đếm animation đơn giản
    private int animFrame = 0;
    private int animCounter = 0;

    public Enemy(GamePanel gp, int type) {
        this.gp = gp;
        this.type = type;

        if (type == 0) {           // Slime
            maxHp = 4; hp = 4;
            attack = 1; defense = 0;
            speed = 1;
        } else {                   // Skeleton
            maxHp = 6; hp = 6;
            attack = 2; defense = 1;
            speed = 2;
        }

        solidArea = new Rectangle(6, 6, 36, 36);
        direction = "down";
    }

    public void update() {
        if (!alive) return;

        if (hitTimer > 0) hitTimer--;
        if (attackCooldown > 0) attackCooldown--;

        int px = gp.player.worldX;
        int py = gp.player.worldY;
        double dist = Math.hypot(px - worldX, py - worldY);

        // ── Chỉ rượt khi trong tầm aggro ──
        if (dist < 350 && dist > 5) {
            double angle = Math.atan2(py - worldY, px - worldX);
            int dx = (int) Math.round(Math.cos(angle) * speed);
            int dy = (int) Math.round(Math.sin(angle) * speed);

            // Cập nhật hướng hiển thị
            if (Math.abs(dx) >= Math.abs(dy)) {
                direction = dx > 0 ? "right" : "left";
            } else {
                direction = dy > 0 ? "down" : "up";
            }

            // Di chuyển từng trục (tránh kẹt góc)
            Rectangle boxNow = new Rectangle(
                worldX + solidArea.x, worldY + solidArea.y,
                solidArea.width, solidArea.height);

            if (dx != 0) {
                Rectangle nx = new Rectangle(boxNow); nx.x += dx;
                if (!gp.collisionCheck.isBlocked(nx)) {
                    worldX += dx;
                    boxNow.x += dx;
                }
            }
            if (dy != 0) {
                Rectangle ny = new Rectangle(boxNow); ny.y += dy;
                if (!gp.collisionCheck.isBlocked(ny)) {
                    worldY += dy;
                }
            }

            // Animation bounce
            animCounter++;
            if (animCounter > 12) { animFrame = 1 - animFrame; animCounter = 0; }
        }

        // ── Gây dame khi chạm người chơi ──
        if (attackCooldown == 0 && gp.player.alive) {
            Rectangle playerBox = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width, gp.player.solidArea.height);
            Rectangle myBox = new Rectangle(
                worldX + solidArea.x, worldY + solidArea.y,
                solidArea.width, solidArea.height);

            if (playerBox.intersects(myBox) && gp.player.invincibleTimer == 0) {
                int dmg = Math.max(1, attack - gp.player.defense);
                gp.player.hp -= dmg;
                gp.player.invincibleTimer = 90;   // 1.5 giây bất tử
                attackCooldown = 60;
                Main.Sound.play("hit");
            }
        }
    }

    public void draw(Graphics2D g2) {
        int sx = worldX - gp.cameraX;
        int sy = worldY - gp.cameraY;
        int ts = gp.tileSize; // 48

        // Culling
        if (sx + ts < 0 || sx > gp.width || sy + ts < 0 || sy > gp.depth) return;

        // Nháy đỏ khi bị đánh
        if (hitTimer > 0 && (hitTimer / 3) % 2 == 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        if (type == 0) drawSlime(g2, sx, sy);
        else           drawSkeleton(g2, sx, sy);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        drawHpBar(g2, sx, sy);
    }

    // ── Slime: hình tròn xanh ──
    private void drawSlime(Graphics2D g2, int sx, int sy) {
        int bounce = animFrame * 2;

        // Thân
        g2.setColor(new Color(30, 180, 90));
        g2.fillOval(sx + 4, sy + 10 - bounce, 40, 34 + bounce);

        // Viền
        g2.setColor(new Color(10, 120, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(sx + 4, sy + 10 - bounce, 40, 34 + bounce);
        g2.setStroke(new BasicStroke(1));

        // Mắt
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 12, sy + 16, 10, 10);
        g2.fillOval(sx + 28, sy + 16, 10, 10);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 15, sy + 19, 4, 4);
        g2.fillOval(sx + 31, sy + 19, 4, 4);

        // Miệng
        g2.setColor(new Color(10, 80, 40));
        g2.drawArc(sx + 14, sy + 28, 20, 8, 0, -180);
    }

    // ── Skeleton: hình người xương ──
    private void drawSkeleton(Graphics2D g2, int sx, int sy) {
        g2.setColor(new Color(210, 205, 195));

        // Đầu
        g2.fillOval(sx + 12, sy + 2, 24, 22);
        g2.setColor(new Color(140, 135, 125));
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(sx + 12, sy + 2, 24, 22);

        // Hốc mắt
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 16, sy + 8, 7, 7);
        g2.fillOval(sx + 26, sy + 8, 7, 7);

        // Xương hàm
        g2.setColor(new Color(210, 205, 195));
        for (int i = 0; i < 4; i++) {
            g2.fillRect(sx + 16 + i * 5, sy + 20, 3, 5);
        }

        // Thân
        g2.setColor(new Color(195, 190, 180));
        g2.fillRect(sx + 16, sy + 24, 16, 14);

        // Tay + chân (animation nhẹ)
        int legSwing = animFrame == 0 ? -3 : 3;
        g2.setColor(new Color(210, 205, 195));
        g2.fillRoundRect(sx + 8,  sy + 24 + legSwing, 6, 14, 4, 4);  // tay trái
        g2.fillRoundRect(sx + 34, sy + 24 - legSwing, 6, 14, 4, 4);  // tay phải
        g2.fillRoundRect(sx + 16, sy + 38 - legSwing, 7, 10, 4, 4);  // chân trái
        g2.fillRoundRect(sx + 25, sy + 38 + legSwing, 7, 10, 4, 4);  // chân phải
    }

    private void drawHpBar(Graphics2D g2, int sx, int sy) {
        int bw = 40, bh = 5;
        int bx = sx + 4, by = sy - 10;
        // Nền
        g2.setColor(new Color(60, 0, 0));
        g2.fillRect(bx, by, bw, bh);
        // Máu còn lại
        int filled = (int)((double) hp / maxHp * bw);
        g2.setColor(hp > maxHp / 2 ? new Color(50, 200, 50) : new Color(220, 60, 60));
        g2.fillRect(bx, by, filled, bh);
        // Viền
        g2.setColor(Color.BLACK);
        g2.drawRect(bx, by, bw, bh);
    }
}
