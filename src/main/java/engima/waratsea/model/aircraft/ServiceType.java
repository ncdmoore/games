package engima.waratsea.model.aircraft;

/**
 * The aircraft's service.
 */
public enum ServiceType {
    AIR_FORCE("Air Force"),
    NAVY("Navy");

    private String value;

    /**
     * Constructor.
     *
     * @param value The String value of this enum.
     */
    ServiceType(final String value) {
        this.value = value;
    }

    /**
     * Returns the String value of this enum.
     *
     * @return The String value.
     */
    @Override
    public String toString() {
        return value;
    }
}
