package objects;

public class Key extends SuperObject{
    public Key(){
        name = "Key";
        description = "[" + name + "]\nA small key that can unlock a door.";
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/key.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
