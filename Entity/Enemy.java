package Entity;

import java.awt.*;
import Main.GamePanel;
import Main.Sound;

/**
 * Kẻ thù với 4 loại:
 *  0 = Slime    (xanh lá, cận chiến, yếu)
 *  1 = Skeleton (xám, cận chiến, trung bình)
 *  2 = Archer   (nâu, bắn đạn từ xa)
 *  3 = Boss     (tím, HP rất cao, charge attack)
 */
public class Enemy extends Entity {

    public GamePanel gp;
    public int type;
    public int xpReward;

    // ── Timers ──
    public int hitTimer      = 0;   // nháy đỏ khi bị đánh
    public int attackCooldown = 0;  // chống dame liên tục
    private int shootTimer   = 0;   // Archer: đếm ngược bắn đạn
    private int bossTimer    = 0;   // Boss: đếm ngược charge
    private boolean charging = false;
    private int chargeDx, chargeDy, chargeFrames;

    // ── Animation ──
    private int animFrame = 0, animCounter = 0;

    // ─────────────────────────────────────────────────────────────────
    public Enemy(GamePanel gp, int type) {
        this.gp   = gp;
        this.type = type;
        direction = "down";

        switch (type) {
            case 0 -> { // Slime
                maxHp = 4; hp = 4; attack = 1; defense = 0; speed = 1; xpReward = 5;
                solidArea = new Rectangle(6, 10, 36, 32);
            }
            case 1 -> { // Skeleton
                maxHp = 6; hp = 6; attack = 2; defense = 1; speed = 2; xpReward = 10;
                solidArea = new Rectangle(6, 6, 36, 36);
            }
            case 2 -> { // Archer
                maxHp = 8; hp = 8; attack = 3; defense = 0; speed = 1; xpReward = 15;
                solidArea = new Rectangle(8, 8, 32, 32);
            }
            case 3 -> { // Boss
                maxHp = 80; hp = 80; attack = 5; defense = 2; speed = 2; xpReward = 100;
                solidArea = new Rectangle(4, 4, 40, 40);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    public void update() {
        if (!alive) return;
        if (hitTimer > 0)       hitTimer--;
        if (attackCooldown > 0) attackCooldown--;
        if (shootTimer > 0)     shootTimer--;
        if (bossTimer > 0)      bossTimer--;

        int px = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width  / 2;
        int py = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;
        int mx = worldX + solidArea.x + solidArea.width  / 2;
        int my = worldY + solidArea.y + solidArea.height / 2;
        double dist = Math.hypot(px - mx, py - my);

        switch (type) {
            case 0, 1 -> updateMelee(px, py, dist);
            case 2    -> updateArcher(px, py, dist);
            case 3    -> updateBoss(px, py, dist);
        }

        // Animation
        animCounter++;
        if (animCounter > 12) { animFrame = 1 - animFrame; animCounter = 0; }

        // Gây dame khi chạm player
        damagePlayerOnContact();
    }

    private void updateMelee(int px, int py, double dist) {
        if (dist > 400) return;

        double angle = Math.atan2(py - (worldY + solidArea.y), px - (worldX + solidArea.x));
        int dx = (int) Math.round(Math.cos(angle) * speed);
        int dy = (int) Math.round(Math.sin(angle) * speed);
        updateDirection(dx, dy);
        moveWithCollision(dx, dy);
    }

    private void updateArcher(int px, int py, double dist) {
        if (dist > 600) return;

        // Giữ khoảng cách ~200px
        double angle = Math.atan2(py - (worldY + solidArea.y), px - (worldX + solidArea.x));
        if (dist > 220) {
            int dx = (int) Math.round(Math.cos(angle) * speed);
            int dy = (int) Math.round(Math.sin(angle) * speed);
            updateDirection(dx, dy);
            moveWithCollision(dx, dy);
        } else if (dist < 150) {
            // Lùi ra xa
            int dx = -(int) Math.round(Math.cos(angle) * speed);
            int dy = -(int) Math.round(Math.sin(angle) * speed);
            updateDirection(-dx, -dy);  // hướng vẫn nhìn về phía player
            direction = angle > -Math.PI/2 && angle < Math.PI/2 ? "right" : "left";
            moveWithCollision(dx, dy);
        }

        // Bắn đạn nâu
        if (shootTimer <= 0 && dist < 500 && gp.player.alive) {
            shootTimer = 100;
            int ex = worldX + solidArea.x + solidArea.width  / 2;
            int ey = worldY + solidArea.y + solidArea.height / 2;
            gp.addProjectile(new Projectile(ex, ey, px, py, 5, attack,
                new java.awt.Color(139, 90, 43), 5));
            Sound.play("shoot");
        }
    }

    private void updateBoss(int px, int py, double dist) {
        if (dist > 800) return;

        if (charging) {
            // Lao về phía đã chọn
            Rectangle nx = new Rectangle(worldX + solidArea.x + chargeDx,
                                         worldY + solidArea.y, solidArea.width, solidArea.height);
            Rectangle ny = new Rectangle(worldX + solidArea.x,
                                         worldY + solidArea.y + chargeDy, solidArea.width, solidArea.height);
            if (!gp.collisionCheck.isBlocked(nx)) worldX += chargeDx;
            if (!gp.collisionCheck.isBlocked(ny)) worldY += chargeDy;
            chargeFrames--;
            if (chargeFrames <= 0) { charging = false; bossTimer = 180; }

        } else if (bossTimer <= 0) {
            // Chuẩn bị charge
            double angle = Math.atan2(py - (worldY + solidArea.y), px - (worldX + solidArea.x));
            int cs = 7;
            chargeDx    = (int) Math.round(Math.cos(angle) * cs);
            chargeDy    = (int) Math.round(Math.sin(angle) * cs);
            chargeFrames = 35;
            charging    = true;
            Sound.play("attack");
        } else {
            // Di chuyển thông thường
            double angle = Math.atan2(py - (worldY + solidArea.y), px - (worldX + solidArea.x));
            int dx = (int) Math.round(Math.cos(angle) * speed);
            int dy = (int) Math.round(Math.sin(angle) * speed);
            updateDirection(dx, dy);
            moveWithCollision(dx, dy);
        }

        // Phase 2: bắn đạn khi HP < 50%
        if (hp < maxHp / 2 && shootTimer <= 0 && gp.player.alive) {
            shootTimer = 70;
            int ex = worldX + solidArea.x + solidArea.width  / 2;
            int ey = worldY + solidArea.y + solidArea.height / 2;
            // Bắn theo 3 hướng
            for (int angle = 0; angle < 360; angle += 120) {
                double rad = Math.toRadians(angle);
                int tx = px + (int)(Math.cos(rad) * 100);
                int ty = py + (int)(Math.sin(rad) * 100);
                gp.addProjectile(new Projectile(ex, ey, tx, ty, 4, 3,
                    new java.awt.Color(180, 0, 255), 6));
            }
        }
    }

    private void updateDirection(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) direction = dx >= 0 ? "right" : "left";
        else                               direction = dy >= 0 ? "down"  : "up";
    }

    private void moveWithCollision(int dx, int dy) {
        Rectangle boxNow = new Rectangle(worldX + solidArea.x, worldY + solidArea.y,
                                         solidArea.width, solidArea.height);
        if (dx != 0) {
            Rectangle nx = new Rectangle(boxNow); nx.x += dx;
            if (!gp.collisionCheck.isBlocked(nx)) { worldX += dx; boxNow.x += dx; }
        }
        if (dy != 0) {
            Rectangle ny = new Rectangle(boxNow); ny.y += dy;
            if (!gp.collisionCheck.isBlocked(ny)) worldY += dy;
        }
    }

    private void damagePlayerOnContact() {
        if (attackCooldown > 0 || !gp.player.alive) return;
        Rectangle pb = new Rectangle(gp.player.worldX + gp.player.solidArea.x,
                                     gp.player.worldY + gp.player.solidArea.y,
                                     gp.player.solidArea.width, gp.player.solidArea.height);
        Rectangle eb = new Rectangle(worldX + solidArea.x, worldY + solidArea.y,
                                     solidArea.width, solidArea.height);
        if (pb.intersects(eb) && gp.player.invincibleTimer == 0) {
            int dmg = Math.max(1, attack - gp.player.effectiveDefense());
            gp.player.hp -= dmg;
            gp.player.invincibleTimer = 90;
            attackCooldown = 60;
            Sound.play("hit");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int sx = worldX - gp.cameraX;
        int sy = worldY - gp.cameraY;
        int ts = gp.tileSize;

        if (sx + ts < -20 || sx > gp.width + 20 || sy + ts < -20 || sy > gp.depth + 20) return;

        // Hit flash
        if (hitTimer > 0 && (hitTimer / 3) % 2 == 0)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

        switch (type) {
            case 0 -> drawSlime(g2, sx, sy);
            case 1 -> drawSkeleton(g2, sx, sy);
            case 2 -> drawArcher(g2, sx, sy);
            case 3 -> drawBoss(g2, sx, sy);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        drawHpBar(g2, sx, sy);
    }

    private void drawSlime(Graphics2D g2, int sx, int sy) {
        int bounce = animFrame * 3;
        g2.setColor(new Color(30, 180, 90));
        g2.fillOval(sx + 4, sy + 12 - bounce, 40, 32 + bounce);
        g2.setColor(new Color(10, 120, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(sx + 4, sy + 12 - bounce, 40, 32 + bounce);
        g2.setStroke(new BasicStroke(1));
        // Eyes
        g2.setColor(Color.WHITE);
        g2.fillOval(sx + 12, sy + 17, 10, 10);
        g2.fillOval(sx + 27, sy + 17, 10, 10);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 15, sy + 20, 4, 4);
        g2.fillOval(sx + 30, sy + 20, 4, 4);
        g2.setColor(new Color(10, 80, 40));
        g2.drawArc(sx + 15, sy + 29, 18, 7, 0, -180);
    }

    private void drawSkeleton(Graphics2D g2, int sx, int sy) {
        int legSwing = animFrame == 0 ? -3 : 3;
        g2.setColor(new Color(210, 205, 195));
        g2.fillOval(sx + 12, sy + 2, 24, 22);
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 16, sy + 8, 7, 7);
        g2.fillOval(sx + 26, sy + 8, 7, 7);
        g2.setColor(new Color(210, 205, 195));
        for (int i = 0; i < 4; i++) g2.fillRect(sx + 16 + i * 5, sy + 20, 3, 5);
        g2.fillRect(sx + 16, sy + 24, 16, 14);
        g2.fillRoundRect(sx + 8,  sy + 24 + legSwing, 6, 14, 4, 4);
        g2.fillRoundRect(sx + 34, sy + 24 - legSwing, 6, 14, 4, 4);
        g2.fillRoundRect(sx + 16, sy + 38 - legSwing, 7, 10, 4, 4);
        g2.fillRoundRect(sx + 25, sy + 38 + legSwing, 7, 10, 4, 4);
    }

    private void drawArcher(Graphics2D g2, int sx, int sy) {
        int legSwing = animFrame == 0 ? -2 : 2;
        // Thân (xanh lục đậm)
        g2.setColor(new Color(40, 100, 40));
        g2.fillRoundRect(sx + 13, sy + 22, 22, 20, 6, 6);
        // Đầu
        g2.setColor(new Color(220, 180, 140));
        g2.fillOval(sx + 14, sy + 6, 20, 18);
        // Mũi
        g2.setColor(new Color(60, 100, 60));
        g2.fillOval(sx + 12, sy + 4, 24, 10);
        // Mắt
        g2.setColor(Color.BLACK);
        g2.fillOval(sx + 17, sy + 11, 3, 3);
        g2.fillOval(sx + 28, sy + 11, 3, 3);
        // Cung (bên phải)
        g2.setColor(new Color(100, 60, 20));
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(sx + 34, sy + 10, 10, 26, -80, 160);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(200, 180, 140));
        g2.drawLine(sx + 39, sy + 12, sx + 39, sy + 34);
        // Chân
        g2.setColor(new Color(60, 40, 20));
        g2.fillRoundRect(sx + 16, sy + 40 - legSwing, 8, 10, 3, 3);
        g2.fillRoundRect(sx + 24, sy + 40 + legSwing, 8, 10, 3, 3);
    }

    private void drawBoss(Graphics2D g2, int sx, int sy) {
        // Charge effect: màu đỏ cam
        Color bodyColor = charging ? new Color(220, 60, 20) : new Color(90, 20, 140);
        Color armorColor = charging ? new Color(180, 40, 0)  : new Color(60, 0, 100);

        // Thân to hơn (~1.4x)
        g2.setColor(bodyColor);
        g2.fillRoundRect(sx + 6, sy + 22, 36, 24, 8, 8);
        // Giáp
        g2.setColor(armorColor);
        g2.fillRoundRect(sx + 8, sy + 22, 32, 18, 6, 6);
        // Đầu
        g2.setColor(bodyColor);
        g2.fillOval(sx + 10, sy + 2, 28, 24);
        // Vương miện / sừng
        g2.setColor(new Color(180, 150, 0));
        g2.fillRect(sx + 10, sy + 2, 4, 10);
        g2.fillRect(sx + 22, sy - 2, 4, 12);
        g2.fillRect(sx + 34, sy + 2, 4, 10);
        // Mắt đỏ phát sáng
        g2.setColor(new Color(255, 30, 30));
        g2.fillOval(sx + 14, sy + 8, 8, 8);
        g2.fillOval(sx + 26, sy + 8, 8, 8);
        g2.setColor(new Color(255, 200, 0));
        g2.fillOval(sx + 16, sy + 10, 4, 4);
        g2.fillOval(sx + 28, sy + 10, 4, 4);
        // Tay
        g2.setColor(armorColor);
        g2.fillRoundRect(sx, sy + 24, 8, 18, 4, 4);
        g2.fillRoundRect(sx + 40, sy + 24, 8, 18, 4, 4);
        // Chân
        g2.setColor(bodyColor);
        g2.fillRoundRect(sx + 12, sy + 43, 10, 10, 4, 4);
        g2.fillRoundRect(sx + 26, sy + 43, 10, 10, 4, 4);
        // Hiệu ứng phase 2 (ánh sáng tím)
        if (hp < maxHp / 2) {
            g2.setColor(new Color(180, 0, 255, 80));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(sx - 4, sy - 4, 56, 56);
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawHpBar(Graphics2D g2, int sx, int sy) {
        int bw = (type == 3) ? 80 : 40;
        int bh = (type == 3) ? 7  : 5;
        int bx = sx + (gp.tileSize - bw) / 2;
        int by = sy - 12;

        g2.setColor(new Color(60, 0, 0));
        g2.fillRect(bx, by, bw, bh);

        int filled = Math.max(0, (int)((double) hp / maxHp * bw));
        Color bar  = hp > maxHp * 0.5 ? new Color(50, 210, 50) : new Color(230, 60, 60);
        g2.setColor(bar);
        g2.fillRect(bx, by, filled, bh);

        g2.setColor(Color.BLACK);
        g2.drawRect(bx, by, bw, bh);

        // Boss: show HP number
        if (type == 3) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            String txt = hp + "/" + maxHp;
            g2.drawString(txt, bx + 2, by - 1);
        }
    }
}
