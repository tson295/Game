package Entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Rectangle;
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
        solidArea = new Rectangle(8, 16, 32, 32); // Define the collision area
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
        int dx = 0, dy = 0;

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

        // Chuẩn hóa tốc độ khi đi chéo
        if (dx != 0 && dy != 0) {
            double scale = 1 / Math.sqrt(2);
            dx = (int) Math.round(dx * scale);
            dy = (int) Math.round(dy * scale);
        }

        // Cập nhật hướng
        if (dy < 0)
            direction = "up";
        else if (dy > 0)
            direction = "down";
        else if (dx < 0)
            direction = "left";
        else if (dx > 0)
            direction = "right";

        // === CẬP NHẬT SOLID AREA THEO WORLD HIỆN TẠI ===
        // (giả sử solidArea.x/y là offset tương đối so với worldX/Y)
        Rectangle hitboxNow = new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y,
                solidArea.width,
                solidArea.height);

        // === DI CHUYỂN THEO TỪNG TRỤC (PREDICTIVE CHECK) ===

        // 1) Trục X
        if (dx != 0) {
            Rectangle nextX = new Rectangle(hitboxNow);
            nextX.x += dx;
            boolean blockedX = gamePanel.collisionCheck.isBlocked(nextX); // bạn đổi tên theo API của bạn
            if (!blockedX) {
                worldX += dx;
                hitboxNow.x += dx; // nhớ cập nhật hitboxNow để dùng tiếp cho trục Y
            }
        }

        // 2) Trục Y
        if (dy != 0) {
            Rectangle nextY = new Rectangle(hitboxNow);
            nextY.y += dy;
            boolean blockedY = gamePanel.collisionCheck.isBlocked(nextY);
            if (!blockedY) {
                worldY += dy;
                hitboxNow.y += dy;
            }
        }

        // Animation
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 7) {
                spriteNum++;
                if (spriteNum > 6)
                    spriteNum = 1;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; // frame đứng yên
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
