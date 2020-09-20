package engima.waratsea.model.victory;

public enum VictoryType {
    SHIP("Ship"),
    SQUADRON("Squadron"),
    AIRFIELD("Airfield");

    private final String value;

    VictoryType(final String value) {
        this.value = value;
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
