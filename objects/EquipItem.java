package objects;

/**
 * Base class cho các vật phẩm có thể trang bị.
 * Mỗi slot: "weapon", "shield", "boots", "lantern"
 */
public class EquipItem extends SuperObject {
    public int attackBonus  = 0;
    public int defenseBonus = 0;
    public int speedBonus   = 0;
    public String slot      = "weapon"; // "weapon" | "shield" | "boots" | "lantern"
}
