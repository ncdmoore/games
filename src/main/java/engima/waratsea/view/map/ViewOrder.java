package engima.waratsea.view.map;

import lombok.Getter;

public enum ViewOrder {
    GRID(1000.0),
    GRID_DECORATION(450.0),
    RADIUS(600.0),
    MARKER(500.0),
    MARKER_DECORATION(400.0),
    POPUP(50.0);

    @Getter
    private final double value;

    /**
     * Constructor.
     *
     * @param value The z-value of the object on the map.
     */
    ViewOrder(final double value) {
        this.value = value;
    }
}
