package engima.waratsea.view.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.BaseGridType;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.main.BaseMarker;
import engima.waratsea.view.map.marker.main.BaseMarkerFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
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

    private ImageResourceProvider imageResourceProvider;
    private GameMap gameMap;
    private ViewProps props;
    private Provider<MainMenu> menuProvider;
    private BaseMarkerFactory markerFactory;


    @Getter
    private StackPane map;

    @Getter
    private ImageView mapImageView;

    private MapView mapView;

    private Map<Side, List<BaseMarker>> baseMarkers = new HashMap<>();


    private Map<Airbase, BaseMarker> airbases = new HashMap<>();

    /**
     * Constructor called by guice.
     * @param gameMap The game map.
     * @param props The view properties.
     * @param imageResourceProvider provides images.
     * @param menuProvider The main menu provider.
     * @param markerFactory The base marker provider.
     * @param mapView A utility to draw the map's grid.
     */
    @Inject
    public MainMapView(final GameMap gameMap,
                       final ViewProps props,
                       final ImageResourceProvider imageResourceProvider,
                       final Provider<MainMenu> menuProvider,
                       final BaseMarkerFactory markerFactory,
                       final MapView mapView) {
        this.gameMap = gameMap;
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.menuProvider = menuProvider;
        this.markerFactory = markerFactory;
        this.mapView = mapView;

        baseMarkers.put(Side.ALLIES, new ArrayList<>());
        baseMarkers.put(Side.AXIS, new ArrayList<>());
    }

    /**
     * Build the view.
     *
     * @return The node that contians the map.
     */
    public Node build() {

        mapImageView = imageResourceProvider.getImageView("mainMap.png");
        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        Node mapGrid = mapView.draw(mapImageView, gridSize);

        drawBaseMarkers(Side.ALLIES);
        drawBaseMarkers(Side.AXIS);

        mapView.registerMouseClick(this::mouseClicked);

        map = new StackPane(mapImageView, mapGrid);
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
     * Set the base grid's context airfield menu item.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The airfield menu item handler.
     */
    public void setAirfieldMenuHandler(final Side side, final EventHandler<ActionEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setAirfieldMenuHandler(handler));
    }

    /**
     * Set the base grid's patrol radius click handler.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The base patrol radius click handler.
     */
    public void setPatrolRadiusClickHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setPatrolRadiusClickHandler(handler));
    }

    /**
     * Draw the given base's marker patrol radii.
     *
     * @param baseMarker A base marker.
     */
    public void selectMarker(final BaseMarker baseMarker) {
        baseMarker.selectMarker();
    }

    /**
     * Draw the given airbase patrol radii.
     *
     * @param airbase An airbase.
     */
    public void drawPatrolRadii(final Airbase airbase) {
        airbases.get(airbase).drawPatrolRadii();
    }

    /**
     * Highlight a patrol radius.
     *
     * @param airbase The airbase that has one of its patrol radii highlighted.
     * @param radius The radius that is highlighted.
     */
    public void highlightPatrolRadius(final Airbase airbase, final int radius) {
        airbases.get(airbase).highlightRadius(radius);
    }

    /**
     * Remove a highlight from a patrol radius.
     *
     * @param airbase The airbase that has one of its patrol raddi highlight removed.
     */
    public void unhighlightPatrolRadius(final Airbase airbase) {
        airbases.get(airbase).unhighlightRadius();
    }

    /**
     * Get the given side's base markers.
     *
     * @param side The side ALLIES or AXIS.
     */
    public void toggleBaseMarkers(final Side side) {
        baseMarkers.get(side).forEach(this::toggleMarker);
    }

    /**
     * Build the given side's base markers.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void drawBaseMarkers(final Side side) {
        gameMap.getBaseGrids(side).forEach(this::drawBaseMarker);
    }

    /**
     * Build an individual base marker.
     *
     * @param baseGrid The base grid of the base marker.
     */
    private void drawBaseMarker(final BaseGrid baseGrid) {
        BaseGridType type = baseGrid.getType();

        int gridSize = props.getInt("taskforce.mainMap.gridSize");
        BaseMarker baseMarker = markerFactory.create(baseGrid, new GridView(gridSize, baseGrid.getGameGrid()), mapView);
        baseMarkers.get(baseGrid.getSide()).add(baseMarker);

        // Save the airfield in a map for easy access.
        baseMarker
                .getBaseGrid()
                .getAirfield()
                .ifPresent(airfield -> airbases.put(airfield, baseMarker));

        if (displayBaseMarker(type)) {
            baseMarker.draw();
        }
    }

    /**
     * Callback when main map grid is clicked.
     *
     * @param event The mouse click event.
     */
    private void mouseClicked(final MouseEvent event) {
        GridView gv = mapView.getGridView(event);
        log.info("row={},column={}", gv.getRow(), gv.getColumn());

        GameGrid gameGrid = gameMap.getGrid(gv.getRow(), gv.getColumn());

        log.info(gameGrid.getMapReference());
    }

    /**
     * Toggle the given base marker.
     *
     * @param baseMarker The base marker.
     */
    private void toggleMarker(final BaseMarker baseMarker) {
        if (displayBaseMarker(baseMarker.getBaseGrid().getType())) {
            baseMarker.draw();
        } else {
            baseMarker.hide();
        }
    }

    /**
     * Determine if the base marker should be displayed on the map.
     *
     * @param type The type of base that the marker represents.
     * @return True if the base marker should be displayed. Fasle otherwise.
     */
    private boolean displayBaseMarker(final BaseGridType type) {
        boolean showAirfields = menuProvider.get().getShowAirfields().isSelected();
        boolean showPorts = menuProvider.get().getShowPorts().isSelected();

        if (type == BaseGridType.AIRFIELD && showAirfields) {
            return true;
        }

        if (type == BaseGridType.SEAPLANE && showAirfields) {
            return true;
        }

        if (type == BaseGridType.PORT && showPorts) {
            return true;
        }

        return type == BaseGridType.BOTH && (showAirfields || showPorts);
    }
}
