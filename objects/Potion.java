package objects;

public class Potion extends SuperObject {
    public int healAmount = 2;

    public Potion() {
        name = "Potion";
        description = "[" + name + "]\nBình thuốc đỏ – hồi phục 2 HP.";
        collision = false;
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/potion_red.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
