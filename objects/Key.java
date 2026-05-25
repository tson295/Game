package objects;

public class Key extends SuperObject {
    public Key() {
        name = "Key";
        description = "[" + name + "]\nChìa khóa nhỏ có thể mở cửa.";
        collision = false;
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/key.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
