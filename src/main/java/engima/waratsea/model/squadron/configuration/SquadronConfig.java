package engima.waratsea.model.squadron.configuration;

/**
 * The configuration of the squadron. These are mutually exclusive configuration.
 *
 */
public enum SquadronConfig {
    NONE("Basic"),                // No special configuration.
    DROP_TANKS("Drop Tanks"),     // The squadron is equipped with drop tanks - this extends the squadron's range. Exclusive to fighters.
    LEAN_ENGINE("Long Range");    // The squadron is both equipped with extra internal fuel and is running very lean to extend its range. Any payload is reduced.

    private String value;

    /**
     * Constructor.
     *
     * @param value The String value of this enum.
     */
    SquadronConfig(final String value) {
        this.value = value;
    }

    /**
     * The String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    public String toString() {
        return value;
    }
}
