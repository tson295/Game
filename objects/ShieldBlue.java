package objects;

public class ShieldBlue extends EquipItem {
    public ShieldBlue() {
        name         = "BlueShield";
        description  = "Khiên xanh  +2 DEF";
        slot         = "shield";
        defenseBonus = 2;
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/shield_blue.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
