// Main/CollisionCheck.java
package Main;

import java.awt.Rectangle;
import Entity.Entity;

public class CollisionCheck {
    private final GamePanel gp;

    public CollisionCheck(GamePanel gp) {
        this.gp = gp;
    }

    /** Kiểm tra 1 hitbox (tọa độ world) có đè lên tile collidable không */
    public boolean isBlocked(Rectangle box) {
        // Lấy kích thước map
        int rows = gp.maxWorldRow;
        int cols = gp.maxWorldCol;

        // Quy đổi hitbox -> chỉ số hàng/cột bị phủ
        int leftCol = Math.floorDiv(box.x, gp.tileSize);
        int rightCol = Math.floorDiv(box.x + box.width - 1, gp.tileSize);
        int topRow = Math.floorDiv(box.y, gp.tileSize);
        int bottomRow = Math.floorDiv(box.y + box.height - 1, gp.tileSize);

        // Ra ngoài biên bản đồ => chặn (tùy game logic của bạn)
        if (leftCol < 0 || topRow < 0 || rightCol >= cols || bottomRow >= rows) {
            return true;
        }

        // Quét các tile mà hitbox phủ lên
        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                int tileId = gp.tilesManager.mapTile[r][c];
                // Phòng null và id lệch
                if (tileId >= 0 && tileId < gp.tilesManager.tiles.length) {
                    var t = gp.tilesManager.tiles[tileId];
                    if (t != null && t.collision)
                        return true;
                }
            }
        }
        return false;
    }

    /** Kiểm tra entity có bị chặn nếu di chuyển (dx, dy) không */
    public boolean isBlocked(Entity e, int dx, int dy) {
        Rectangle next = new Rectangle(
                e.worldX + e.solidArea.x + dx,
                e.worldY + e.solidArea.y + dy,
                e.solidArea.width,
                e.solidArea.height);
        return isBlocked(next);
    }
}
