package engima.waratsea.view.map;

import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * A target marker on the preview map.
 */
public class TargetMarker {

    private final double opacity = 1.0;

    private final GridView gridView;
    private final EventHandler<? super MouseEvent> eventHandler;
    private final PopUp popUp;

    private Circle circle;

    /**
     * Construct a marker.
     * @param dto All the data needed to create a marker.
     */
    public TargetMarker(final TargetMarkerDTO dto) {
        this.gridView = dto.getGridView();
        this.eventHandler = dto.getMarkerEventHandler();
        dto.setStyle("popup-target");
        this.popUp = new PopUp(dto);
    }

    /**
     * Draw the marker on the provided map.
     * Register a mouse click callback for the marker.
     *
     * @param map The map where the marker is drawn.
     * @param active Indicates whether the popup contents are active or inactive. A target marker will be inactive
     *               if it occupies the same space as a task force marker.
     */
    public void draw(final Group map, final boolean active) {

        if (active) {
            double radius = (double) gridView.getSize() / 2;

            circle = new Circle(gridView.getX() + radius, gridView.getY() + radius, radius);
            circle.setStroke(Color.RED);
            circle.setFill(Color.RED);
            circle.setOpacity(opacity);

            setOnMouseClicked(eventHandler);
        }

        popUp.draw(active);
    }


    /**
     * Select this marker. The marker is now the currently selected marker.
     *
     * @param map The game map.
     */
    public void select(final Group map) {
        if (circle != null) {
            map.getChildren().remove(circle);
            map.getChildren().add(circle);
            circle.setOpacity(1.0);
        }
        popUp.display(map);
    }

    /**
     * Clear this marker. The marker is no longer selected if it was selected.
     *
     * @param map The game map.
     **/
    public void clear(final Group map) {
        if (circle != null) {
            map.getChildren().remove(circle);
            circle.setOpacity(opacity);
        }
        popUp.hide(map);
    }

    /**
     * Determine if this marker was clicked.
     *
     * @param clickedMarker The marker that was clicked.
     * @return True if this marker was the marker that was clicked. False otherwise.
     */
    public boolean wasClicked(final Object clickedMarker) {
        return this.circle == clickedMarker;
    }

    /**
     * Register this marker for mouse clicks.
     *
     * @param callback The method that is called when this marker is clicked.
     */
    private void setOnMouseClicked(final EventHandler<? super MouseEvent> callback) {
        circle.setOnMouseClicked(callback);
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     *
     * @param scale How much the y is adjusted per text item in the popup.
     **/
    public void adjustY(final int scale) {
        popUp.adjustY(scale);
    }

    /**
     * Determine if the pop up is near the bottom of the map.
     *
     * @param yThreshold The y threshold for which popups are moved upward to avoid running off the bottom of the map.
     * @return True if the popup is near the bottom of the map.
     */
    public boolean isPopUpNearMapBotton(final int yThreshold) {
        return popUp.getY() > yThreshold;
    }
}
