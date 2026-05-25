package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Quản lý toàn bộ UI: HUD, màn hình title, pause, game over, win.
 */
public class UI {

    private final GamePanel gp;
    private final Font fontBig, fontMed, fontSmall;

    private BufferedImage heartFull, heartHalf, heartBlank;
    private BufferedImage keyIcon;

    public String message = "";
    public int messageTimer = 0;

    // Cursor cho title menu
    private int titleCursor = 0;
    private static final String[] TITLE_OPTIONS = {"Bắt đầu chơi", "Thoát game"};

    public UI(GamePanel gp) {
        this.gp = gp;
        fontBig   = new Font("SansSerif", Font.BOLD, 48);
        fontMed   = new Font("SansSerif", Font.BOLD, 24);
        fontSmall = new Font("SansSerif", Font.PLAIN, 14);

        try {
            heartFull  = ImageIO.read(getClass().getResourceAsStream("/objects/heart_full.png"));
            heartHalf  = ImageIO.read(getClass().getResourceAsStream("/objects/heart_half.png"));
            heartBlank = ImageIO.read(getClass().getResourceAsStream("/objects/heart_blank.png"));
            keyIcon    = ImageIO.read(getClass().getResourceAsStream("/objects/key.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMessage(String msg) {
        message = msg;
        messageTimer = 150; // 2.5 giây
    }

    public void update() {
        if (messageTimer > 0) messageTimer--;

        // Điều hướng cursor title
        if (gp.gameState == GameState.TITLE) {
            if (gp.keyB.down && !downHeld) {
                titleCursor = (titleCursor + 1) % TITLE_OPTIONS.length;
                downHeld = true;
            } else if (!gp.keyB.down) downHeld = false;

            if (gp.keyB.up && !upHeld) {
                titleCursor = (titleCursor - 1 + TITLE_OPTIONS.length) % TITLE_OPTIONS.length;
                upHeld = true;
            } else if (!gp.keyB.up) upHeld = false;
        }
    }

    private boolean downHeld = false, upHeld = false;

    public void draw(Graphics2D g2) {
        // Chống aliasing chữ
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        switch (gp.gameState) {
            case TITLE    -> drawTitle(g2);
            case PLAYING  -> drawHUD(g2);
            case PAUSED   -> { drawHUD(g2); drawPause(g2); }
            case GAME_OVER -> drawGameOver(g2);
            case WIN      -> drawWin(g2);
        }
    }

    // ────────────────── HUD ──────────────────
    private void drawHUD(Graphics2D g2) {
        final int hs = 34; // heart size
        int halfHearts = gp.player.maxHp; // maxHp đơn vị = half-heart → maxHp/2 icons

        for (int i = 0; i < gp.player.maxHp / 2; i++) {
            int hx = 12 + i * (hs + 3);
            int hy = 12;
            // blank
            drawImg(g2, heartBlank, hx, hy, hs, hs);
            int filled = gp.player.hp - i * 2;
            if (filled >= 2) drawImg(g2, heartFull,  hx, hy, hs, hs);
            else if (filled == 1) drawImg(g2, heartHalf, hx, hy, hs, hs);
        }

        // Số chìa khóa
        drawImg(g2, keyIcon, 12, 52, 22, 22);
        g2.setFont(fontMed);
        g2.setColor(Color.WHITE);
        shadow(g2, "× " + gp.player.keys, 38, 70);

        // Thông điệp tạm thời
        if (messageTimer > 0) {
            float alpha = Math.min(1f, messageTimer / 30f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setFont(new Font("SansSerif", Font.BOLD, 19));
            g2.setColor(new Color(255, 230, 80));
            String[] lines = message.split("\n");
            int my = gp.depth - 70;
            FontMetrics fm = g2.getFontMetrics();
            for (String line : lines) {
                int mx = (gp.width - fm.stringWidth(line)) / 2;
                shadow(g2, line, mx, my);
                my += 26;
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Gợi ý điều khiển (mờ)
        g2.setFont(fontSmall);
        g2.setColor(new Color(200, 200, 200, 160));
        g2.drawString("WASD: Di chuyển  |  E: Tương tác  |  Space: Tấn công  |  ESC: Tạm dừng",
                10, gp.depth - 8);
    }

    // ────────────────── TITLE ──────────────────
    private void drawTitle(Graphics2D g2) {
        // Nền gradient
        GradientPaint gp2 = new GradientPaint(0, 0, new Color(10, 10, 35),
                0, this.gp.depth, new Color(30, 0, 50));
        g2.setPaint(gp2);
        g2.fillRect(0, 0, this.gp.width, this.gp.depth);

        // Tiêu đề
        g2.setFont(fontBig);
        drawCentered(g2, "⚔  DUNGEON QUEST  ⚔", 120, new Color(255, 215, 0));

        g2.setFont(new Font("SansSerif", Font.ITALIC, 20));
        drawCentered(g2, "Game của NVTS", 160, new Color(180, 180, 220));

        // Separator
        g2.setColor(new Color(100, 80, 150));
        g2.fillRect(this.gp.width / 4, 178, this.gp.width / 2, 2);

        // Menu
        g2.setFont(fontMed);
        int optY = 250;
        for (int i = 0; i < TITLE_OPTIONS.length; i++) {
            boolean sel = i == titleCursor;
            String prefix = sel ? "▶  " : "    ";
            Color c = sel ? new Color(255, 230, 80) : new Color(200, 200, 220);
            drawCentered(g2, prefix + TITLE_OPTIONS[i], optY, c);
            optY += 52;
        }

        // Hướng dẫn
        g2.setFont(fontSmall);
        drawCentered(g2, "WASD: Di chuyển   E: Tương tác   Space: Tấn công",
                this.gp.depth - 40, new Color(140, 140, 170));
        drawCentered(g2, "Phím Enter để chọn  •  Phím mũi tên để điều hướng",
                this.gp.depth - 20, new Color(120, 120, 150));
    }

    // ────────────────── PAUSE ──────────────────
    private void drawPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, gp.width, gp.depth);

        g2.setFont(fontBig);
        drawCentered(g2, "TẠM DỪNG", 190, Color.WHITE);

        g2.setFont(fontMed);
        drawCentered(g2, "[ ESC ]  Tiếp tục", 270, new Color(180, 230, 180));
        drawCentered(g2, "[ Q ]  Về menu chính", 318, new Color(220, 180, 180));
    }

    // ────────────────── GAME OVER ──────────────────
    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, gp.width, gp.depth);

        g2.setFont(fontBig);
        drawCentered(g2, "GAME OVER", 200, new Color(220, 40, 40));

        g2.setFont(fontMed);
        drawCentered(g2, "Bạn đã bị đánh bại...", 258, new Color(200, 160, 160));
        drawCentered(g2, "[ R ] Chơi lại    [ ESC ] Menu chính", 320, Color.WHITE);
    }

    // ────────────────── WIN ──────────────────
    private void drawWin(Graphics2D g2) {
        // Nền ánh vàng
        GradientPaint gp2 = new GradientPaint(0, 0, new Color(40, 30, 0),
                0, this.gp.depth, new Color(60, 50, 10));
        g2.setPaint(gp2);
        g2.fillRect(0, 0, this.gp.width, this.gp.depth);

        g2.setFont(fontBig);
        drawCentered(g2, "✨  CHIẾN THẮNG!  ✨", 180, new Color(255, 215, 0));

        g2.setFont(fontMed);
        drawCentered(g2, "Bạn đã mở được rương kho báu!", 245, Color.WHITE);
        drawCentered(g2, "Màn chơi hoàn thành!", 285, new Color(200, 220, 180));

        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        drawCentered(g2, "[ R ] Chơi lại    [ ESC ] Menu chính", 360, new Color(200, 200, 200));
    }

    // ────────────────── Helpers ──────────────────
    private void drawCentered(Graphics2D g2, String text, int y, Color color) {
        g2.setFont(g2.getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (gp.width - fm.stringWidth(text)) / 2;
        shadow(g2, text, x, y, color);
    }

    private void shadow(Graphics2D g2, String text, int x, int y) {
        shadow(g2, text, x, y, g2.getColor());
    }

    private void shadow(Graphics2D g2, String text, int x, int y, Color color) {
        Color prev = g2.getColor();
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(color);
        g2.drawString(text, x, y);
        g2.setColor(prev);
    }

    private void drawImg(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img != null) g2.drawImage(img, x, y, w, h, null);
    }

    // Cho phép GamePanel đặt cursor khi nhấn Enter ở title
    public int getTitleCursor() { return titleCursor; }
}
