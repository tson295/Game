package objects;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import Main.GamePanel;

public class SuperObject {
    public String name;
    public String description;
    public int worldX, worldY;
    public BufferedImage image;
    public boolean collision = false;
    public void draw(Graphics2D g2, GamePanel gp){
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if(worldX + gp.realPixel > gp.player.worldX - gp.player.screenX &&
           worldX - gp.realPixel < gp.player.worldX + gp.player.screenX &&
           worldY + gp.realPixel > gp.player.worldY - gp.player.screenY &&
           worldY - gp.realPixel < gp.player.worldY + gp.player.screenY){
            g2.drawImage(image, screenX, screenY, gp.realPixel, gp.realPixel, null);
        }
    }
}
