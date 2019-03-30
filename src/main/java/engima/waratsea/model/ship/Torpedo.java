package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.TorpedoData;
import lombok.Getter;
import lombok.Setter;

/**
 * A ship's or submarine's torpedo.
 */
public class Torpedo implements Component {
    @Getter
    private final String name;

    @Getter
    private final int maxHealth;

    @Getter
    @Setter
    private int health;

    /**
     * Constructor.
     *
     * @param data The persisted torpedo data.
     */
    public Torpedo(final TorpedoData data) {
        this.name = "Torpedo";
        this.maxHealth = data.getMaxHealth();
        health = data.getHealth();

        // A torpedo that is knocked out will have a health of -1.
        // Thus a value of 0 for health indicates this is a newly
        // created torpedo and the health is set to max health.
        if (health == 0) {
            health = maxHealth;
        }
    }

    /**
     * Get the torpedo's persistent data.
     *
     * @return The torpedo's persistent data.
     */
    public TorpedoData getData() {
        TorpedoData data = new TorpedoData();
        data.setMaxHealth(maxHealth);
        data.setHealth(health);
        return data;
    }

    /**
     * Determine if the component is present.
     *
     * @return True if the component is present. False otherwise.
     */
    @Override
    public boolean isPresent() {
        return maxHealth != 0;
    }
}
