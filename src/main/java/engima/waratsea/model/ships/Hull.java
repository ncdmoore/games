package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.HullData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's hull.
 */
public class Hull {
    @Getter
    private final int maxHealth;

    @Getter
    private final ArmourType armour;

    @Getter
    @Setter
    private int health;

    /**
     * Constructor.
     *
     * @param data The persisted hull data.
     */
    public Hull(final HullData data) {
        this.maxHealth = data.getMaxHealth();
        this.armour = data.getArmour();
    }
}
