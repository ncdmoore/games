package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.HullData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's hull.
 */
public class Hull implements Component {
    @Getter
    private final String name;

    @Getter
    private final int maxHealth;

    @Getter
    private final ArmourType armour;

    @Getter
    private final boolean deck;

    @Getter
    @Setter
    private int health;

    /**
     * Constructor.
     *
     * @param data The persisted hull data.
     */
    public Hull(final HullData data) {
        this.name = "Hull";
        this.maxHealth = data.getMaxHealth();
        this.health = data.getHealth();
        this.armour = data.getArmour();
        this.deck = data.isDeck();

        // If the health is 0 then this is a newly built ship.
        // Note, sunk ships have a health of -1. So a built ship
        // should never have a health of 0.
        if (health == 0) {
            health = maxHealth;
        }
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

    /**
     * The hull is always present.
     *
     * @return True.
     */
    @Override
    public boolean isPresent() {
        return true;
    }
}
