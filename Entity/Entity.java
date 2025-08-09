package Entity;

import java.awt.image.BufferedImage;

public class Entity {
    public int worldX, worldY, speed = 3;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2, up[], down[], left[], right[];
    public String direction;
    public int spriteCounter = 0, spriteNum = 1;
}
