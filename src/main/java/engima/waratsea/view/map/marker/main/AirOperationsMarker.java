package engima.waratsea.view.map.marker.main;

/**
 * Implement this interface to indicate support for air patrol and air mission markers.
 */
public interface AirOperationsMarker {
    /**
     * Highlight a patrol radius for this marker.
     *
     * @param radius The radius to highlight.
     */
    void highlightRadius(int radius);

    /**
     * Remove this marker's highlighted patrol radius.
     */
    void unhighlightRadius();
}
