package objects;

public class Chest extends SuperObject {
    public Chest(){
        name = "Chest";
        description = "[" + name + "]\nA wooden chest that can be opened to reveal its contents.";
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/chest.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
