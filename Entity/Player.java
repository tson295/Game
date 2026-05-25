package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import Main.GamePanel;
import Main.GameState;
import Main.Move;
import Main.Sound;
import objects.*;

public class Player extends Entity {

    final GamePanel gp;
    final Move keyB;

    // Vị trí trên màn hình (luôn cố định ở giữa)
    public final int screenX, screenY;

    // Inventory
    public int keys = 0;

    // Combat
    public int invincibleTimer = 0;  // bất tử tạm thời sau khi bị đánh
    private int attackTimer    = 0;  // đếm ngược cooldown tấn công
    private static final int ATTACK_CD = 22;
    private boolean showAttack = false;

    // ─────────────────────────────────────────────────────────────────
    public Player(GamePanel gp, Move keyB) {
        this.gp   = gp;
        this.keyB = keyB;

        worldX   = gp.realPixel * 23;
        worldY   = gp.realPixel * 21;
        screenX  = gp.width  / 2 - gp.realPixel / 2;
        screenY  = gp.depth  / 2 - gp.realPixel / 2;
        direction = "down";

        maxHp = 6; hp = 6;
        attack  = 2;
        defense = 0;
        speed   = 3;

        solidArea = new Rectangle(8, 16, 32, 32);

        down  = new BufferedImage[7];
        up    = new BufferedImage[7];
        left  = new BufferedImage[7];
        right = new BufferedImage[7];
        loadSprites();
    }

    private void loadSprites() {
        try {
            for (int i = 0; i < 7; i++) {
                down[i]  = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 1)  + ".png"));
                up[i]    = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 8)  + ".png"));
                right[i] = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 15) + ".png"));
                left[i]  = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 22) + ".png"));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────
    public void update() {
        if (!alive) return;

        // ── Nhập input ──
        boolean moving = false;
        int dx = 0, dy = 0;
        if (keyB.up)    { dy -= speed; moving = true; }
        if (keyB.down)  { dy += speed; moving = true; }
        if (keyB.left)  { dx -= speed; moving = true; }
        if (keyB.right) { dx += speed; moving = true; }

        // Chuẩn hóa đi chéo
        if (dx != 0 && dy != 0) {
            double f = 1.0 / Math.sqrt(2);
            dx = (int) Math.round(dx * f);
            dy = (int) Math.round(dy * f);
        }

        // Cập nhật hướng nhìn
        if      (dy < 0) direction = "up";
        else if (dy > 0) direction = "down";
        else if (dx < 0) direction = "left";
        else if (dx > 0) direction = "right";

        // ── Di chuyển (kiểm tra va chạm từng trục) ──
        Rectangle box = currentBox();
        if (dx != 0) {
            Rectangle nx = new Rectangle(box); nx.x += dx;
            if (!gp.collisionCheck.isBlocked(nx) && !gp.collisionCheck.isBlockedByObject(nx)) {
                worldX += dx; box.x += dx;
            }
        }
        if (dy != 0) {
            Rectangle ny = new Rectangle(box); ny.y += dy;
            if (!gp.collisionCheck.isBlocked(ny) && !gp.collisionCheck.isBlockedByObject(ny)) {
                worldY += dy;
            }
        }

        // ── Animation ──
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 7) {
                spriteNum = (spriteNum % 6) + 1;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0;
        }

        // ── Bộ đếm ──
        if (invincibleTimer > 0) invincibleTimer--;
        if (attackTimer > 0) { attackTimer--; showAttack = true; }
        else                 { showAttack = false; }

        // ── Tương tác (E) ──
        if (keyB.interactPressed) {
            keyB.interactPressed = false;
            interact();
        }

        // ── Tấn công (Space) ──
        if (keyB.attackPressed) {
            keyB.attackPressed = false;
            doAttack();
        }

        // ── Kiểm tra chết ──
        if (hp <= 0) {
            alive = false;
            Sound.play("die");
            gp.gameState = GameState.GAME_OVER;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    private void interact() {
        Rectangle reach = interactBox();

        for (int i = 0; i < gp.obj.length; i++) {
            SuperObject o = gp.obj[i];
            if (o == null) continue;

            Rectangle oBox = new Rectangle(
                o.worldX + o.solidArea.x,
                o.worldY + o.solidArea.y,
                o.solidArea.width, o.solidArea.height);

            if (!reach.intersects(oBox)) continue;

            // ── Phản ứng theo loại object ──
            if (o instanceof Key) {
                keys++;
                gp.obj[i] = null;
                gp.ui.showMessage("Nhặt được chìa khóa!  [🗝 × " + keys + "]");
                Sound.play("pickup");

            } else if (o instanceof Door) {
                if (keys > 0) {
                    keys--;
                    gp.obj[i] = null;
                    gp.ui.showMessage("Cửa đã mở!  [🗝 còn lại: " + keys + "]");
                    Sound.play("door");
                } else {
                    gp.ui.showMessage("Cần chìa khóa để mở cửa!");
                    Sound.play("nope");
                }

            } else if (o instanceof Chest chest) {
                if (!chest.opened) {
                    chest.opened = true;
                    try {
                        chest.image = javax.imageio.ImageIO.read(
                            getClass().getResourceAsStream("/objects/chest_opened.png"));
                    } catch (Exception ex) { ex.printStackTrace(); }
                    gp.gameState = GameState.WIN;
                    Sound.play("win");
                }

            } else if (o instanceof Potion pot) {
                if (hp < maxHp) {
                    hp = Math.min(maxHp, hp + pot.healAmount);
                    gp.obj[i] = null;
                    gp.ui.showMessage("Uống thuốc! +" + pot.healAmount + " HP  ❤");
                    Sound.play("pickup");
                } else {
                    gp.ui.showMessage("HP đã đầy rồi!");
                }
            }
            break; // Chỉ tương tác 1 object mỗi lần
        }
    }

    // ─────────────────────────────────────────────────────────────────
    private void doAttack() {
        if (attackTimer > 0) return;
        attackTimer = ATTACK_CD;
        Sound.play("attack");

        Rectangle atkBox = attackBox();

        for (Enemy enemy : gp.enemies) {
            if (enemy == null || !enemy.alive) continue;
            Rectangle eBox = new Rectangle(
                enemy.worldX + enemy.solidArea.x,
                enemy.worldY + enemy.solidArea.y,
                enemy.solidArea.width, enemy.solidArea.height);

            if (!atkBox.intersects(eBox)) continue;

            int dmg = Math.max(1, attack - enemy.defense);
            enemy.hp   -= dmg;
            enemy.hitTimer = 12;

            if (enemy.hp <= 0) {
                enemy.alive = false;
                dropLoot(enemy);
            }
        }
    }

    private void dropLoot(Enemy enemy) {
        // 40% cơ hội rơi thuốc
        if (Math.random() < 0.40) {
            for (int i = 0; i < gp.obj.length; i++) {
                if (gp.obj[i] == null) {
                    gp.obj[i] = new Potion();
                    gp.obj[i].worldX = enemy.worldX;
                    gp.obj[i].worldY = enemy.worldY;
                    break;
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    /** Hitbox hiện tại (tọa độ world) */
    private Rectangle currentBox() {
        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y,
                             solidArea.width, solidArea.height);
    }

    /** Vùng tầm với tương tác (mở rộng 52px về phía đang nhìn) */
    private Rectangle interactBox() {
        Rectangle b = currentBox();
        int range = 52;
        return switch (direction) {
            case "up"    -> new Rectangle(b.x, b.y - range, b.width, b.height + range);
            case "down"  -> new Rectangle(b.x, b.y, b.width, b.height + range);
            case "left"  -> new Rectangle(b.x - range, b.y, b.width + range, b.height);
            default      -> new Rectangle(b.x, b.y, b.width + range, b.height); // right
        };
    }

    /** Vùng tầm tấn công (mở rộng 64px về phía đang nhìn) */
    private Rectangle attackBox() {
        Rectangle b = currentBox();
        int range = 64;
        return switch (direction) {
            case "up"    -> new Rectangle(b.x, b.y - range, b.width, b.height + range);
            case "down"  -> new Rectangle(b.x, b.y, b.width, b.height + range);
            case "left"  -> new Rectangle(b.x - range, b.y, b.width + range, b.height);
            default      -> new Rectangle(b.x, b.y, b.width + range, b.height); // right
        };
    }

    // ─────────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        // Nhấp nháy khi bất tử
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        }

        BufferedImage img = switch (direction) {
            case "up"    -> up[spriteNum];
            case "left"  -> left[spriteNum];
            case "right" -> right[spriteNum];
            default      -> down[spriteNum];
        };

        int sx = worldX - gp.cameraX;
        int sy = worldY - gp.cameraY;
        g2.drawImage(img, sx, sy, gp.tileSize, gp.tileSize, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Hiển thị vùng tấn công
        if (showAttack) {
            Rectangle ab = attackBox();
            int ax = ab.x - gp.cameraX;
            int ay = ab.y - gp.cameraY;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(255, 220, 50));
            g2.fillRect(ax, ay, ab.width, ab.height);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            g2.setColor(new Color(255, 140, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(ax, ay, ab.width, ab.height);
            g2.setStroke(new BasicStroke(1));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }
}
