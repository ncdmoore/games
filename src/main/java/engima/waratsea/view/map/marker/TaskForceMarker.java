package engima.waratsea.view.map.marker;

import engima.waratsea.model.asset.Asset;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * The class represents a marker on a map.
 */
@Slf4j
public class TaskForceMarker {

    private static final double OPACITY = 0.5;

    private final GridView gridView;
    private final EventHandler<? super MouseEvent> eventHandler;
    private final PopUp popUp;

    private Rectangle rectangle;

    private List<Asset> assets;

    /**
     * Construct a marker.
     *
     * @param dto All the data needed to create a marker.
     */
    public TaskForceMarker(final TaskForceMarkerDTO dto) {
        this.assets = new ArrayList<>();
        this.assets.add(dto.getAsset());
        this.gridView = dto.getGridView();
        this.eventHandler = dto.getMarkerEventHandler();
        dto.setStyle("popup-taskforce");
        this.popUp = new PopUp(dto);
    }

    /**
     * Draw the marker on the provided map. Register a mouse click callback for the marker.
     *
     * @param dto All the data needed to create a marker.
     */
    public void draw(final TaskForceMarkerDTO dto) {
        rectangle = new Rectangle(gridView.getX(), gridView.getY(), gridView.getSize(), gridView.getSize());
        rectangle.setOpacity(OPACITY);
        rectangle.getStyleClass().add("taskforce-marker");

        rectangle.setUserData(assets);

        setOnMouseClicked(eventHandler);

        dto.getMapView().add(rectangle);

        popUp.draw(dto.isActive());
    }

    /**
     * Add text to the marker's corresponding popup.
     *
     * @param dto All the data needed to create a marker.
     */
    public void addText(final TaskForceMarkerDTO dto) {
        Asset asset = dto.getAsset();

        assets.add(asset);
        popUp.addText(dto.getName(), dto.isActive());
    }

    /**
     * Select this marker. The marker is now the currently selected marker.
     *
     * @param map The game map.
     * @param name The name of the task force.
     */
    public void select(final MapView map, final String name) {
        rectangle.setOpacity(1.0);
        popUp.display(map, name);
    }

    /**
     * Clear this marker. The marker is no longer selected if it was selected.
     *
     * @param map The game map.
     **/
    public void clear(final MapView map) {
        rectangle.setOpacity(OPACITY);
        popUp.hide(map);
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
     *
     * @param offset How much the y is adjusted once.
     * @param yThreshold Determines if the popup is near the bottom and needs to be moved up.
     **/
    public void adjustY(final int offset, final int yThreshold) {
        popUp.adjustY(offset, yThreshold);
    }

    /**
     * Get the size of the marker.
     *
     * @return The size of the marker.
     */
    public int size() {
        return popUp.size();
    }

    /**
     * Get the map legend key. This is just a duplicate rectangle that is the same
     * size and type as used for this marker. It is used in the map legend.
     *
     * @param x The x coordinate of the marker.
     * @param y The y coordingate of the marker.
     * @param size The size height and width of the marker.
     * @return A marker legend key.
     */
    public static Node getLegend(final double x, final double y, final double size) {
        Rectangle r = new Rectangle(x, y, size, size);
        r.setOpacity(OPACITY);
        r.getStyleClass().add("taskforce-marker");
        return r;
    }
}
