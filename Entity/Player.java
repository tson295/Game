package Entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import Main.GamePanel;
import Main.Move;

public class Player extends Entity {
    GamePanel gamePanel;
    Move keyB;
    public final int screenX, screenY;

    public Player(GamePanel gamePanel, Move keyB) {
        worldX = gamePanel.realPixel * 23; // Initial world X position
        worldY = gamePanel.realPixel * 21; // Initial world Y position
        this.gamePanel = gamePanel;
        this.keyB = keyB;
        screenX = gamePanel.width / 2 - gamePanel.realPixel / 2; // Initial x position
        screenY = gamePanel.depth / 2 - gamePanel.realPixel / 2; // Initial y position
        this.direction = "down"; // Initial direction
        down = new BufferedImage[7];
        up = new BufferedImage[7];
        left = new BufferedImage[7];
        right = new BufferedImage[7];
        getPlayerImage();
    }

    public void getPlayerImage() {
        try {
            for (int i = 0; i < 7; i++) {
                down[i] = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 1) + ".png"));
                up[i] = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 8) + ".png"));
                right[i] = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 15) + ".png"));
                left[i] = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 22) + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        boolean moving = false;

        int dx = 0;
        int dy = 0;

        if (keyB.up) {
            dy -= speed;
            moving = true;
        }
        if (keyB.down) {
            dy += speed;
            moving = true;
        }
        if (keyB.left) {
            dx -= speed;
            moving = true;
        }
        if (keyB.right) {
            dx += speed;
            moving = true;
        }

        // Nếu di chuyển chéo thì chuẩn hóa tốc độ (để không nhanh hơn)
        if (dx != 0 && dy != 0) {
            double scale = 1 / Math.sqrt(2); // ~0.707
            dx = (int) (dx * scale);
            dy = (int) (dy * scale);
        }

        worldX += dx;
        worldY += dy;

        // Cập nhật direction (ưu tiên hướng dọc trước, hoặc tuỳ bạn muốn)
        if (dy < 0)
            direction = "up";
        else if (dy > 0)
            direction = "down";
        else if (dx < 0)
            direction = "left";
        else if (dx > 0)
            direction = "right";

        // Animation
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 7) {
                spriteNum++;
                if (spriteNum > 6)
                    spriteNum = 1;
                spriteCounter = 0;;
            }
        } else {
            spriteNum = 0;
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (direction) {
            case "up":
                image = up[spriteNum];
                break;
            case "down":
                image = down[spriteNum];
                break;
            case "left":
                image = left[spriteNum];
                break;
            case "right":
                image = right[spriteNum];
                break;
        }
        int screenX = worldX - gamePanel.cameraX;
        int screenY = worldY - gamePanel.cameraY;

        g2.drawImage(image, screenX, screenY, gamePanel.tileSize, gamePanel.tileSize, null);
    }

}
