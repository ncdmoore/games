package engima.waratsea.model.aircraft;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the base aircraft type. This represents the aircraft's main purpose; the role in which it is best suited.
 */
public enum AircraftBaseType {
    RECON("Recon"),
    FIGHTER("Fighter"),
    BOMBER("Bomber"),
    DIVE_BOMBER("Dive Bomber"),
    TORPEDO_BOMBER("Torpedo Bomber");

    private static final List<AircraftBaseType> VALUES = Arrays.asList(AircraftBaseType.values());

    private final String value;

    /**
     * The constructor.
     *
     * @param value The String value of this enum.
     */
    AircraftBaseType(final String value) {
        this.value = value;
    }

    /**
     * This is used to loop through the enum values. This returns the next value
     * in the list based off the current value. If the end of the list is reached
     * then the head of the list is returned.
     *
     * @return The next reference in the enum values list.
     */
    public AircraftBaseType next() {
        int index = VALUES.indexOf(this);

        if (index + 1 == VALUES.size()) {
            index = 0;
        } else {
            index++;
        }

        return VALUES.get(index);
    }

    /**
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
