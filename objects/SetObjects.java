package objects;

import Main.GamePanel;

public class SetObjects {
    GamePanel gp;

    public SetObjects(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        gp.obj[0] = new Key();
        gp.obj[0].worldX = 23 * gp.tileSize;
        gp.obj[0].worldY = 13 * gp.tileSize;

        gp.obj[1] = new Key();
        gp.obj[1].worldX = 30 * gp.tileSize;
        gp.obj[1].worldY = 37 * gp.tileSize;

        gp.obj[2] = new Key();
        gp.obj[2].worldX = 38 * gp.tileSize;
        gp.obj[2].worldY = 9 * gp.tileSize;

        gp.obj[3] = new Door();
        gp.obj[3].worldX = 12 * gp.tileSize;
        gp.obj[3].worldY = 12 * gp.tileSize;

        gp.obj[4] = new Door();
        gp.obj[4].worldX = 36 * gp.tileSize;
        gp.obj[4].worldY = 40 * gp.tileSize;

        gp.obj[5] = new Door();
        gp.obj[5].worldX = 13 * gp.tileSize;
        gp.obj[5].worldY = 23 * gp.tileSize;

        gp.obj[6] = new Chest();
        gp.obj[6].worldX = 12 * gp.tileSize;
        gp.obj[6].worldY = 9 * gp.tileSize;
    }
}
