package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.HullData;
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

    /**
     * Get the hull's persistent data.
     *
     * @return The hull's persistent data.
     */
    public HullData getData() {
        HullData data = new HullData();
        data.setMaxHealth(maxHealth);
        data.setArmour(armour);
        data.setHealth(health);
        return data;
    }
}
