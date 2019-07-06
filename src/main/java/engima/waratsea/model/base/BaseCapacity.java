package engima.waratsea.model.base;

import lombok.Getter;

public enum BaseCapacity {
    HAS_ROOM("Base has room"),
    REGION_FULL("Region does not have capacity"),
    BASE_FULL("Base does not have capacity");

    @Getter
    private String value;

    /**
     * Constructor.
     *
     * @param value String value of the enum.
     */
    BaseCapacity(final String value) {
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
