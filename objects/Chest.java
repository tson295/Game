package objects;

public class Chest extends SuperObject {
    public boolean opened = false;

    public Chest() {
        name = "Chest";
        description = "[" + name + "]\nRương kho báu – mục tiêu cuối cùng!";
        collision = false;
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/chest.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
