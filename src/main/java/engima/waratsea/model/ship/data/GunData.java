package engima.waratsea.model.ship.data;

import engima.waratsea.model.ship.ArmourType;
import lombok.Data;

/**
 * Gun data that is persisted.
 */
@Data
public class GunData {
    private String name;
    private int maxHealth;
    private ArmourType armour;
    private int health;

    /**
     * The default constructor.
     */
    public GunData() {
        this.maxHealth = 0;
        armour = ArmourType.NONE;
    }
}
