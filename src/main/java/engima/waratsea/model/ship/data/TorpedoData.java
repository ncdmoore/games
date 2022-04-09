package engima.waratsea.model.ship.data;

import lombok.Data;

/**
 * Torpedo data that is persisted.
 */
@Data
public class TorpedoData {
    private int maxHealth;
    private int health;
    private int maxNumber;
    private int number;

    /**
     * The default constructor.
     */
    public TorpedoData() {
        maxHealth = 0;
    }
}
