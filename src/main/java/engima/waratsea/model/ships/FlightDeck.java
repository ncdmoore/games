package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.FlightDeckData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Aircraft carrier flight deck.
 */
public class FlightDeck {

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

    /**
     * Constructor.
     *
     * @param data The flight deck's persisted data.
     */
    public FlightDeck(final FlightDeckData data) {
        this.armour = data.getArmour();
        this.capacityList = data.getCapacityList();

        this.health = capacityList != null ? capacityList.size()  : 0;
    }

    /**
     * Return the flight deck's current capacity.
     *
     * @return The flight deck's current capacity.
     */
    public int getCapacity() {
        // The capacity list is zero based, so minus one from the health.
        return capacityList.get(health - 1);
    }
}
