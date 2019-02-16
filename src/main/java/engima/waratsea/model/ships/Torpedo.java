package engima.waratsea.model.ships;

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
     * @param maxHealth The maximum health of the torpedo rating.
     */
    public Torpedo(final int maxHealth) {
        this.maxHealth = maxHealth;
        health = maxHealth;
    }
}
