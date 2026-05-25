package objects;

public class ShieldWood extends EquipItem {
    public ShieldWood() {
        name         = "WoodShield";
        description  = "Khiên gỗ  +1 DEF";
        slot         = "shield";
        defenseBonus = 1;
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/shield_wood.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
