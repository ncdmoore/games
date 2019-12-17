package engima.waratsea.view.map.marker.preview;

import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A target marker on the preview map.
 */
@Slf4j
public class TargetMarker {

    private static final double OPACITY = 1.0;

    private final String name;

    private final GridView gridView;
    private final EventHandler<? super MouseEvent> eventHandler;
    private Circle circle;

    @Getter
    private final PopUp popUp;

    /**
     * Construct a marker.
     * @param dto All the data needed to create a marker.
     */
    public TargetMarker(final TargetMarkerDTO dto) {
        this.name = dto.getName();
        this.gridView = dto.getGridView();
        this.eventHandler = dto.getMarkerEventHandler();
        dto.setStyle("popup-target");

        this.popUp = dto.isPopupShared() ?  dto.getPopup() : new PopUp(dto);
    }

    /**
     * Draw the marker on the provided map.
     * Register a mouse click callback for the marker.
     * @param dto Data transfer object.2
     */
    public void draw(final TargetMarkerDTO dto) {
        //indicates whether the popup contents are active or inactive. A target marker will be inactive
        //if it occupies the same space as a task force marker.
        boolean active = dto.isActive();

        if (active) {
            double radius = (double) gridView.getSize() / 2;
            circle = new Circle(gridView.getX() + radius, gridView.getY() + radius, radius);
            circle.getStyleClass().add("target-marker");

            circle.setViewOrder(ViewOrder.MARKER.getValue());


            circle.setUserData(this);


            setOnMouseClicked(eventHandler);
        }

        if (!dto.isPopupShared()) {
            popUp.draw(dto);
        }
    }

    /**
     * Select this marker. The marker is now the currently selected marker.
     * @param map The game map.
     */
    public void select(final MapView map) {
        if (circle != null) {
            map.remove(circle);
            map.add(circle);
            circle.setOpacity(1.0);
        }

        log.info("display target marker: '{}'", name);

        popUp.display(map);
    }

    /**
     * Clear this marker. The marker is no longer selected if it was selected.
     * @param map The game map.
     **/
    public void clear(final MapView map) {
        if (circle != null) {
            map.remove(circle);
            circle.setOpacity(OPACITY);
        }

        popUp.hide(map);
    }

    /**
     * Determine if this marker was clicked.
     * @param clickedMarker The marker that was clicked.
     * @return True if this marker was the marker that was clicked. False otherwise.
     */
    public boolean wasClicked(final Object clickedMarker) {
        return this.circle == clickedMarker;
    }

    /**
     * Register this marker for mouse clicks.
     * @param callback The method that is called when this marker is clicked.
     */
    private void setOnMouseClicked(final EventHandler<? super MouseEvent> callback) {
        circle.setOnMouseClicked(callback);
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     *
     * @param offset How much the y is adjusted once.
     * @param yThreshold Determines if the popup is near the bottom and needs to be moved up.
     **/
    public void adjustY(final int offset, final int yThreshold) {
        popUp.adjustY(offset, yThreshold);
    }

    /**
     * Get the map legend key. This is just a duplicate circle that is the same
     * size and type as used for this marker. It is used in the map legend.
     *
     * @param x The marker's x coordinate.
     * @param y The marker's y coordinate.
     * @param radius The radius of the marker.
     * @return The marker legend key.
     */
    public static Node getLegend(final double x, final double y, final double radius) {
        Circle c = new Circle(x + radius, y + radius, radius);
        c.getStyleClass().add("target-marker");
        return c;
    }
}
