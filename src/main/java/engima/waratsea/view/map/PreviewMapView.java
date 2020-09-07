package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.preview.AirfieldMarker;
import engima.waratsea.view.map.marker.preview.SquadronRangeMarker;
import engima.waratsea.view.map.marker.preview.TargetMarker;
import engima.waratsea.view.map.marker.preview.TaskForceMarker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents a map view. A map view is as it sounds a view of the game map. For example the task force
 * preview map is a map view of the game map.
 */
@Slf4j
public class PreviewMapView {

    private ViewProps props;
    private ImageResourceProvider imageResourceProvider;

    private GameMap gameMap;
    private MapView mapView;

    private Map<String, TaskForceMarker> markerMap = new HashMap<>();                //marker name (task force name)-> grid.
    private Map<String, TaskForceMarker> mapRefMarkerMap = new HashMap<>();          //map reference -> grid.

    private Map<String, List<TargetMarker>> targetMap = new HashMap<>();             //marker name (task force name) -> grid.
    private List<TargetMarker> targetMarkers = new ArrayList<>();

    private Map<String, AirfieldMarker> airfieldMarkerMap = new HashMap<>();

    private SquadronRangeMarker rangeMarker;

    private ImageView imageView;
    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param imageResourceProvider The image resource provider.
     * @param gameMap The game map.
     * @param mapView A utility to aid in drawing the map grid.
     */
    @Inject
    public PreviewMapView(final ViewProps props,
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
        imageView = imageResourceProvider.getImageView(props.getString("preview.map.image"));
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        Node grid = mapView.draw(imageView, gridSize);

        StackPane map = new StackPane(imageView, grid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Place the task force marker on the map.
     *
     * @param dto The task force marker data transfer object.
     */
    public void markTaskForce(final AssetMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);

        if (mapRefMarkerMap.containsKey(dto.getMapReference())) {                                                       //Check if this grid already has a marker.
            TaskForceMarker existingMarker = mapRefMarkerMap.get(dto.getMapReference());
            existingMarker.addText(dto);                                                                                //Add this task force's name to the existing marker.
            markerMap.put(dto.getName(), existingMarker);                                                               //Index this task force's name to the existing marker.
        } else {
            TaskForceMarker marker = new TaskForceMarker(dto);                                                          //Create a new marker.
            marker.draw(dto);                                                                                           //Store this task force's name in the new marker.
            mapRefMarkerMap.put(dto.getMapReference(), marker);
            markerMap.put(dto.getName(), marker);                                                                       //Index this task force's name to the new marker.
        }
    }

    /**
     * Place an airfield marker on the map.
     *
     * @param dto The task force marker data transfer object.
     */
    public void markAirfield(final AssetMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);

        AirfieldMarker marker = new AirfieldMarker(dto);
        marker.draw(dto);

        airfieldMarkerMap.put(dto.getName(), marker);
    }

    /**
     * Place the target marker on the map.
     *
     * @param dto The target marker data transfer object.
     * @return The target marker.
     */
    public TargetMarker markTarget(final TargetMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);

        TargetMarker marker = new TargetMarker(dto);
        marker.draw(dto);
        targetMarkers.add(marker);
        addTargetMarker(dto, marker);

        return marker;
    }

    /**
     * Mark the selected squadron's range radius from the selected airfield.
     *
     * @param dto The data transfer object.
     */
    public void markRange(final AssetMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);
        dto.setImageView(imageView);

        clearRange();   // Clear the old range marker.

        rangeMarker = new SquadronRangeMarker(dto);
        rangeMarker.display();
    }

    /**
     * Clear the range radius.
     */
    public void clearRange() {
        if (rangeMarker != null) {
            rangeMarker.hide();
        }
    }

    /**
     * This method is called to adjust the y coordinate of the popup's that are near the bottom of the map.
     */
    public void finish() {
        int yBottomThreshold = props.getInt("taskforce.previewMap.y.size");
        int yPopUpOffset1 = props.getInt("taskforce.previewMap.popup.yOffset.1");

        mapRefMarkerMap.values().forEach(marker -> {
            int size = marker.size();
            int yPopUpOffset = props.getInt("taskforce.previewMap.popup.yOffset." + size);
            marker.adjustY(yPopUpOffset, yBottomThreshold);
        });

        airfieldMarkerMap.values().forEach(marker -> marker.adjustY(yPopUpOffset1, yBottomThreshold));
        targetMarkers.forEach(marker -> marker.adjustY(yPopUpOffset1, yBottomThreshold));
    }

    /**
     * Select a marker on the map.
     *
     * @param name specifies the marker to select.
     */
    public void selectMarker(final String name) {
        markerMap.get(name).select(mapView, name);                                                                      //Show the task force marker.

        Optional.ofNullable(targetMap.get(name))
                .ifPresent(markers -> markers.forEach(targetMarker -> targetMarker.select(mapView)));                   //Show this task force's target markers if any exist.
    }

    /**
     * Clear a marker selection on the map.
     *
     * @param name specifies the marker to clear.
     */
    public void clearMarker(final String name) {
        markerMap.get(name).clear(mapView);                                                                             //Hide the task force marker.

        Optional.ofNullable(targetMap.get(name))
                .ifPresent(markers -> markers.forEach(targetMarker -> targetMarker.clear(mapView)));                    //Hide any target marker's if any exist.
    }

    /**
     * Select target marker. Show the corresponding popup. Note, only a single target marker can be clicked.
     *
     * @param clickedMarker represents the marker.
     */
    public void selectTargetMarker(final Object clickedMarker) {
        Circle circle = (Circle) clickedMarker;
        TargetMarker marker = (TargetMarker) circle.getUserData();
        marker.select(mapView);
    }

    /**
     * Add a target marker to the preview map.
     *
     * @param dto Target marker data transfer object.
     * @param marker The marker to add to the preview map.
     */
    private void addTargetMarker(final TargetMarkerDTO dto, final TargetMarker marker) {
        if (!targetMap.containsKey(dto.getTaskForceName())) {
            targetMap.put(dto.getTaskForceName(), new ArrayList<>());
        }

        targetMap.get(dto.getTaskForceName()).add(marker);
    }

    /**
     * Select a marker on the map.
     *
     * @param name specifies the marker to select.
     */
    public void selectAirfieldMarker(final String name) {
        airfieldMarkerMap.get(name).select(mapView, name);
    }

    /**
     * Clear a marker selection on the map.
     *
     * @param name specifies the marker to clear.
     */
    public void clearAirfieldMarker(final String name) {
        airfieldMarkerMap.get(name).clear(mapView);
    }

    /**
     * Remove an airfield marker from the map.
     *
     * @param name specifies the marker to remove.
     */
    public void removeAirfieldMarker(final String name) {
        airfieldMarkerMap.get(name).remove();
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
        Node node = (Node) event.getSource();
        mapView.remove(node);
    }

    /**
     * Get the task force preview map legend.
     *
     * @return A grid pane that contains the task force preview map legend.
     */
    public Node getLegend() {
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        GridPane gridPane = new GridPane();
        Node taskForceKey = TaskForceMarker.getLegend(0, 0, gridSize);
        gridPane.add(taskForceKey, 0, 0);
        gridPane.add(new Label("Task Force"), 1, 0);

        Node targetKey = TargetMarker.getLegend(0, 0, gridSize / 2.0);
        gridPane.add(targetKey, 0, 1);
        gridPane.add(new Label("Task Force Target"), 1, 1);

        gridPane.setId("map-legend-grid");

        return gridPane;
    }

    /**
     * Get the airfield preview map legend.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     * @return A grid pane that contains the airfield preview map legend.
     */
    public Node getLegendAirfield(final Nation nation) {
        int gridSize = props.getInt("taskforce.previewMap.gridSize");
        final int col3 = 3;

        GridPane gridPane = new GridPane();

        Node bothKey = AirfieldMarker.getLegendBoth(nation, 0, 0, gridSize);
        Label bothLabel = new Label("Airfield/Port");
        gridPane.add(bothKey, 0, 0);
        gridPane.add(bothLabel, 1, 0);
        bothLabel.setTooltip(new Tooltip("Can station both land-based and seaplane squadrons"));

        Node airfieldKey = AirfieldMarker.getLegendAirfield(nation, 0, 0, gridSize);
        Label airfieldLabel = new Label("Airfield");
        gridPane.add(airfieldKey, 2, 0);
        gridPane.add(airfieldLabel, col3, 0);
        airfieldLabel.setTooltip(new Tooltip("Can station only land-based squadrons"));

        Node seaplaneKey = AirfieldMarker.getLegendSeaplane(nation, 0, 0, gridSize);
        Label seaplaneLabel = new Label("Port");
        gridPane.add(seaplaneKey, 2, 1);
        gridPane.add(seaplaneLabel, col3, 1);
        seaplaneLabel.setTooltip(new Tooltip("Can station only seaplane squadrons"));

        Node rangeKey = SquadronRangeMarker.getLegend(0, 0, (double) gridSize / 2);
        Label rangeLabel = new Label("Combat Radius");
        rangeLabel.setTooltip(new Tooltip("Outer circle: squadron equipped with drop tanks"));

        gridPane.add(rangeKey, 0, 1);
        gridPane.add(rangeLabel, 1, 1);

        gridPane.setId("map-legend-grid");

        return gridPane;
    }
}