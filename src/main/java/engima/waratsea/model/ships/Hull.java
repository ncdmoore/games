package engima.waratsea.model.ships;

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
     * @param maxHealth The initial health of the hull.
     * @param armour The armour rating of the hull.
     */
    public Hull(final int maxHealth, final ArmourType armour) {
        this.maxHealth = maxHealth;
        this.armour = armour;
    }
}
