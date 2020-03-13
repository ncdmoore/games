package engima.waratsea.model.base.airfield.mission;

public enum MissionRole {
    MAIN("Main"),
    ESCORT("Escort");

    private String value;

    /**
     * Constructor.
     *
     * @param value The String representation of this enum.
     */
    MissionRole(final String value) {
        this.value = value;
    }

    /**
     * The String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
