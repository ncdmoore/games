package engima.waratsea.view.map;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

/**
 * The class represents a marker on a map.
 */
public class Marker {

    @Getter
    private String name;

    @Getter
    private String mapRef;

    @Getter
    private int x;

    @Getter
    private int y;

    @Getter
    private int size;

    @Getter
    private boolean active;

    @Getter
    private Rectangle rectangle;

    private final double opacity = 0.3;

    private EventHandler<? super MouseEvent> eventHandler;

    /**
     * Construct a marker.
     * @param name The name of the marker.
     * @param mapRef The location of the marker on the map. Map reference.
     * @param x x-coordinate of the marker.
     * @param y y-coordinate of the marker.
     * @param size size of the marker.
     * @param active indicates whether the marker is active or not.
     * @param eventHandler Method that handles mouse clicks on this marker.
     */
    public Marker(final String name,
                  final String mapRef,
                  final int x,
                  final int y,
                  final int size,
                  final boolean active,
                  final EventHandler<? super MouseEvent> eventHandler) {

        this.name = name;
        this.mapRef = mapRef;
        this.x = x;
        this.y = y;
        this.size = size;
        this.active = active;
        this.eventHandler = eventHandler;
    }

    /**
     * Draw the marker on the provided map.
     * Register a mouse click callback for the marker.
     *
     * @param map The map where the marker is drawn.
     */
    public void draw(final Group map) {
        rectangle = new Rectangle(x, y, size, size);
        rectangle.setStroke(Color.BLACK);
        rectangle.setOpacity(opacity);

        setOnMouseClicked(eventHandler);

        map.getChildren().add(rectangle);
    }

    /**
     * Select this marker. The marker is now the currently selected marker.
     */
    public void select() {
        rectangle.setFill(Color.BLACK);
        rectangle.setOpacity(1.0);
    }

    /**
     * Clear this marker. The marker is no longer selected if it was selected.
     **/
    public void clear() {
        rectangle.setFill(Color.BLACK);
        rectangle.setOpacity(opacity);
    }

    /**
     * Determine if this marker was clicked.
     *
     * @param clickedMarker The marker that was clicked.
     * @return True if this marker was the marker that was clicked. False otherwise.
     */
    public boolean wasClicked(final Object clickedMarker) {
        return this.rectangle == clickedMarker;
    }

    /**
     * Register this marker for mouse clicks.
     *
     * @param callback The method that is called when this marker is clicked.
     */
    private void setOnMouseClicked(final EventHandler<? super MouseEvent> callback) {
        rectangle.setOnMouseClicked(callback);
    }
}
