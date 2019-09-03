package engima.waratsea.model.map;

/**
 * Each grid within the game map is composed one of the following.
 *
 *   Only Sea
 *   Only Land
 *   or both Sea and Land
 */
public enum GridType {
    SEA,
    LAND,
    BOTH
}
