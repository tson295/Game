package objects;

public class LanternItem extends EquipItem {
    public LanternItem() {
        name        = "Lantern";
        description = "Đèn lồng  Tầm sáng ×2";
        slot        = "lantern";
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/lantern.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
