package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Target marker data transfer object. Used to transfer task force targets information to the view layer.
 */
public class TargetMarkerDTO implements PopUpDTO {
    @Getter
    private final String mapReference;

    @Getter
    private final String taskForceName;

    @Getter
    private final String mission;

    @Getter
    @Setter
    private EventHandler<? super MouseEvent> markerEventHandler;

    @Getter
    @Setter
    private  EventHandler<? super MouseEvent> popupEventHandler;

    @Getter
    private GameGrid grid;

    @Getter
    private GridView gridView;

    @Getter
    @Setter
    private int xOffset;

    @Getter
    @Setter
    private String style;

    /**
     * Construct the target marker DTO.
     * @param taskForce The task force that has this target.
     * @param target The targets.
     */
    public TargetMarkerDTO(final TaskForce taskForce, final Target target) {
        this.mapReference = target.getLocation().getReference();
        this.taskForceName = taskForce.getName();
        this.mission = taskForce.getMission().toString();
    }

    /**
     * Set the game map which allows the grid to be determined.
     * @param gameMap The game map.
     */
    public void setGameMap(final GameMap gameMap) {
        grid = gameMap.getGrid(mapReference);
    }

    /**
     * Set the map view which allows the grid view to be determined.
     * @param mapView The view of the map.
     */
    public void setMapView(final MapView mapView) {
        gridView = mapView.getGridView(grid);
    }

    /**
     * Get the popup text for the target marker.
     * @return The popup text.
     */
    public String getText() {
        return mission;
    }
}
