package engima.waratsea.model.squadron;

import lombok.Getter;

public enum PatrolType {
    ASW("ASW"),
    CAP("CAP"),
    SEARCH("Search");

    @Getter
    private String value;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     */
    PatrolType(final String value) {
        this.value = value;
    }
}
