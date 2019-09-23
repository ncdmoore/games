package engima.waratsea.model.base.airfield;

public enum AirfieldType {
    LAND("Land"),
    SEAPLANE("Seaplane"),
    BOTH("Land-Seaplane");

    private String value;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     */
    AirfieldType(final String value) {
        this.value = value;
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
