package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents a map view. A map view is as it sounds a view of the game map. For example the task force
 * preview map is a map view of the game map.
 */
@Slf4j
public class TaskForcePreviewMapView {

    private ViewProps props;
    private ImageResourceProvider imageResourceProvider;

    private GameMap gameMap;
    private MapView mapView;

    private Map<String, TaskForceMarker> markerMap = new HashMap<>();                //marker name -> grid.
    private Map<String, TaskForceMarker> mapRefMarkerMap = new HashMap<>();          //map reference -> grid.

    private Map<String, List<TargetMarker>> targetMap = new HashMap<>();
    private Map<String, TargetMarker> mapRefTargetMap = new HashMap<>();

    /**
     * Constructor called by guice.
     * @param props View properties.
     * @param imageResourceProvider The image resource provider.
     * @param gameMap The game map.
     * @param mapView A utility to aid in drawing the map grid.
     */
    @Inject
    public TaskForcePreviewMapView(final ViewProps props,
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
     * @return The node containing the map grid.
     */
    public Node draw() {
        ImageView imageView = imageResourceProvider.getImageView("previewMap.png");
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        Node grid = mapView.draw(gameMap, gridSize);

        StackPane map = new StackPane(imageView, grid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Place the task force marker on the map.
     * @param dto The task force marker data transfer object.
     */
    public void markTaskForce(final TaskForceMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);

        if (mapRefMarkerMap.containsKey(dto.getMapReference())) {                                                       //Check if this grid already has a marker.
            TaskForceMarker existingMarker = mapRefMarkerMap.get(dto.getMapReference());
            existingMarker.addText(dto.getText(), dto.isActive());                                                      //Add this task force's name to the existing marker.
            markerMap.put(dto.getName(), existingMarker);                                                               //Index this task force's name to the existing marker.
        } else {
            TaskForceMarker marker = new TaskForceMarker(dto);                                                          //Create a new marker.
            marker.draw(mapView, dto.isActive());                                                                       //Store this task force's name in the new marker.
            mapRefMarkerMap.put(dto.getMapReference(), marker);
            markerMap.put(dto.getName(), marker);                                                                       //Index this task force's name to the new marker.
        }
    }

    /**
     * Place the target marker on the map.
     * @param dto The target marker data transfer object.
     */
    public void markTarget(final TargetMarkerDTO dto) {
        dto.setGameMap(gameMap);
        dto.setMapView(mapView);

        if (mapRefTargetMap.containsKey(dto.getMapReference())) {
            TargetMarker existingMarker = mapRefTargetMap.get(dto.getMapReference());
            addTargetMarker(dto, existingMarker);
        } else {
            TargetMarker marker = new TargetMarker(dto);

            //If the target marker occupies the same space as a task force marker
            //then make the target marker inactive.
            boolean active = !mapRefMarkerMap.containsKey(dto.getMapReference());

            marker.draw(active);
            mapRefTargetMap.put(dto.getMapReference(), marker);
            addTargetMarker(dto, marker);
        }
    }

    /**
     * This method is called to adjust the y coordinate of the popup's that are near the bottom of the map.
     */
    public void finish() {
        int yPopUpAdjust = props.getInt("taskforce.previewMap.popup.yScale");
        int yBottomThreshold = props.getInt("taskforce.previewMap.y.size");

        markerMap.values().stream()
                .filter(marker -> marker.isPopUpNearMapBotton(yBottomThreshold))
                .forEach(marker -> marker.adjustY(yPopUpAdjust));

        mapRefTargetMap.values().stream()
                .filter(marker -> marker.isPopUpNearMapBotton(yBottomThreshold))
                .forEach(marker -> marker.adjustY(yPopUpAdjust));
    }

    /**
     * Select a marker on the map.
     * @param name specifies the marker to select.
     */
    public void selectMarker(final String name) {
        markerMap.get(name).select(mapView, name);                                                                      //Show the task force marker.

        Optional.ofNullable(targetMap.get(name))
                .ifPresent(targetMarkers -> targetMarkers.forEach(targetMarker -> targetMarker.select(mapView)));       //Show this task force's target markers if any exist.
    }

    /**
     * Clear a marker selection on the map.
     * @param name specifies the marker to clear.
     */
    public void clearMarker(final String name) {
        markerMap.get(name).clear(mapView);                                                                             //Hide the task force marker.

        Optional.ofNullable(targetMap.get(name))
                .ifPresent(targetMarkers -> targetMarkers.forEach(targetMarker -> targetMarker.clear(mapView)));        //Hide any target marker's if any exist.
    }

    /**
     * Get the name of the marker from the markers grid.
     * @param clickedMarker represents the marker.
     * @return A list of names associated with this marker.
     */
    public List<String> getNameFromMarker(final Object clickedMarker) {
        return markerMap.entrySet().stream()
                .filter(entry -> entry.getValue().wasClicked(clickedMarker))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Select target marker. Show the corresponding popup. Note, only a single target marker can be clicked.
     * @param clickedMarker represents the marker.
     */
    public void selectTargetMarker(final Object clickedMarker) {
        mapRefTargetMap.entrySet().stream()
                .filter(entry -> entry.getValue().wasClicked(clickedMarker))
                .findFirst()
                .map(Map.Entry::getValue)
                .ifPresent(targetMarker -> targetMarker.select(mapView));
    }

    /**
     * Add a target marker to the preview map.
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
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
        VBox o = (VBox) event.getSource();
        mapView.remove(o);
    }

}
