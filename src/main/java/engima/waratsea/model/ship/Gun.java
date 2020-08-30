package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.GunData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's gun.
 */
public class Gun implements Component {
    @Getter
    private final String name;

    @Getter
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
        this.name = data.getName();
        this.maxHealth = data.getMaxHealth();
        this.armour = data.getArmour();
        this.health = data.getHealth();
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

    /**
     * Determine if the component is present.
     *
     * @return True if the component is present. False otherwise.
     */
    @Override
    public boolean isPresent() {
        return maxHealth != 0;
    }

    /**
     * Get the component's units.
     *
     * @return The component's units.
     */
    @Override
    public String getUnits() {
        return "";
    }
}
