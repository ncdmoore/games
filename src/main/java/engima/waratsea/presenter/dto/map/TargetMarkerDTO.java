package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.target.TargetFriendlyTaskForce;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.marker.preview.PopUp;
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

    private final Target target;
    private final TaskForce taskForce;

    @Getter
    @Setter
    private boolean active;

    @Getter
    @Setter
    private EventHandler<? super MouseEvent> markerEventHandler;

    @Getter
    @Setter
    private EventHandler<? super MouseEvent> popupEventHandler;

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

    @Getter
    @Setter
    private PopUp popup; // Indicates if this target marker will contain a popup.

    /**
     * Construct the target marker DTO.
     *
     * @param taskForce The task force that has this target.
     * @param target    The targets.
     */
    public TargetMarkerDTO(final TaskForce taskForce, final Target target) {
        this.taskForce = taskForce;
        this.target = target;
        this.mapReference = target.getLocation();
    }

    /**
     * Get the target's name.
     *
     * @return The target's name.
     */
    public String getName() {
        return taskForce.getMission().getType().toString();
    }

    /**
     * Get the task force's name.
     *
     * @return The task force's name.
     */
    public String getTaskForceName() {
        return taskForce.getName();
    }

    /**
     * Get the target's title.
     *
     * @return The target's title.
     */
    public String getTitle() {
        return taskForce.getMission().getType().toString();
    }

    /**
     * Set the game map which allows the grid to be determined.
     *
     * @param gameMap The game map.
     */
    public void setGameMap(final GameMap gameMap) {
        grid = gameMap.getGrid(mapReference);
    }

    /**
     * Set the map view which allows the grid view to be determined.
     *
     * @param mapView The view of the map.
     */
    public void setMapView(final MapView mapView) {
        gridView = mapView.getGridView(grid);
    }

    /**
     * Determine if this popup is shared amongst the target markers.
     *
     * @return True if the target is shared. False otherwise.
     */
    public boolean isPopupShared() {
        return popup != null;
    }

    /**
     * Indicates if the popup should be shown.
     *
     * Currently task forces on escort missions target is another task force.
     * We prevent the popup for friendly task forces as it obscures the
     * task force popup.
     *
     * @return True if the popup should be shown. False otherwise.
     */
    public boolean showPopup() {
        return target.getClass() != TargetFriendlyTaskForce.class;
    }
}
