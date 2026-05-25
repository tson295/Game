package objects;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import Main.GamePanel;

public class SuperObject {
    public String name;
    public String description;
    public int worldX, worldY;
    public BufferedImage image;
    public boolean collision = false;

    /** Vùng va chạm tương đối so với (worldX, worldY) */
    public Rectangle solidArea = new Rectangle(4, 4, 40, 40);

    public void draw(Graphics2D g2, GamePanel gp) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        // Chỉ vẽ nếu nằm trong vùng nhìn thấy
        if (screenX + gp.tileSize < 0 || screenX > gp.width ||
            screenY + gp.tileSize < 0 || screenY > gp.depth) return;

        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}
