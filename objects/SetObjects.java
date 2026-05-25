package objects;

import Entity.Enemy;
import Main.GamePanel;

public class SetObjects {
    final GamePanel gp;

    public SetObjects(GamePanel gp) { this.gp = gp; }

    // ── Dispatcher ───────────────────────────────────────────────────
    public void setObjectsForLevel(int level) {
        switch (level) {
            case 1  -> setObjectsLevel1();
            case 2  -> setObjectsLevel2();
            case 3  -> setObjectsLevel3();
            default -> setObjectsLevel1();
        }
    }

    public void setEnemiesForLevel(int level) {
        switch (level) {
            case 1  -> setEnemiesLevel1();
            case 2  -> setEnemiesLevel2();
            case 3  -> setEnemiesLevel3();
            default -> setEnemiesLevel1();
        }
    }

    // ── LEVEL 1 ──────────────────────────────────────────────────────
    private void setObjectsLevel1() {
        // Chìa khóa
        place(new Key(),  23, 13);
        place(new Key(),  30, 37);
        place(new Key(),  38,  9);

        // Cửa
        place(new Door(), 12, 12);
        place(new Door(), 36, 40);
        place(new Door(), 13, 23);

        // Rương kho báu
        place(new Chest(), 12,  9);

        // Thuốc
        place(new Potion(), 20, 10);
        place(new Potion(), 35, 35);
        place(new Potion(), 42, 18);
        place(new Potion(),  8, 30);

        // Vật phẩm trang bị (phần thưởng khám phá)
        place(new SwordItem(), 40, 45);
        place(new ShieldWood(), 7, 7);
        place(new BootsItem(), 45, 7);

        // NPC thương nhân
        place(new NPC(), 5, 3);

        // Portal sang Màn 2
        place(new Portal(2), 47, 47);
    }

    private void setEnemiesLevel1() {
        // Slime
        spawn(0, 10, 10);
        spawn(0, 15, 16);
        spawn(0, 20, 36);
        spawn(0,  8, 42);
        spawn(0, 40, 14);

        // Skeleton
        spawn(1, 32, 10);
        spawn(1, 36, 26);
        spawn(1, 44, 38);
        spawn(1, 28, 44);
        spawn(1, 18, 28);
    }

    // ── LEVEL 2 ──────────────────────────────────────────────────────
    private void setObjectsLevel2() {
        // Chìa khóa (nhiều hơn, khó tìm hơn)
        place(new Key(),  8, 42);
        place(new Key(), 40, 10);
        place(new Key(), 42, 42);

        // Cửa (3 cửa)
        place(new Door(), 25, 10);
        place(new Door(), 10, 25);
        place(new Door(), 40, 25);

        // Rương kho báu (trong phòng khó hơn)
        place(new Chest(), 25, 44);

        // Vật phẩm tốt hơn
        place(new Potion(), 15, 15);
        place(new Potion(), 35, 40);
        place(new AxeItem(), 8, 8);
        place(new ShieldBlue(), 42, 42);
        place(new LanternItem(), 25, 25);

        // NPC
        place(new NPC(), 5, 5);

        // Portal sang Màn 3
        place(new Portal(3), 47, 47);
    }

    private void setEnemiesLevel2() {
        // Nhiều Skeleton và Archer hơn, stats cao hơn
        int[][] slimes    = {{8,8},{12,20},{6,38},{42,12}};
        int[][] skeletons = {{20,8},{38,8},{8,28},{38,28},{20,40},{38,40},{10,45},{40,45}};
        int[][] archers   = {{25,5},{5,25},{45,25},{25,45},{15,15},{35,35}};

        for (int[] s : slimes)    spawn(0, s[0], s[1]);
        for (int[] s : skeletons) spawn(1, s[0], s[1]);
        for (int[] a : archers)   spawn(2, a[0], a[1]);

        // Scale stats cho level 2
        for (Enemy e : gp.enemies) {
            if (e != null) {
                e.maxHp = (int)(e.maxHp * 1.3);
                e.hp    = e.maxHp;
                e.attack += 1;
            }
        }
    }

    // ── LEVEL 3 – Boss Arena ─────────────────────────────────────────
    private void setObjectsLevel3() {
        // Không có key/door – chỉ cần giết Boss
        place(new Potion(), 5, 5);
        place(new Potion(), 44, 5);
        place(new Potion(), 5, 44);
        place(new Potion(), 44, 44);

        // Vật phẩm tốt nhất
        place(new AxeItem(),    5, 24);
        place(new ShieldBlue(), 44, 24);
        place(new BootsItem(),  24, 5);
        place(new LanternItem(), 24, 44);

        // NPC trước Boss arena
        place(new NPC(), 5, 5);

        // Chest + Portal xuất hiện sau khi Boss chết
        // (đặt sẵn nhưng xa, SetObjects sẽ đặt khi Boss chết)
        place(new Chest(), 25, 25);  // Giữa boss arena
    }

    private void setEnemiesLevel3() {
        // 4 Archer canh 4 góc
        spawn(2,  8,  8);
        spawn(2, 42,  8);
        spawn(2,  8, 42);
        spawn(2, 42, 42);

        // BOSS ở trung tâm
        Enemy boss = new Enemy(gp, 3);
        boss.worldX = 25 * gp.tileSize;
        boss.worldY = 25 * gp.tileSize;
        addEnemy(boss);
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private void place(SuperObject obj, int col, int row) {
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                gp.obj[i]  = obj;
                obj.worldX = col * gp.tileSize;
                obj.worldY = row * gp.tileSize;
                return;
            }
        }
    }

    private void spawn(int type, int col, int row) {
        Enemy e = new Enemy(gp, type);
        e.worldX = col * gp.tileSize;
        e.worldY = row * gp.tileSize;
        addEnemy(e);
    }

    private void addEnemy(Enemy e) {
        for (int i = 0; i < gp.enemies.length; i++) {
            if (gp.enemies[i] == null) { gp.enemies[i] = e; return; }
        }
    }
}
