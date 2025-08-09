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

        if (keyB.up) {
            direction = "up";
            worldY -= speed;
            moving = true;
        } else if (keyB.down) {
            direction = "down";
            worldY += speed;
            moving = true;
        } else if (keyB.left) {
            direction = "left";
            worldX -= speed;
            moving = true;
        } else if (keyB.right) {
            direction = "right";
            worldX += speed;
            moving = true;
        }

        // Chỉ cập nhật ảnh nếu đang di chuyển
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 7) { // 8 ở đây là tốc độ đổi ảnh
                spriteNum++;
                if (spriteNum > 6) { // 6 là số ảnh cuối
                    spriteNum = 1; // quay lại ảnh đầu
                }
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; // hoặc ảnh đứng yên tuỳ bạn muốn
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
