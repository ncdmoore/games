package engima.waratsea.model.ship.data;

import engima.waratsea.model.ship.ArmourType;
import lombok.Getter;
import lombok.Setter;

/**
 * Gun data that is persisted.
 */
public class GunData {
    @Getter
    @Setter
    private int maxHealth;

    @Getter
    @Setter
    private ArmourType armour;

    @Getter
    @Setter
    private int health;

    /**
     * The default constructor.
     */
    public GunData() {
        this.maxHealth = 0;
        armour = ArmourType.NONE;
    }
}
