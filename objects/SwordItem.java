package objects;

public class SwordItem extends EquipItem {
    public SwordItem() {
        name        = "Sword";
        description = "Kiếm sắc bén  +3 ATK";
        slot        = "weapon";
        attackBonus = 3;
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/sword_normal.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
