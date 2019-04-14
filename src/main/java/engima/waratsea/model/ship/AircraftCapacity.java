package engima.waratsea.model.ship;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an aircraft carrier or seaplane carrier's aircraft capacity.
 */
public class AircraftCapacity implements Component {
    @Getter
    private final String name;

    @Getter
    @Setter
    private int maxHealth;

    @Getter
    @Setter
    private int health;

    /**
     * The constructor.
     */
    public AircraftCapacity() {
        this.name = "Aircraft Capacity";
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
        return "steps";
    }
}
