package engima.waratsea.model.base.airfield.mission;

public enum MissionRole {
    FERRY("Ferry"),
    STRIKE("Strike"),
    ESCORT("Escort"),
    SWEEP("Sweep");

    private String value;

    /**
     * Constructor.
     *
     * @param value The String representation of this enum.
     */
    MissionRole(final String value) {
        this.value = value;
    }
}
