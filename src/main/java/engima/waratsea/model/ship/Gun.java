package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.GunData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's gun.
 */
public class Gun {

    private final int maxHealth;

    @Getter
    private final ArmourType armour;

    @Getter
    @Setter
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

    /**
     * Get the gun persistent data.
     *
     * @return The gun's persistent data.
     */
    public GunData getData() {
        GunData data = new GunData();
        data.setMaxHealth(maxHealth);
        data.setArmour(armour);
        data.setHealth(health);
        return data;
    }
}
