package engima.waratsea.model.base.airfield;


import lombok.Getter;

public enum AirfieldType {
    LAND("land", "Airfield"),                   // Land airfield. Only land and carrier aircraft can station here.
    SEAPLANE("seaplane", "Seaplane Base"),      // Seaplane base. Only seaplanes can station here.
    BOTH("both", "Airfield"),                   // Both land and seaplane. Land, carrier and seaplanes can station here.
    TASKFORCE("taskforce", "Task Force");       // Task force with a carrier. Only carrier aircraft can station here.

    private String value;

    @Getter
    private String title;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     * @param title The title of the airfield type.
     */
    AirfieldType(final String value, final String title) {
        this.value = value;
        this.title = title;
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
