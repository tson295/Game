package objects;

public class AxeItem extends EquipItem {
    public AxeItem() {
        name        = "Axe";
        description = "Rìu nặng  +5 ATK  -1 SPD";
        slot        = "weapon";
        attackBonus = 5;
        speedBonus  = -1;
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/axe.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
