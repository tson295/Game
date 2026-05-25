package Main;

import java.awt.Rectangle;
import Entity.Entity;
import objects.SuperObject;

public class CollisionCheck {
    private final GamePanel gp;

    public CollisionCheck(GamePanel gp) {
        this.gp = gp;
    }

    /** Kiểm tra hitbox có đè lên tile collidable không */
    public boolean isBlocked(Rectangle box) {
        int leftCol   = Math.floorDiv(box.x, gp.tileSize);
        int rightCol  = Math.floorDiv(box.x + box.width  - 1, gp.tileSize);
        int topRow    = Math.floorDiv(box.y, gp.tileSize);
        int bottomRow = Math.floorDiv(box.y + box.height - 1, gp.tileSize);

        if (leftCol < 0 || topRow < 0 || rightCol >= gp.maxWorldCol || bottomRow >= gp.maxWorldRow)
            return true;

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                int id = gp.tilesManager.mapTile[r][c];
                if (id >= 0 && id < gp.tilesManager.tiles.length) {
                    var t = gp.tilesManager.tiles[id];
                    if (t != null && t.collision) return true;
                }
            }
        }
        return false;
    }

    /** Kiểm tra hitbox có đè lên object có collision không */
    public boolean isBlockedByObject(Rectangle box) {
        for (SuperObject obj : gp.obj) {
            if (obj != null && obj.collision) {
                Rectangle objBox = new Rectangle(
                    obj.worldX + obj.solidArea.x,
                    obj.worldY + obj.solidArea.y,
                    obj.solidArea.width,
                    obj.solidArea.height
                );
                if (box.intersects(objBox)) return true;
            }
        }
        return false;
    }

    /** Phiên bản tiện dụng nhận entity + delta */
    public boolean isBlocked(Entity e, int dx, int dy) {
        Rectangle next = new Rectangle(
            e.worldX + e.solidArea.x + dx,
            e.worldY + e.solidArea.y + dy,
            e.solidArea.width, e.solidArea.height);
        return isBlocked(next);
    }
}
