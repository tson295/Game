package objects;

import Entity.Enemy;
import Main.GamePanel;

public class SetObjects {
    final GamePanel gp;

    public SetObjects(GamePanel gp) {
        this.gp = gp;
    }

    // ──────────────────────── OBJECTS ────────────────────────
    public void setObject() {
        int ts = gp.tileSize;

        // ── Chìa khóa ──
        place(new Key(),  23, 13);
        place(new Key(),  30, 37);
        place(new Key(),  38,  9);

        // ── Cửa ──
        place(new Door(), 12, 12);
        place(new Door(), 36, 40);
        place(new Door(), 13, 23);

        // ── Rương kho báu (mục tiêu) ──
        place(new Chest(), 12, 9);

        // ── Thuốc hồi máu ──
        place(new Potion(), 20, 10);
        place(new Potion(), 35, 35);
        place(new Potion(), 42, 18);
        place(new Potion(), 8,  30);
    }

    private void place(SuperObject obj, int col, int row) {
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                gp.obj[i] = obj;
                obj.worldX = col * gp.tileSize;
                obj.worldY = row * gp.tileSize;
                return;
            }
        }
    }

    // ──────────────────────── ENEMIES ────────────────────────
    public void setEnemies() {
        // Slime (type 0)
        spawnEnemy(0, 10, 10);
        spawnEnemy(0, 15, 16);
        spawnEnemy(0, 20, 36);
        spawnEnemy(0,  8, 42);
        spawnEnemy(0, 40, 14);

        // Skeleton (type 1)
        spawnEnemy(1, 32, 10);
        spawnEnemy(1, 36, 26);
        spawnEnemy(1, 44, 38);
        spawnEnemy(1, 28, 44);
        spawnEnemy(1, 18, 28);
    }

    private void spawnEnemy(int type, int col, int row) {
        for (int i = 0; i < gp.enemies.length; i++) {
            if (gp.enemies[i] == null) {
                Enemy e = new Enemy(gp, type);
                e.worldX = col * gp.tileSize;
                e.worldY = row * gp.tileSize;
                gp.enemies[i] = e;
                return;
            }
        }
    }
}
