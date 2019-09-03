package engima.waratsea.view.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.main.BaseMarker;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The view of the main map.
 */
@Slf4j
@Singleton
public class MainMapView {

    private GameMap gameMap;
    private ViewProps props;
    private ImageResourceProvider imageResourceProvider;


    private MapView mapView;

    private Map<Side, List<BaseMarker>> baseMarkers = new HashMap<>();

    /**
     * Constructor called by guice.
     * @param gameMap The game map.
     * @param props The view properties.
     * @param imageResourceProvider provides images.
     * @param mapView A utility to draw the map's grid.
     */
    @Inject
    public MainMapView(final GameMap gameMap,
                       final ViewProps props,
                       final ImageResourceProvider imageResourceProvider,
                       final MapView mapView) {
        this.gameMap = gameMap;
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.mapView = mapView;

        baseMarkers.put(Side.ALLIES, new ArrayList<>());
        baseMarkers.put(Side.AXIS, new ArrayList<>());
    }

    /**
     * Build the main game map view.
     * @return  The main game map.
     */
    public Node build() {
        ImageView mapImageView = imageResourceProvider.getImageView("mainMap.png");
        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        Node mapGrid = mapView.draw(mapImageView, gridSize);

        buildBaseMarkers(Side.ALLIES);
        buildBaseMarkers(Side.AXIS);

        mapView.registerMouseClick(this::mouseClicked);

        StackPane map = new StackPane(mapImageView, mapGrid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Set the base grid click handler.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The base grid mouse click handler.
     */
    public void setBaseClickHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setBaseClickHandler(handler));
    }

    /**
     * Build the given side's base markers.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildBaseMarkers(final Side side) {
        gameMap.getBaseGrids(side).forEach(this::buildBaseMarker);
    }

    /**
     * Build an individual base marker.
     *
     * @param baseGrid The base grid of the base marker.
     */
    private void buildBaseMarker(final BaseGrid baseGrid) {
        int gridSize = props.getInt("taskforce.mainMap.gridSize");
        final String imagePrefix = baseGrid.getSide().getValue().toLowerCase();
        ImageView baseImageView = imageResourceProvider.getImageView(imagePrefix + baseGrid.getType().getValue() + ".png");
        BaseMarker baseMarker = new BaseMarker(baseGrid, new GridView(gridSize, baseGrid.getGameGrid()), baseImageView);
        baseMarker.draw(mapView);
        baseMarkers.get(baseGrid.getSide()).add(baseMarker);
    }

    /**
     * Callback when main map gri
     * d is clicked.
     *
     * @param event The mouse click event.
     */
    private void mouseClicked(final MouseEvent event) {
        GridView gv = mapView.getGridView(event);
        log.info("row={},column={}", gv.getRow(), gv.getColumn());

        GameGrid gameGrid = gameMap.getGrid(gv.getRow(), gv.getColumn());

        log.info(gameGrid.getMapReference());
    }


}
