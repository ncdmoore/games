package engima.waratsea.view.map;

import lombok.Getter;

public enum ViewOrder {
    GRID(100.0),
    MARKER(10.0),
    POPUP(5.0);

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
