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

    /** Vị trí cố định trên màn hình (luôn ở giữa) */
    public final int screenX, screenY;

    // ── Inventory / stats ──
    public int keys   = 0;
    public int coins  = 0;
    public int xp     = 0;
    public int xpToNextLevel = 10;
    public int levelUpTimer  = 0;   // flash khi lên cấp

    // ── Equipment slots ──
    public SuperObject equippedWeapon  = null;  // SwordItem | AxeItem
    public SuperObject equippedShield  = null;  // ShieldWood | ShieldBlue
    public SuperObject equippedBoots   = null;  // BootsItem
    public SuperObject equippedLantern = null;  // LanternItem

    // ── Combat ──
    public int invincibleTimer  = 0;
    private int attackTimer     = 0;
    private static final int ATTACK_CD = 22;
    private boolean showAttack  = false;
    private int interactCooldown = 0;  // frames cooldown giữa 2 lần interact

    // ── Constructor ──────────────────────────────────────────────────
    public Player(GamePanel gp, Move keyB) {
        this.gp   = gp;
        this.keyB = keyB;

        worldX   = gp.tileSize * 23;
        worldY   = gp.tileSize * 21;
        screenX  = gp.width  / 2 - gp.tileSize / 2;
        screenY  = gp.depth  / 2 - gp.tileSize / 2;
        direction = "down";

        maxHp  = 6; hp = 6;
        attack = 2; defense = 0; speed = 3;
        level  = 1;

        solidArea = new Rectangle(8, 16, 32, 32);
        down  = new BufferedImage[7]; up   = new BufferedImage[7];
        left  = new BufferedImage[7]; right = new BufferedImage[7];
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

    // ── Effective stats (base + equipment) ──────────────────────────
    public int effectiveAttack() {
        int b = 0;
        if (equippedWeapon instanceof EquipItem ei) b += ei.attackBonus;
        return attack + b;
    }

    public int effectiveDefense() {
        int b = 0;
        if (equippedShield instanceof EquipItem ei) b += ei.defenseBonus;
        return defense + b;
    }

    public int effectiveSpeed() {
        int b = 0;
        if (equippedBoots instanceof EquipItem ei) b += ei.speedBonus;
        if (equippedWeapon instanceof AxeItem ax)   b += ax.speedBonus; // Axe penalty
        return Math.max(1, speed + b);
    }

    // ── Update ───────────────────────────────────────────────────────
    public void update() {
        if (!alive) return;

        // Movement
        boolean moving = false;
        int dx = 0, dy = 0;
        if (keyB.up)    { dy -= effectiveSpeed(); moving = true; }
        if (keyB.down)  { dy += effectiveSpeed(); moving = true; }
        if (keyB.left)  { dx -= effectiveSpeed(); moving = true; }
        if (keyB.right) { dx += effectiveSpeed(); moving = true; }

        // Normalize diagonal
        if (dx != 0 && dy != 0) {
            double f = 1.0 / Math.sqrt(2);
            dx = (int) Math.round(dx * f);
            dy = (int) Math.round(dy * f);
        }

        // Direction
        if      (dy < 0) direction = "up";
        else if (dy > 0) direction = "down";
        else if (dx < 0) direction = "left";
        else if (dx > 0) direction = "right";

        // Collision-aware movement
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

        // Animation
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 7) { spriteNum = (spriteNum % 6) + 1; spriteCounter = 0; }
        } else { spriteNum = 0; }

        // Timers
        if (invincibleTimer > 0) invincibleTimer--;
        if (attackTimer > 0)     { attackTimer--; showAttack = true; }
        else                     { showAttack = false; }
        if (levelUpTimer > 0)    levelUpTimer--;

        // Auto-pickup (coins, keys, potions)
        autoPickup();

        // Interact với cooldown chống spam
        if (interactCooldown > 0) interactCooldown--;
        if (keyB.interactPressed) {
            keyB.interactPressed = false;
            if (interactCooldown == 0) {
                interact();
                interactCooldown = 30;  // 0.5 giây cooldown
            }
        }
        if (keyB.attackPressed)   { keyB.attackPressed   = false; doAttack(); }

        // Death check
        if (hp <= 0) {
            alive = false;
            Sound.play("die");
            gp.gameState = GameState.GAME_OVER;
        }
    }

    // ── Auto-pickup (walk-over): coins, keys, potions ────────────────
    private void autoPickup() {
        Rectangle myBox = currentBox();
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) continue;
            Rectangle oBox = new Rectangle(
                gp.obj[i].worldX + gp.obj[i].solidArea.x,
                gp.obj[i].worldY + gp.obj[i].solidArea.y,
                gp.obj[i].solidArea.width, gp.obj[i].solidArea.height);
            if (!myBox.intersects(oBox)) continue;

            if (gp.obj[i] instanceof Coin c) {
                coins += c.value;
                gp.obj[i] = null;
                Sound.play("coin");
                gp.ui.showMessage("+" + c.value + " 🪙  [Tổng: " + coins + "]");

            } else if (gp.obj[i] instanceof Key) {
                keys++;
                gp.obj[i] = null;
                gp.ui.showMessage("Nhặt chìa khóa!  [🗝 × " + keys + "]");
                Sound.play("pickup");

            } else if (gp.obj[i] instanceof Potion pot) {
                if (hp < maxHp) {
                    hp = Math.min(maxHp, hp + pot.healAmount);
                    gp.obj[i] = null;
                    gp.ui.showMessage("Uống thuốc! +" + pot.healAmount + " HP ❤");
                    Sound.play("pickup");
                }
                // Nếu HP đầy → để lại trên sàn, nhặt sau khi bị đánh
            }
        }
    }

    // ── Interact (E) ─────────────────────────────────────────────────
    private void interact() {
        Rectangle reach = interactBox();

        for (int i = 0; i < gp.obj.length; i++) {
            SuperObject o = gp.obj[i];
            if (o == null) continue;
            Rectangle oBox = new Rectangle(
                o.worldX + o.solidArea.x, o.worldY + o.solidArea.y,
                o.solidArea.width, o.solidArea.height);
            if (!reach.intersects(oBox)) continue;

            handleObject(i, o);
            break;
        }
    }

    private void handleObject(int idx, SuperObject o) {
        if (o instanceof Door) {
            if (keys > 0) {
                keys--;
                gp.obj[idx] = null;
                gp.ui.showMessage("Cửa đã mở!  [🗝 còn: " + keys + "]");
                Sound.play("door");
            } else {
                gp.ui.showMessage("Cần chìa khóa để mở cửa!");
                Sound.play("nope");
            }

        } else if (o instanceof Chest chest) {
            if (!chest.opened) {
                chest.opened = true;
                try { chest.image = ImageIO.read(getClass().getResourceAsStream("/objects/chest_opened.png")); }
                catch (Exception ex) { ex.printStackTrace(); }
                gp.gameState = GameState.WIN;
                Sound.play("win");
            }

        } else if (o instanceof EquipItem equip) {
            equipItem(equip);
            gp.obj[idx] = null;

        } else if (o instanceof Portal portal) {
            if (gp.currentLevel < gp.MAX_LEVEL) {
                Sound.play("portal");
                gp.ui.showMessage("Chuyển sang Màn " + portal.targetLevel + "!");
                Main.SaveManager.save(gp);
                gp.loadLevel(portal.targetLevel);
            } else {
                // Level 3 portal = WIN
                gp.gameState = GameState.WIN;
                Sound.play("win");
            }

        } else if (o instanceof NPC) {
            gp.gameState = GameState.SHOP;
            Sound.play("shop");
        }
    }

    public void equipItem(EquipItem equip) {
        switch (equip.slot) {
            case "weapon"  -> equippedWeapon  = equip;
            case "shield"  -> equippedShield  = equip;
            case "boots"   -> equippedBoots   = equip;
            case "lantern" -> equippedLantern = equip;
        }
        gp.ui.showMessage("Trang bị: " + equip.name + "!");
        Sound.play("equip");
    }

    // ── Attack (Space) ────────────────────────────────────────────────
    private void doAttack() {
        if (attackTimer > 0) return;
        attackTimer = ATTACK_CD;
        Sound.play("attack");
        Rectangle atkBox = attackBox();

        for (Enemy enemy : gp.enemies) {
            if (enemy == null || !enemy.alive) continue;
            Rectangle eBox = new Rectangle(
                enemy.worldX + enemy.solidArea.x, enemy.worldY + enemy.solidArea.y,
                enemy.solidArea.width, enemy.solidArea.height);
            if (!atkBox.intersects(eBox)) continue;

            int dmg = Math.max(1, effectiveAttack() - enemy.defense);
            enemy.hp -= dmg;
            enemy.hitTimer = 12;

            if (enemy.hp <= 0) {
                enemy.alive = false;
                gainXP(enemy.xpReward);
                dropLoot(enemy);
            }
        }
    }

    public void gainXP(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        xpToNextLevel = level * 12;
        maxHp += 2; hp = maxHp;
        if (level % 2 == 0) attack++;
        else                 defense++;
        levelUpTimer = 150;
        gp.ui.showMessage("⬆ LEVEL UP!  Lv." + level
            + "  HP+" + maxHp + "  ATK+" + effectiveAttack());
        Sound.play("levelup");
    }

    private void dropLoot(Enemy enemy) {
        java.util.Random rnd = new java.util.Random();

        // Boss drops guaranteed coins
        int coinAmt = (enemy.type == 3) ? rnd.nextInt(11) + 20
                    : rnd.nextBoolean()  ? 1 : 0;
        if (coinAmt > 0) spawnObj(new Coin(coinAmt), enemy.worldX, enemy.worldY);

        double r = rnd.nextDouble();
        if      (r < 0.25) spawnObj(new Potion(),   enemy.worldX + 10, enemy.worldY + 10);
        else if (r < 0.30) {
            EquipItem drop = randomEquip(rnd);
            if (drop != null) spawnObj(drop, enemy.worldX + 5, enemy.worldY + 5);
        }
    }

    private EquipItem randomEquip(java.util.Random rnd) {
        return switch (rnd.nextInt(5)) {
            case 0 -> new SwordItem();
            case 1 -> new AxeItem();
            case 2 -> new ShieldWood();
            case 3 -> new BootsItem();
            default -> new LanternItem();
        };
    }

    private void spawnObj(SuperObject obj, int wx, int wy) {
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                gp.obj[i] = obj;
                obj.worldX = wx; obj.worldY = wy;
                return;
            }
        }
    }

    // ── Hitbox helpers ───────────────────────────────────────────────
    private Rectangle currentBox() {
        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y,
                             solidArea.width, solidArea.height);
    }

    private Rectangle interactBox() {
        Rectangle b = currentBox(); int r = 52;
        return switch (direction) {
            case "up"   -> new Rectangle(b.x, b.y - r, b.width, b.height + r);
            case "down" -> new Rectangle(b.x, b.y, b.width, b.height + r);
            case "left" -> new Rectangle(b.x - r, b.y, b.width + r, b.height);
            default     -> new Rectangle(b.x, b.y, b.width + r, b.height);
        };
    }

    private Rectangle attackBox() {
        Rectangle b = currentBox(); int r = 90;
        return switch (direction) {
            case "up"   -> new Rectangle(b.x, b.y - r, b.width, b.height + r);
            case "down" -> new Rectangle(b.x, b.y, b.width, b.height + r);
            case "left" -> new Rectangle(b.x - r, b.y, b.width + r, b.height);
            default     -> new Rectangle(b.x, b.y, b.width + r, b.height);
        };
    }

    // ── Draw ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        // Nhấp nháy khi bất tử
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));

        BufferedImage img = switch (direction) {
            case "up"   -> up[spriteNum];
            case "left" -> left[spriteNum];
            case "right"-> right[spriteNum];
            default     -> down[spriteNum];
        };

        g2.drawImage(img, worldX - gp.cameraX, worldY - gp.cameraY,
                     gp.tileSize, gp.tileSize, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Hiệu ứng vùng tấn công
        if (showAttack) {
            Rectangle ab = attackBox();
            int ax = ab.x - gp.cameraX, ay = ab.y - gp.cameraY;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(255, 220, 50));
            g2.fillRect(ax, ay, ab.width, ab.height);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            g2.setColor(new Color(255, 140, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(ax, ay, ab.width, ab.height);
            g2.setStroke(new BasicStroke(1));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }
}
