package engima.waratsea.model.base.airfield;


import lombok.Getter;

import java.util.List;

public enum AirbaseType {
    LAND("land", "Airfield", "Land-based Squadrons"),                           // Land airfield. Only land and carrier aircraft can station here.
    SEAPLANE("seaplane", "Seaplane Base", "Seaplane Squadrons"),                // Seaplane base. Only seaplanes can station here.
    BOTH("both", "Airfield", "Both Squadrons"),                                 // Both land and seaplane. Land, carrier and seaplanes can station here.
    CARRIER("carrier", "Carrier", "Carrier-based Squadrons"),                   // Task force with a carrier. Only carrier aircraft can station here.
    SURFACE_SHIP("surfaceShip", "Surface Ship", "Float-plane Squadrons");       // Battleship or cruiser. Can carry sea planes.
    private final String value;

    @Getter private final String title;
    @Getter private final String squadronType;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     * @param title The title of the airfield type.
     * @param squadronType The type of squadrons stationed at this type of airbase.
     */
    AirbaseType(final String value, final String title, final String squadronType) {
        this.value = value;
        this.title = title;
        this.squadronType = squadronType;
    }

    public List<AirbaseType> expand() {
        return (this == BOTH) ? List.of(LAND, SEAPLANE) : List.of(this);
    }

    /**
     * Get the String value of this enum.
     *
     * @return The String representation of this enum.
     */
    public String toString() {
        return value;
    }
}
