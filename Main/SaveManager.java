package Main;

import java.io.*;
import java.util.Properties;
import objects.*;

/**
 * Lưu và tải tiến trình game vào file save.dat.
 */
public class SaveManager {

    private static final String FILE = "save.dat";

    public static boolean hasSave() {
        return new File(FILE).exists();
    }

    public static void deleteSave() {
        new File(FILE).delete();
    }

    // ── Save ──────────────────────────────────────────────────────────
    public static void save(GamePanel gp) {
        Properties p = new Properties();
        Entity.Player pl = gp.player;

        p.setProperty("currentLevel",    String.valueOf(gp.currentLevel));
        p.setProperty("playerLevel",     String.valueOf(pl.level));
        p.setProperty("xp",              String.valueOf(pl.xp));
        p.setProperty("xpToNextLevel",   String.valueOf(pl.xpToNextLevel));
        p.setProperty("hp",              String.valueOf(pl.hp));
        p.setProperty("maxHp",           String.valueOf(pl.maxHp));
        p.setProperty("attack",          String.valueOf(pl.attack));
        p.setProperty("defense",         String.valueOf(pl.defense));
        p.setProperty("speed",           String.valueOf(pl.speed));
        p.setProperty("keys",            String.valueOf(pl.keys));
        p.setProperty("coins",           String.valueOf(pl.coins));
        p.setProperty("worldX",          String.valueOf(pl.worldX));
        p.setProperty("worldY",          String.valueOf(pl.worldY));
        p.setProperty("weapon",  pl.equippedWeapon  != null ? pl.equippedWeapon.name  : "none");
        p.setProperty("shield",  pl.equippedShield  != null ? pl.equippedShield.name  : "none");
        p.setProperty("boots",   pl.equippedBoots   != null ? pl.equippedBoots.name   : "none");
        p.setProperty("lantern", pl.equippedLantern != null ? pl.equippedLantern.name : "none");

        try (OutputStream os = new FileOutputStream(FILE)) {
            p.store(os, "Dungeon Quest Save File");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Load ──────────────────────────────────────────────────────────
    public static void load(GamePanel gp) {
        Properties p = new Properties();
        try (InputStream is = new FileInputStream(FILE)) {
            p.load(is);
        } catch (IOException e) { e.printStackTrace(); return; }

        int level = Integer.parseInt(p.getProperty("currentLevel", "1"));
        gp.loadLevel(level);   // resets world, keeps player ref

        Entity.Player pl = gp.player;
        pl.level          = Integer.parseInt(p.getProperty("playerLevel",   "1"));
        pl.xp             = Integer.parseInt(p.getProperty("xp",            "0"));
        pl.xpToNextLevel  = Integer.parseInt(p.getProperty("xpToNextLevel", "10"));
        pl.hp             = Integer.parseInt(p.getProperty("hp",            "6"));
        pl.maxHp          = Integer.parseInt(p.getProperty("maxHp",         "6"));
        pl.attack         = Integer.parseInt(p.getProperty("attack",        "2"));
        pl.defense        = Integer.parseInt(p.getProperty("defense",       "0"));
        pl.speed          = Integer.parseInt(p.getProperty("speed",         "3"));
        pl.keys           = Integer.parseInt(p.getProperty("keys",          "0"));
        pl.coins          = Integer.parseInt(p.getProperty("coins",         "0"));
        pl.worldX         = Integer.parseInt(p.getProperty("worldX",        String.valueOf(gp.tileSize * 23)));
        pl.worldY         = Integer.parseInt(p.getProperty("worldY",        String.valueOf(gp.tileSize * 21)));

        pl.equippedWeapon  = makeItem(p.getProperty("weapon",  "none"));
        pl.equippedShield  = makeItem(p.getProperty("shield",  "none"));
        pl.equippedBoots   = makeItem(p.getProperty("boots",   "none"));
        pl.equippedLantern = makeItem(p.getProperty("lantern", "none"));
    }

    private static SuperObject makeItem(String name) {
        return switch (name) {
            case "Sword"      -> new SwordItem();
            case "Axe"        -> new AxeItem();
            case "WoodShield" -> new ShieldWood();
            case "BlueShield" -> new ShieldBlue();
            case "Boots"      -> new BootsItem();
            case "Lantern"    -> new LanternItem();
            default           -> null;
        };
    }
}
