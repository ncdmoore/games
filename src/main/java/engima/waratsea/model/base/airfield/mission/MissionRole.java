package engima.waratsea.model.base.airfield.mission;

import lombok.Getter;

public enum MissionRole {
    MAIN("Main"),
    ESCORT("Escort");

    @Getter
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
