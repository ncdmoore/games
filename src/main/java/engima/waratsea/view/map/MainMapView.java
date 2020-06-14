package engima.waratsea.view.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.BaseGridType;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.TaskForceGrid;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.main.BaseMarker;
import engima.waratsea.view.map.marker.main.BaseMarkerFactory;
import engima.waratsea.view.map.marker.main.TaskForceMarker;
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
    private Game game;
    private GameMap gameMap;
    private ViewProps props;
    private Provider<MainMenu> menuProvider;
    private BaseMarkerFactory markerFactory;

    @Getter private StackPane map;
    @Getter private ImageView mapImageView;

    private MapView mapView;

    private Map<Side, List<BaseMarker>> baseMarkers = new HashMap<>();
    private Map<Side, List<TaskForceMarker>> taskForceMarkers = new HashMap<>();
    private Map<Airbase, BaseMarker> airbases = new HashMap<>();

    /**
     * Constructor called by guice.
     * @param game The game.
     * @param gameMap The game map.
     * @param props The view properties.
     * @param imageResourceProvider provides images.
     * @param menuProvider The main menu provider.
     * @param markerFactory The base marker provider.
     * @param mapView A utility to draw the map's grid.
     */
    @Inject
    public MainMapView(final Game game,
            final GameMap gameMap,
                       final ViewProps props,
                       final ImageResourceProvider imageResourceProvider,
                       final Provider<MainMenu> menuProvider,
                       final BaseMarkerFactory markerFactory,
                       final MapView mapView) {
        this.game = game;
        this.gameMap = gameMap;
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.menuProvider = menuProvider;
        this.markerFactory = markerFactory;
        this.mapView = mapView;
    }

    /**
     * Build the view.
     *
     * @return The node that contians the map.
     */
    public Node build() {
        airbases.clear();
        baseMarkers.put(Side.ALLIES, new ArrayList<>());
        baseMarkers.put(Side.AXIS, new ArrayList<>());
        taskForceMarkers.put(Side.ALLIES, new ArrayList<>());
        taskForceMarkers.put(Side.AXIS, new ArrayList<>());

        mapImageView = imageResourceProvider.getImageView(props.getString("main.map.image"));
        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        Node mapGrid = mapView.draw(mapImageView, gridSize);

        drawBaseMarkers(Side.ALLIES);
        drawBaseMarkers(Side.AXIS);

        drawTaskForceMarkers(game.getHumanSide());

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
     * Set the base grid's context task force operations menu item.
     *
     * @param side The side ALLIES of AXIS.
     * @param handler The task force menu item handler.
     */
    public void setTaskForceOperationsMenuHandler(final Side side, final EventHandler<ActionEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setTaskForceMenuOperations(handler));
        taskForceMarkers.get(side).forEach(taskForceMarker -> taskForceMarker.setOperationsMenuHandler(handler));
    }

    /**
     * Set the context menu detach menu item handler for the base and task force markers.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The task force menu item handler.
     */
    public void setTaskForceDetachMenuHandler(final Side side, final EventHandler<ActionEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setTaskForceMenuDetach(handler));
        taskForceMarkers.get(side).forEach(taskForceMarker -> taskForceMarker.setDetachMenuHandler(handler));
    }

    /**
     * Set the context menu join menu item handler for the base and task force markers.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The task force menu item handler.
     */
    public void setTaskForceJoinMenuHandler(final Side side, final EventHandler<ActionEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setTaskForceMenuJoin(handler));
        taskForceMarkers.get(side).forEach(taskForceMarker -> taskForceMarker.setJoinMenuHandler(handler));
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
     *
     * @return True if the marker is selected. False if the marker is not selected.
     */
    public boolean selectMarker(final BaseMarker baseMarker) {
        return baseMarker.selectMarker();
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

        BaseMarker baseMarker = markerFactory.createBaseMarker(baseGrid, mapView);
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
     * Draw all of the task force markers for the given side.
     *
     * @param side The side: ALLIES or AXIS.
     */
    private void drawTaskForceMarkers(final Side side) {
        gameMap.getTaskForceGrids().get(side).forEach(this::drawTaskForceMarker);
    }

    /**
     * Draw an individual task force marker.
     *
     * @param taskForceGrid The task force grid.
     */
    private void drawTaskForceMarker(final TaskForceGrid taskForceGrid) {
        TaskForceMarker taskForceMarker = markerFactory.createTaskForceMarker(taskForceGrid, mapView);

        taskForceMarkers.get(taskForceGrid.getSide()).add(taskForceMarker);

        taskForceMarker.draw();
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
