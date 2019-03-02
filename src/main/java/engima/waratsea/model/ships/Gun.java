package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.GunData;

/**
 * Ship's gun.
 */
public class Gun {

    private final int maxHealth;

    private final ArmourType armour;

    private int health;


    /**
     * Constructor.
     *
     * @param data The persisted gun data.
     */
    public Gun(final GunData data) {
        this.maxHealth = data.getMaxHealth();
        this.armour = data.getArmour();

        this.health = maxHealth;
    }
}
