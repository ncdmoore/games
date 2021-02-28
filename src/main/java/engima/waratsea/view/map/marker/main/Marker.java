package engima.waratsea.view.map.marker.main;

/**
 * Represents a game marker shown on the game map.
 */
public interface Marker {
    /**
     * Set the marker as the current active marker. Only a single marker may be the active marker.
     */
    void setActive();

    /**
     * Set the marker as inactive.
     */
    void setInactive();
}
