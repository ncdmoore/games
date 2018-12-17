package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.Grid;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.view.map.GridView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Task force marker data transfer object. Used to transfer task force information to the view layer.
 */
public class TaskForceMarkerDTO implements PopUpDTO {

    @Getter
    private final String mapReference;

    @Getter
    private final String name;

    @Getter
    private final boolean active;

    @Getter
    @Setter
    private  EventHandler<? super MouseEvent> markerEventHandler;

    @Getter
    @Setter
    private  EventHandler<? super MouseEvent> popupEventHandler;

    @Getter
    private Grid grid;

    @Getter
    private GridView gridView;

    @Getter
    @Setter
    private int xOffset;

    @Getter
    @Setter
    private String style;

    /**
     * Construct the task force marker DTO.
     *
     * @param taskForce The task force model.
     */
    public TaskForceMarkerDTO(final TaskForce taskForce) {
        mapReference = taskForce.getLocation();
        name = taskForce.getName();
        active = taskForce.isActive();
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
     * Set the size of the grid that the marker occupies.
     *
     * @param size map grid size.
     */
    public void setGridSize(final int size) {
        gridView = new GridView(size, grid.getRow(), grid.getColumn());
    }

    /**
     * Get the related popup text.
     *
     * @return The task force name is placed in the popup.
     */
    public String getText() {
        return name;
    }
}
