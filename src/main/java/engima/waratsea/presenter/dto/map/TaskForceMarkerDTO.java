package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Task force marker data transfer object. Used to transfer task force information to the view layer.
 */
public class TaskForceMarkerDTO implements PopUpDTO {

    @Getter
    private final Asset asset;

    @Getter
    private final String mapReference;

    @Getter
    private final String name;

    @Getter
    private final boolean active;

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

    /**
     * Construct the task force marker DTO.
     *
     * @param asset The game asset model.
     */
    public TaskForceMarkerDTO(final Asset asset) {
        this.asset = asset;
        this.mapReference = asset.getLocation();
        this.name = asset.getName();
        this.active = asset.isActive();
    }



    /**
     * Set the game map which allows the grid to be determined from the task force's map reference.
     *
     * @param gameMap The game map.
     */
    public void setGameMap(final GameMap gameMap) {
        grid = gameMap.getGrid(mapReference);
    }

    /**
     * Set the map view which allows the grid view to be determined from the task force's grid location.
     *
     * @param mapView The map view.
     */
    public void setMapView(final MapView mapView) {
        gridView = mapView.getGridView(grid);
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
