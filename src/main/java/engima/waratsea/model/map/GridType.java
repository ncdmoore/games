package engima.waratsea.model.map;

/**
 * Each grid within the game map is composed one of the following.
 *
 *   Only Sea
 *   Only Land
 *   or both Sea and Land
 */
public enum GridType {
    SEA_DEEP("Deep sea"),
    SEA_SHALLOW("Shallow sea"),
    LAND("Land"),
    BOTH("Coast");

    private final String title;

    GridType(final String title) {
        this.title = title;
    }

    /**
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return title;
    }
}
