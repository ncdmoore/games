package engima.waratsea.model.base.airfield;


import lombok.Getter;

public enum AirfieldType {
    LAND("land", "Airfield"),
    SEAPLANE("seaplane", "Seaplane Base"),
    BOTH("both", "Airfield");

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
