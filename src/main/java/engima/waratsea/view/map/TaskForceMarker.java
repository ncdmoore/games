package engima.waratsea.view.map;

import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;

/**
 * The class represents a marker on a map.
 */
@Slf4j
public class TaskForceMarker {

    private final double opacity = 0.3;

    private final GridView gridView;
    private final EventHandler<? super MouseEvent> eventHandler;
    private final PopUp popUp;

    private Rectangle rectangle;

    /**
     * Construct a marker.
     * @param dto All the data needed to create a marker.
     */
    public TaskForceMarker(final TaskForceMarkerDTO dto) {
        this.gridView = dto.getGridView();
        this.eventHandler = dto.getMarkerEventHandler();
        dto.setStyle("popup-taskforce");
        this.popUp = new PopUp(dto);
    }

    /**
     * Draw the marker on the provided map.
     * Register a mouse click callback for the marker.
     * @param map The map where the marker is drawn.
     * @param active Indicates whether the popup contents are active or inactive.
     */
    public void draw(final Group map, final boolean active) {
        rectangle = new Rectangle(gridView.getX(), gridView.getY(), gridView.getSize(), gridView.getSize());
        rectangle.setStroke(Color.BLACK);
        rectangle.setOpacity(opacity);

        setOnMouseClicked(eventHandler);

        map.getChildren().add(rectangle);

        popUp.draw(active);
    }

    /**
     * Add text to the marker's corresponding popup.
     * @param name The text to add.
     * @param active Indicates if the name is active or inactive.
     */
    public void addText(final String name, final boolean active) {
        popUp.addText(name, active);
    }

    /**
     * Select this marker. The marker is now the currently selected marker.
     *
     * @param map The game map.
     * @param name The name of the task force.
     */
    public void select(final Group map, final String name) {
        rectangle.setOpacity(1.0);
        popUp.display(map, name);
    }

    /**
     * Clear this marker. The marker is no longer selected if it was selected.
     * @param map The game map.
     **/
    public void clear(final Group map) {
        rectangle.setOpacity(opacity);
        popUp.hide(map);
    }

    /**
     * Determine if this marker was clicked.
     * @param clickedMarker The marker that was clicked.
     * @return True if this marker was the marker that was clicked. False otherwise.
     */
    public boolean wasClicked(final Object clickedMarker) {
        return this.rectangle == clickedMarker;
    }

    /**
     * Register this marker for mouse clicks.
     * @param callback The method that is called when this marker is clicked.
     */
    private void setOnMouseClicked(final EventHandler<? super MouseEvent> callback) {
        rectangle.setOnMouseClicked(callback);
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     * @param scale How much the y is adjusted per text item in the popup.
     **/
    public void adjustY(final int scale) {
        popUp.adjustY(scale);
    }

    /**
     * Determine if the pop up is near the bottom of the map.
     * @param yThreshold The y threshold for which popups are moved upward to avoid running off the bottom of the map.
     * @return True if the popup is near the bottom of the map.
     */
    public boolean isPopUpNearMapBotton(final int yThreshold) {
        return popUp.getY() > yThreshold;
    }

}
