package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import objects.*;

/**
 * Quản lý toàn bộ UI: HUD, title, pause, inventory, game over, win.
 */
public class UI {

    private final GamePanel gp;
    private final Font fontBig, fontMed, fontSmall;

    private BufferedImage heartFull, heartHalf, heartBlank;
    private BufferedImage keyIcon, coinIcon;
    private BufferedImage manaFull, manaBlank;

    public String message = "";
    public int messageTimer = 0;

    // Title cursor
    private int titleCursor = 0;
    private boolean downHeld = false, upHeld = false;

    // ─────────────────────────────────────────────────────────────────
    public UI(GamePanel gp) {
        this.gp   = gp;
        fontBig   = new Font("SansSerif", Font.BOLD, 48);
        fontMed   = new Font("SansSerif", Font.BOLD, 22);
        fontSmall = new Font("SansSerif", Font.PLAIN, 13);

        try {
            heartFull  = ImageIO.read(getClass().getResourceAsStream("/objects/heart_full.png"));
            heartHalf  = ImageIO.read(getClass().getResourceAsStream("/objects/heart_half.png"));
            heartBlank = ImageIO.read(getClass().getResourceAsStream("/objects/heart_blank.png"));
            keyIcon    = ImageIO.read(getClass().getResourceAsStream("/objects/key.png"));
            coinIcon   = ImageIO.read(getClass().getResourceAsStream("/objects/coin_bronze.png"));
            manaFull   = ImageIO.read(getClass().getResourceAsStream("/objects/manacrystal_full.png"));
            manaBlank  = ImageIO.read(getClass().getResourceAsStream("/objects/manacrystal_blank.png"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void showMessage(String msg) { message = msg; messageTimer = 150; }

    public int getTitleCursor() { return titleCursor; }

    // ─────────────────────────────────────────────────────────────────
    public void update() {
        if (messageTimer > 0) messageTimer--;

        if (gp.gameState == GameState.TITLE) {
            String[] opts = titleOptions();
            if (gp.keyB.down && !downHeld) { titleCursor = (titleCursor + 1) % opts.length; downHeld = true; }
            else if (!gp.keyB.down) downHeld = false;

            if (gp.keyB.up && !upHeld) { titleCursor = (titleCursor - 1 + opts.length) % opts.length; upHeld = true; }
            else if (!gp.keyB.up) upHeld = false;
        }

        if (gp.gameState == GameState.SHOP) gp.shopUI.update();
    }

    private String[] titleOptions() {
        return SaveManager.hasSave()
            ? new String[]{"Bắt đầu mới", "Tiếp tục", "Thoát game"}
            : new String[]{"Bắt đầu chơi", "Thoát game"};
    }

    // ─────────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        switch (gp.gameState) {
            case TITLE     -> drawTitle(g2);
            case PLAYING   -> drawHUD(g2);
            case PAUSED    -> { drawHUD(g2); drawPause(g2); }
            case INVENTORY -> { drawHUD(g2); drawInventory(g2); }
            case SHOP      -> { drawHUD(g2); gp.shopUI.draw(g2); }
            case GAME_OVER -> drawGameOver(g2);
            case WIN       -> drawWin(g2);
        }
    }

    // ── HUD ──────────────────────────────────────────────────────────
    private void drawHUD(Graphics2D g2) {
        Entity.Player p = gp.player;
        final int hs = 32;

        // Hearts
        for (int i = 0; i < p.maxHp / 2; i++) {
            int hx = 12 + i * (hs + 2);
            drawImg(g2, heartBlank, hx, 10, hs, hs);
            int f = p.hp - i * 2;
            if      (f >= 2) drawImg(g2, heartFull,  hx, 10, hs, hs);
            else if (f == 1) drawImg(g2, heartHalf,  hx, 10, hs, hs);
        }

        // Level badge
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(255, 215, 0));
        shadow(g2, "Lv." + p.level, 14, 58);

        // XP bar (small, below hearts)
        int xpBarW = 120, xpBarH = 6;
        int xpFill = (p.xpToNextLevel > 0)
            ? (int)((double) p.xp / p.xpToNextLevel * xpBarW) : xpBarW;
        g2.setColor(new Color(30, 30, 60));
        g2.fillRect(14, 62, xpBarW, xpBarH);
        g2.setColor(new Color(80, 180, 255));
        g2.fillRect(14, 62, xpFill, xpBarH);
        g2.setColor(new Color(60, 100, 180));
        g2.drawRect(14, 62, xpBarW, xpBarH);

        // Keys
        drawImg(g2, keyIcon, 14, 74, 20, 20);
        g2.setFont(fontSmall);
        g2.setColor(Color.WHITE);
        shadow(g2, "×" + p.keys, 38, 89);

        // Coins
        drawImg(g2, coinIcon, 60, 74, 20, 20);
        g2.setFont(fontSmall);
        shadow(g2, "×" + p.coins, 84, 89);

        // Level-up flash
        if (p.levelUpTimer > 0) {
            float alpha = Math.min(1f, p.levelUpTimer / 40f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            drawCentered(g2, "⬆ LEVEL UP!  Lv." + p.level, gp.depth / 2 - 30, new Color(255, 215, 0));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Equipped items strip (bottom-right)
        drawEquipped(g2);

        // Floating message
        if (messageTimer > 0) {
            float alpha = Math.min(1f, messageTimer / 30f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setFont(new Font("SansSerif", Font.BOLD, 17));
            int my = gp.depth - 65;
            for (String line : message.split("\n")) {
                FontMetrics fm = g2.getFontMetrics();
                int mx = (gp.width - fm.stringWidth(line)) / 2;
                shadow(g2, line, mx, my, new Color(255, 230, 80));
                my += 22;
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Control hints
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(180, 180, 180, 140));
        g2.drawString("WASD:Move  E:Interact  Space:Attack  I:Inventory  M:Map  ESC:Pause",
                10, gp.depth - 6);

        // Current level indicator
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(180, 180, 255));
        g2.drawString("Màn " + gp.currentLevel + " / " + gp.MAX_LEVEL,
                gp.width - 80, gp.depth - 6);
    }

    private void drawEquipped(Graphics2D g2) {
        Entity.Player p = gp.player;
        int slotSize = 28;
        int baseX = gp.width - (slotSize + 4) * 4 - 8;
        int baseY = gp.depth - slotSize - 28;

        SuperObject[] slots = {p.equippedWeapon, p.equippedShield, p.equippedBoots, p.equippedLantern};
        String[] labels = {"ATK", "DEF", "SPD", "LGT"};
        Color[] colors  = {new Color(255,120,60), new Color(100,160,255),
                           new Color(100,220,100), new Color(255,220,80)};

        for (int i = 0; i < 4; i++) {
            int sx = baseX + i * (slotSize + 4);
            g2.setColor(new Color(20, 20, 40, 180));
            g2.fillRoundRect(sx, baseY, slotSize, slotSize, 6, 6);
            g2.setColor(colors[i]);
            g2.drawRoundRect(sx, baseY, slotSize, slotSize, 6, 6);

            if (slots[i] != null && slots[i].image != null)
                g2.drawImage(slots[i].image, sx + 2, baseY + 2, slotSize - 4, slotSize - 4, null);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.setColor(colors[i]);
            g2.drawString(labels[i], sx + 3, baseY + slotSize + 10);
        }

        // Stat numbers
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(Color.WHITE);
        g2.drawString("ATK:" + p.effectiveAttack() + " DEF:" + p.effectiveDefense()
                + " SPD:" + p.effectiveSpeed(), baseX, baseY - 4);
    }

    // ── INVENTORY ────────────────────────────────────────────────────
    private void drawInventory(Graphics2D g2) {
        Entity.Player p = gp.player;
        int pw = 500, ph = 340;
        int px = (gp.width - pw) / 2, py = (gp.depth - ph) / 2;

        // Panel
        g2.setColor(new Color(15, 12, 30, 230));
        g2.fillRoundRect(px, py, pw, ph, 14, 14);
        g2.setColor(new Color(120, 80, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(px, py, pw, ph, 14, 14);
        g2.setStroke(new BasicStroke(1));

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        drawCentered(g2, "⚔  INVENTORY  ⚔", py + 28, new Color(255, 215, 0));

        // Separator
        g2.setColor(new Color(90, 60, 140));
        g2.fillRect(px + 10, py + 36, pw - 20, 1);

        // ── Stats column ──
        int statX = px + 20, statY = py + 56;
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(200, 190, 230));
        String[] statLabels = {"Level", "HP", "ATK", "DEF", "SPD", "XP", "Keys", "Coins"};
        String[] statVals   = {
            String.valueOf(p.level),
            p.hp + " / " + p.maxHp,
            String.valueOf(p.effectiveAttack()),
            String.valueOf(p.effectiveDefense()),
            String.valueOf(p.effectiveSpeed()),
            p.xp + " / " + p.xpToNextLevel,
            String.valueOf(p.keys),
            String.valueOf(p.coins)
        };
        Color[] statColors = {
            new Color(255,215,0), new Color(220,60,60), new Color(255,150,50),
            new Color(80,180,255), new Color(100,230,100), new Color(80,180,255),
            new Color(255,220,80), new Color(255,200,60)
        };
        for (int i = 0; i < statLabels.length; i++) {
            g2.setColor(new Color(140, 130, 170));
            g2.drawString(statLabels[i] + ":", statX, statY + i * 26);
            g2.setColor(statColors[i]);
            g2.drawString(statVals[i], statX + 70, statY + i * 26);
        }

        // ── Equipment column ──
        int eqX = px + 260, eqY = py + 56;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(200, 190, 230));
        g2.drawString("EQUIPPED", eqX, eqY - 6);

        SuperObject[] slots = {p.equippedWeapon, p.equippedShield, p.equippedBoots, p.equippedLantern};
        String[] slotLabels = {"Weapon", "Shield", "Boots", "Lantern"};
        int slotSize = 44;

        for (int i = 0; i < 4; i++) {
            int bx = eqX + (i % 2) * (slotSize + 8);
            int by = eqY + (i / 2) * (slotSize + 24);

            g2.setColor(new Color(30, 20, 55));
            g2.fillRoundRect(bx, by, slotSize, slotSize, 8, 8);
            g2.setColor(new Color(100, 70, 150));
            g2.drawRoundRect(bx, by, slotSize, slotSize, 8, 8);

            if (slots[i] != null && slots[i].image != null)
                g2.drawImage(slots[i].image, bx + 4, by + 4, slotSize - 8, slotSize - 8, null);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(150, 140, 180));
            g2.drawString(slotLabels[i], bx, by + slotSize + 12);
            if (slots[i] != null) {
                g2.setColor(Color.WHITE);
                g2.drawString(slots[i].name, bx, by + slotSize + 22);
            }
        }

        // Close hint
        g2.setFont(fontSmall);
        g2.setColor(new Color(130, 120, 160));
        drawCentered(g2, "[ I ] hoặc [ ESC ] Đóng", py + ph - 12, new Color(130, 120, 160));
    }

    // ── TITLE ────────────────────────────────────────────────────────
    private void drawTitle(Graphics2D g2) {
        GradientPaint bg = new GradientPaint(0, 0, new Color(10, 10, 35),
                0, gp.depth, new Color(30, 0, 55));
        g2.setPaint(bg); g2.fillRect(0, 0, gp.width, gp.depth);

        g2.setFont(fontBig);
        drawCentered(g2, "⚔  DUNGEON QUEST  ⚔", 115, new Color(255, 215, 0));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 18));
        drawCentered(g2, "Game của NVTS", 148, new Color(160, 155, 210));

        g2.setColor(new Color(80, 60, 130));
        g2.fillRect(gp.width / 4, 160, gp.width / 2, 2);

        String[] opts = titleOptions();
        g2.setFont(fontMed);
        int oy = 215;
        for (int i = 0; i < opts.length; i++) {
            boolean sel = i == titleCursor;
            drawCentered(g2, (sel ? "▶  " : "    ") + opts[i], oy,
                    sel ? new Color(255, 230, 80) : new Color(190, 185, 215));
            oy += 50;
        }

        g2.setFont(fontSmall);
        drawCentered(g2, "WASD: Di chuyển  |  E: Tương tác  |  Space: Tấn công",
                gp.depth - 38, new Color(120, 115, 155));
        drawCentered(g2, "I: Inventory  |  M: Minimap  |  ESC: Pause",
                gp.depth - 20, new Color(100, 95, 135));
    }

    // ── PAUSE ────────────────────────────────────────────────────────
    private void drawPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 165)); g2.fillRect(0, 0, gp.width, gp.depth);
        g2.setFont(fontBig);
        drawCentered(g2, "TẠM DỪNG", 185, Color.WHITE);
        g2.setFont(fontMed);
        drawCentered(g2, "[ ESC ]  Tiếp tục",   260, new Color(170, 230, 170));
        drawCentered(g2, "[ Enter ]  Lưu game",  308, new Color(170, 200, 255));
        drawCentered(g2, "[ Q ]  Về menu chính", 356, new Color(230, 160, 160));
    }

    // ── GAME OVER ────────────────────────────────────────────────────
    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 195)); g2.fillRect(0, 0, gp.width, gp.depth);
        g2.setFont(fontBig);
        drawCentered(g2, "GAME OVER", 195, new Color(220, 40, 40));
        g2.setFont(fontMed);
        drawCentered(g2, "Bạn đã bị đánh bại...",            255, new Color(200, 155, 155));
        drawCentered(g2, "[ R ] Chơi lại   [ ESC ] Menu", 320, Color.WHITE);
    }

    // ── WIN ──────────────────────────────────────────────────────────
    private void drawWin(Graphics2D g2) {
        GradientPaint bg = new GradientPaint(0, 0, new Color(40, 30, 5),
                0, gp.depth, new Color(60, 50, 10));
        g2.setPaint(bg); g2.fillRect(0, 0, gp.width, gp.depth);
        g2.setFont(fontBig);
        drawCentered(g2, "✨  CHIẾN THẮNG!  ✨", 175, new Color(255, 215, 0));
        g2.setFont(fontMed);
        drawCentered(g2, "Bạn đã hoàn thành Dungeon Quest!", 240, Color.WHITE);
        drawCentered(g2, "Màn " + gp.currentLevel + " / " + gp.MAX_LEVEL + " clears!", 276, new Color(180, 220, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        drawCentered(g2, "[ R ] Chơi lại   [ ESC ] Menu", 350, new Color(200, 195, 220));
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private void drawCentered(Graphics2D g2, String text, int y, Color color) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (gp.width - fm.stringWidth(text)) / 2;
        shadow(g2, text, x, y, color);
    }

    private void shadow(Graphics2D g2, String text, int x, int y) {
        shadow(g2, text, x, y, g2.getColor());
    }

    private void shadow(Graphics2D g2, String text, int x, int y, Color color) {
        Color prev = g2.getColor();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(color);
        g2.drawString(text, x, y);
        g2.setColor(prev);
    }

    private void drawImg(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img != null) g2.drawImage(img, x, y, w, h, null);
    }
}
