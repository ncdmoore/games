package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;


/**
 * Task force marker data transfer object. Used to transfer task force information to the view layer.
 */
public class AssetMarkerDTO implements PopUpDTO {

    @Getter private final Asset asset;
    @Getter @Setter private Nation nation;         //Note, some assets are shared by nations, such as airfields. Thus, we have
                                                   //to explicitly set the nation to indicate which nation this marker is for.
    @Getter private final String mapReference;
    @Getter private final String name;
    @Getter private final String title;
    @Getter private final boolean active;
    @Getter @Setter private EventHandler<? super MouseEvent> markerEventHandler;
    @Getter @Setter private EventHandler<? super MouseEvent> popupEventHandler;
    private GameGrid grid;
    @Getter private MapView mapView;
    @Getter private GridView gridView;
    @Getter @Setter private int xOffset;
    @Getter @Setter private String style;
    @Getter @Setter private ImageView imageView;

    /**
     * Construct the task force marker DTO.
     *
     * @param asset The game asset model.
     */
    public AssetMarkerDTO(final Asset asset) {
        this.asset = asset;
        this.mapReference = asset.getReference();
        this.name = asset.getName();
        this.title = asset.getTitle();
        this.active = asset.isActive();
    }

    /**
     * Set the game map which allows the grid to be determined from the asset's map reference.
     *
     * @param gameMap The game map.
     */
    public void setGameMap(final GameMap gameMap) {
        grid = gameMap.getGrid(mapReference);
    }

    /**
     * Set the map view which allows the grid view to be determined from the asset's grid reference.
     *
     * @param map The map view.
     */
    public void setMapView(final MapView map) {
        mapView = map;
        gridView = mapView.getGridView(grid);
    }
}
