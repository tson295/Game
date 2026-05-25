package objects;

import java.awt.Rectangle;

public class Door extends SuperObject {
    public Door() {
        name = "Door";
        description = "[" + name + "]\nCánh cửa chắc chắn, cần chìa khóa để mở.";
        collision = true;  // Chặn đường đi
        solidArea = new Rectangle(0, 0, 48, 48);
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/door.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
