package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import objects.*;

/**
 * Giao diện cửa hàng NPC.
 * Điều hướng bằng W/S, mua bằng E hoặc Enter, đóng bằng ESC.
 */
public class ShopUI {

    private final GamePanel gp;
    private int cursor = 0;

    // Debounce cho W/S trong shop
    private boolean upHeld = false, downHeld = false;

    record ShopEntry(String name, String desc, int price, String type) {}

    private final ShopEntry[] items = {
        new ShopEntry("Potion",      "+2 HP hồi phục",        5,  "potion"),
        new ShopEntry("Sword",       "+3 ATK",                15, "sword"),
        new ShopEntry("Blue Shield", "+2 DEF",                20, "shield_blue"),
        new ShopEntry("Boots",       "+2 SPD",                12, "boots"),
        new ShopEntry("Lantern",     "Tầm sáng ×2",           18, "lantern"),
        new ShopEntry("Axe",         "+5 ATK  -1 SPD",        22, "axe"),
        new ShopEntry("Wood Shield", "+1 DEF",                 8,  "shield_wood"),
    };

    private BufferedImage[] icons;

    public ShopUI(GamePanel gp) {
        this.gp = gp;
        icons   = new BufferedImage[items.length];
        loadIcons();
    }

    private void loadIcons() {
        String[] paths = {
            "/objects/potion_red.png", "/objects/sword_normal.png",
            "/objects/shield_blue.png", "/objects/boots.png",
            "/objects/lantern.png", "/objects/axe.png", "/objects/shield_wood.png"
        };
        for (int i = 0; i < paths.length; i++) {
            try { icons[i] = ImageIO.read(getClass().getResourceAsStream(paths[i])); }
            catch (Exception ignored) { }
        }
    }

    public void update() {
        // Scroll cursor
        if (gp.keyB.up && !upHeld) {
            cursor = (cursor - 1 + items.length) % items.length;
            upHeld = true;
        } else if (!gp.keyB.up) upHeld = false;

        if (gp.keyB.down && !downHeld) {
            cursor = (cursor + 1) % items.length;
            downHeld = true;
        } else if (!gp.keyB.down) downHeld = false;

        // Buy
        if (gp.keyB.enterPressed || gp.keyB.interactPressed) {
            gp.keyB.enterPressed = false;
            gp.keyB.interactPressed = false;
            tryBuy();
        }

        // Close
        if (gp.keyB.escPressed) {
            gp.keyB.escPressed = false;
            gp.gameState = GameState.PLAYING;
        }
    }

    private void tryBuy() {
        ShopEntry entry = items[cursor];
        if (gp.player.coins < entry.price) {
            gp.ui.showMessage("Không đủ tiền! Cần " + entry.price + " 🪙");
            Sound.play("nope");
            return;
        }
        gp.player.coins -= entry.price;
        giveItem(entry);
        gp.ui.showMessage("Mua: " + entry.name + "!  [-" + entry.price + " 🪙]");
        Sound.play("pickup");
    }

    private void giveItem(ShopEntry e) {
        switch (e.type) {
            case "potion"      -> addToWorld(new Potion(),      gp.player.worldX, gp.player.worldY);
            case "sword"       -> gp.player.equipItem(new SwordItem());
            case "axe"         -> gp.player.equipItem(new AxeItem());
            case "shield_blue" -> gp.player.equipItem(new ShieldBlue());
            case "shield_wood" -> gp.player.equipItem(new ShieldWood());
            case "boots"       -> gp.player.equipItem(new BootsItem());
            case "lantern"     -> gp.player.equipItem(new LanternItem());
        }
    }

    private void addToWorld(SuperObject obj, int wx, int wy) {
        // Potions bought at shop go directly to a heal action
        if (obj instanceof Potion pot) {
            gp.player.hp = Math.min(gp.player.maxHp, gp.player.hp + pot.healAmount);
            return;
        }
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) { gp.obj[i] = obj; obj.worldX = wx; obj.worldY = wy; return; }
        }
    }

    // ── Draw ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int pw = 520, ph = 360;
        int px = (gp.width  - pw) / 2;
        int py = (gp.depth  - ph) / 2;

        // Background panel
        g2.setColor(new Color(20, 15, 35, 230));
        g2.fillRoundRect(px, py, pw, ph, 16, 16);
        g2.setColor(new Color(140, 100, 200));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(px, py, pw, ph, 16, 16);
        g2.setStroke(new BasicStroke(1));

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(new Color(255, 215, 0));
        String title = "🛒  CỬA HÀNG THƯƠNG NHÂN";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, px + (pw - fm.stringWidth(title)) / 2, py + 32);

        // Coins
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(255, 200, 60));
        g2.drawString("🪙 " + gp.player.coins, px + pw - 100, py + 32);

        // Separator
        g2.setColor(new Color(100, 70, 150));
        g2.fillRect(px + 10, py + 40, pw - 20, 2);

        // Item list
        int itemY = py + 60;
        int rowH  = 40;
        for (int i = 0; i < items.length; i++) {
            ShopEntry entry = items[i];
            boolean sel = i == cursor;

            if (sel) {
                g2.setColor(new Color(80, 50, 130, 200));
                g2.fillRoundRect(px + 8, itemY - 18, pw - 16, rowH - 4, 8, 8);
                g2.setColor(new Color(180, 130, 255));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(px + 8, itemY - 18, pw - 16, rowH - 4, 8, 8);
                g2.setStroke(new BasicStroke(1));
            }

            // Icon
            if (icons[i] != null) g2.drawImage(icons[i], px + 14, itemY - 16, 28, 28, null);
            else { g2.setColor(Color.GRAY); g2.fillRect(px + 14, itemY - 16, 28, 28); }

            // Name
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(sel ? Color.WHITE : new Color(200, 190, 220));
            g2.drawString(entry.name(), px + 50, itemY - 4);

            // Description
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(sel ? new Color(200, 230, 200) : new Color(140, 130, 160));
            g2.drawString(entry.desc(), px + 50, itemY + 10);

            // Price
            boolean canAfford = gp.player.coins >= entry.price;
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(canAfford ? new Color(255, 210, 50) : new Color(180, 80, 80));
            String priceStr = entry.price + " 🪙";
            FontMetrics pfm = g2.getFontMetrics();
            g2.drawString(priceStr, px + pw - pfm.stringWidth(priceStr) - 14, itemY);

            itemY += rowH;
        }

        // Controls hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(140, 130, 160));
        String hint = "W/S: chọn   Enter/E: mua   ESC: đóng";
        fm = g2.getFontMetrics();
        g2.drawString(hint, px + (pw - fm.stringWidth(hint)) / 2, py + ph - 12);
    }
}
