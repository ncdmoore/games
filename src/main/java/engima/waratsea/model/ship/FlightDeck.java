package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.FlightDeckData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Aircraft carrier flight deck.
 */
public class FlightDeck implements Component {

    @Getter
    private String name;

    private static final int MAX = 0;

    @Getter
    private final ArmourType armour;

    // The maximum capacity. This is fixed and never changes.
    @Getter
    private final List<Integer> capacityList;    // Capacity in steps.

    // The current flight deck health. The health is an index into the capacity list
    @Getter
    @Setter
    private int health;

    @Getter
    private final int maxHealth;

    /**
     * Constructor.
     *
     * @param data The flight deck's persisted data.
     */
    public FlightDeck(final FlightDeckData data) {
        this.name = "FlightDeck";
        this.armour = data.getArmour();
        this.capacityList = data.getCapacityList();
        this.health =  data.getHealth();
        this.maxHealth = capacityList.size();

        // The only time the health is zero is on initial creation of the flight deck.
        // Note, when a flight deck is knocked out it's value is set to -1.
        if (health == 0) {
            health = maxHealth;
        }
    }

    /**
     * Get the fight deck's persistent data.
     *
     * @return The flight deck's persistent data.
     */
    public FlightDeckData getData() {
        FlightDeckData data = new FlightDeckData();
        data.setArmour(armour);
        data.setCapacityList(capacityList);
        data.setHealth(health);
        return data;
    }

    /**
     * Return the flight deck's current aircraft capacity.
     *
     * @return The flight deck's current aircraft capacity.
     */
    public int getCapacity() {
        // The capacity list is zero based, so minus one from the health.
        return capacityList.get(health - 1);
    }

    /**
     * Retrun the flight deck's maximum aircraft capacity.
     *
     * @return The flight deck's macimum aircraft capacity.
     */
    public int getMaxCapacity() {
        // The capacity list is zero based, so minus one from the max health.
        return capacityList.get(maxHealth - 1);
    }

    /**
     * Determine if the component is present.
     *
     * @return True if the component is present. False otherwise.
     */
    @Override
    public boolean isPresent() {
        return true;
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
