package engima.waratsea.model.base.airfield;

import lombok.Getter;

public enum AirfieldOperation {
    SUCCESS("Success"),
    REGION_FULL("Region does not have capacity"),
    BASE_FULL("Base does not have capacity"),
    LANDING_TYPE_NOT_SUPPORTED("Squadron landing type not allowed");

    @Getter
    private String value;

    /**
     * Constructor.
     *
     * @param value String value of the enum.
     */
    AirfieldOperation(final String value) {
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
