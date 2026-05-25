package Entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {
    public int worldX, worldY;
    public int speed = 3;
    public int maxHp = 6, hp = 6;
    public int attack = 1, defense = 0;
    public boolean alive = true;
    public BufferedImage up[], down[], left[], right[];
    public String direction;
    public int spriteCounter = 0, spriteNum = 1;
    public Rectangle solidArea;
}
