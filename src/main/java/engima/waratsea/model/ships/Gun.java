package engima.waratsea.model.ships;

/**
 * Ship's gun.
 */
public class Gun {

    private final int maxHealth;

    private final ArmourType armour;

    private int health;

    /**
     * Constructor.
     * @param maxHealth The initial health of the gun.
     * @param armour The armour rating of the gun.
     */
    public Gun(final int maxHealth, final ArmourType armour) {
        this.maxHealth = maxHealth;
        this.armour = armour;

        this.health = maxHealth;
    }
}
