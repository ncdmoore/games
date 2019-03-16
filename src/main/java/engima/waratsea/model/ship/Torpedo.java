package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.TorpedoData;
import lombok.Getter;
import lombok.Setter;

/**
 * A ship's or submarine's torpedo.
 */
public class Torpedo {

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
        this.maxHealth = data.getMaxHealth();
        health = maxHealth;
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
}
