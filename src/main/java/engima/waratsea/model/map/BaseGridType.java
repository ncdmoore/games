package engima.waratsea.model.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class represents the type's or bases within the game. A base may be
 *
 *    only an airfield
 *    only a port
 *    or both an airfield and a port.
 */
@RequiredArgsConstructor
public enum BaseGridType {
    AIRFIELD("Airfield"),
    SEAPLANE("Seaplane"),
    PORT("Port"),
    BOTH("Airfield&Port");

    @Getter
    private final String value;

    /**
     * Get the lower case String version of the value.
     *
     * @return The String representation of this enum in lower case.
     */
    public String toLower() {
        return value.toLowerCase();
    }
}
