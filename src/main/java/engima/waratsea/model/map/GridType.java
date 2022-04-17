package engima.waratsea.model.map;

import lombok.RequiredArgsConstructor;

/**
 * Each grid within the game map is composed one of the following.
 *
 *   Only Sea
 *   Only Land
 *   or both Sea and Land
 */
@RequiredArgsConstructor
public enum GridType {
    SEA_DEEP("Deep sea"),
    SEA_SHALLOW("Shallow sea"),
    LAND("Land"),
    BOTH("Coast");

    private final String title;

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
