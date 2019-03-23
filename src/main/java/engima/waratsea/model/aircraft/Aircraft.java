package engima.waratsea.model.aircraft;

/**
 * Represents an aircraft.
 */
public interface Aircraft {

    /**
     * Get the aircraft's name.
     *
     * @return The aircraft's name.
     */
    String getName();

    /**
     * Get the aircraft's type.
     *
     * @return The aircraft's type.
     */
    AircraftType getType();

}
