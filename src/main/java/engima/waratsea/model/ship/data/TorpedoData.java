package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Torpedo data that is persisted.
 */
public class TorpedoData {
    @Getter
    @Setter
    private int maxHealth;

    @Getter
    @Setter
    private int health;

    /**
     * The default constructor.
     */
    public TorpedoData() {
        maxHealth = 0;
        health = 0;
    }
}
