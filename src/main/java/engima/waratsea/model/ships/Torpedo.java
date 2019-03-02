package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.TorpedoData;
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
}
