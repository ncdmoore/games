package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.preview.TaskForceMarker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a map view. A map view is as it sounds a view of the game map. For example the flotilla
 * preview map is a map view of the game map.
 */
@Slf4j
public class FlotillaPreviewMapView {
    private ViewProps props;
    private ImageResourceProvider imageResourceProvider;

    private GameMap gameMap;
    private MapView mapView;

    private Map<String, TaskForceMarker> markerMap = new HashMap<>();                //marker name -> grid.

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param imageResourceProvider The image resource provider.
     * @param gameMap The game map.
     * @param mapView A utility to aid in drawing the map grid.
     */
    @Inject
    public FlotillaPreviewMapView(final ViewProps props,
                                  final ImageResourceProvider imageResourceProvider,
                                  final GameMap gameMap,
                                  final MapView mapView) {
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.gameMap = gameMap;
        this.mapView = mapView;
    }

    /**
     * Draws the map grid.
     *
     * @return The node containing the map grid.
     */
    public Node draw() {
        ImageView imageView = imageResourceProvider.getImageView("previewMap.png");
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        Node grid = mapView.draw(imageView, gridSize);

        StackPane map = new StackPane(imageView, grid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Mark a grid as having a flotilla.
     *
     * @param dto The taskforce/flotilla data transfer object.
     */
    public void markFlotilla(final AssetMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);

        TaskForceMarker marker = new TaskForceMarker(dto);
        marker.draw(dto);

        markerMap.put(dto.getName(), marker);           //Index this flotilla's name to the new marker.
    }

    /**
     * Select a marker on the map.
     *
     * @param name specifies the marker to select.
     */
    public void selectMarker(final String name) {
        markerMap.get(name).select(mapView, name);      //Show the flotilla popup marker.
    }

    /**
     * Clear a marker selection on the map.
     *
     * @param name specifies the marker to clear.
     */
    public void clearMarker(final String name) {
        markerMap.get(name).clear(mapView);             //Hide the flotilla popup marker.
    }

    /**
     * Remove a marker from the map.
     *
     * @param name specifies the marker to remove.
     */
    public void removeMarker(final String name) {
        markerMap.get(name).remove(mapView);
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
        VBox o = (VBox) event.getSource();
        mapView.remove(o);
    }

    /**
     * Get the task force preview map legend.
     *
     * @return A grid pane that contains the task force preview map legend.
     * */
    public Node getLegend() {
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        GridPane gridPane = new GridPane();
        Node taskForceKey = TaskForceMarker.getLegend(0, 0, gridSize);
        gridPane.add(taskForceKey, 0, 0);
        gridPane.add(new Label("Flotilla"), 1, 0);

        gridPane.setId("map-legend-grid");

        return gridPane;
    }
}
