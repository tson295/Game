package objects;

public class Door extends SuperObject {
    public Door() {
        name = "Door";
        description = "[" + name + "]\nA sturdy door that can be opened with a key.";
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/door.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
