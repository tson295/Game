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

    public Player(GamePanel gamePanel, Move keyB) {
        super(); // Call the constructor of Entity to initialize x, y, and speed
        this.gamePanel = gamePanel;
        this.keyB = keyB;
        this.x = 100; // Initial x position
        this.y = 100; // Initial y position
        this.direction = "down"; // Initial direction
        down = new BufferedImage[7];
        getPlayerImage();
    }

    public void getPlayerImage() {
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_up_2.png"));
            for (int i = 0; i < 7; i++) {
                down[i] = ImageIO.read(getClass().getResourceAsStream("/player/" + (i + 1) + ".png"));
            }

            down1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_down_2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_left_1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_left_2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_right_2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        boolean moving = false;

        if (keyB.up) {
            direction = "up";
            y -= speed;
            moving = true;
        } else if (keyB.down) {
            direction = "down";
            y += speed;
            moving = true;
        } else if (keyB.left) {
            direction = "left";
            x -= speed;
            moving = true;
        } else if (keyB.right) {
            direction = "right";
            x += speed;
            moving = true;
        }

        // Chỉ cập nhật ảnh nếu đang di chuyển
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 7) { // 8 ở đây là tốc độ đổi ảnh
                spriteNum++;
                if (spriteNum > 7) { // 7 là số ảnh cuối
                    spriteNum = 1; // quay lại ảnh đầu
                }
                spriteCounter = 0;
                previousSpriteNum = spriteNum;
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

        g2.drawImage(image, x, y, gamePanel.realPixel, gamePanel.realPixel, null);
    }

}
