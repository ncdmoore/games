package engima.waratsea.model.map;

import lombok.Getter;

/**
 * This class represents the type's or bases within the game. A base may be
 *
 *    only an airfield
 *    only a port
 *    or both an airfield and a port.
 */
public enum BaseGridType {
    AIRFIELD("Airfield"),
    SEAPLANE("Seaplane"),
    PORT("Port"),
    BOTH("Airfield&Port");

    @Getter
    private String value;

    /**
     * Constructor.
     *
     * @param value The string value of the enum.
     */
    BaseGridType(final String value) {
        this.value = value;
    }

    /**
     * Get the lower case String version of the value.
     *
     * @return The String representation of this enum in lower case.
     */
    public String toLower() {
        return value.toLowerCase();
    }
}
