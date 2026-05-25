package objects;

public class Coin extends SuperObject {
    public int value = 1;

    public Coin() {
        this(1);
    }

    public Coin(int value) {
        this.value  = value;
        name        = "Coin";
        description = "Đồng xu  ×" + value;
        collision   = false;
        try { image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/coin_bronze.png")); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
