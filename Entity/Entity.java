package Entity;

import java.awt.image.BufferedImage;

public class Entity {
    protected int x, y, speed = 3;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2, up[], down[], left[], right[];
    public String direction;
    public int spriteCounter = 0, spriteNum = 1, previousSpriteNum = 1;

    public Entity() {
        this.x = 0;
        this.y = 0;
        this.speed = 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
