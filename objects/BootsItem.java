package objects;

public class BootsItem extends EquipItem {
    public BootsItem() {
        name        = "Boots";
        description = "Giày nhanh  +2 SPD";
        slot        = "boots";
        speedBonus  = 2;
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/boots.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
