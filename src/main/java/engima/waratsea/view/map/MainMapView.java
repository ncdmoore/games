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
import engima.waratsea.model.map.region.RegionGrid;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.main.BaseMarker;
import engima.waratsea.view.map.marker.main.BaseMarkerFactory;
import engima.waratsea.view.map.marker.main.RegionMarker;
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
    private final ImageResourceProvider imageResourceProvider;
    private final Game game;
    private final GameMap gameMap;
    private final ViewProps props;
    private final Provider<MainMenu> menuProvider;
    private final BaseMarkerFactory markerFactory;

    @Getter private StackPane map;
    @Getter private ImageView mapImageView;

    private final MapView mapView;

    private final Map<Side, List<BaseMarker>> baseMarkers = new HashMap<>();
    private final Map<Side, List<TaskForceMarker>> taskForceMarkers = new HashMap<>();
    private final Map<Side, List<RegionMarker>> regionMarkers = new HashMap<>();
    private final Map<Airbase, BaseMarker> airbases = new HashMap<>();

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
        regionMarkers.put(Side.ALLIES, new ArrayList<>());
        regionMarkers.put(Side.AXIS, new ArrayList<>());
        baseMarkers.put(Side.ALLIES, new ArrayList<>());
        baseMarkers.put(Side.AXIS, new ArrayList<>());
        baseMarkers.put(Side.NEUTRAL, new ArrayList<>());
        taskForceMarkers.put(Side.ALLIES, new ArrayList<>());
        taskForceMarkers.put(Side.AXIS, new ArrayList<>());

        mapImageView = imageResourceProvider.getImageView(props.getString("main.map.image"));
        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        Node mapGrid = mapView.draw(mapImageView, gridSize);

        drawBaseMarkers(Side.ALLIES);
        drawBaseMarkers(Side.AXIS);
        drawBaseMarkers(Side.NEUTRAL);

        drawRegionMarkers(game.getHumanSide());

        drawTaskForceMarkers(game.getHumanSide());

        mapView.registerMouseClick(this::mouseClicked);

        map = new StackPane(mapImageView, mapGrid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Set the region mouse enter handler.
     *
     * @param side The side: ALLIES or AXIS.
     * @param handler The mouse enter handler.
     */
    public void setRegionMouseEnterHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        regionMarkers.get(side).forEach(regionMarker -> regionMarker.setRegionMouseEnterHandler(handler));
    }

    /**
     * Set the region mouse exit handler.
     *
     * @param side The side: ALLIES or AXIS.
     * @param handler The mouse exit handler.
     */
    public void setRegionMouseExitHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        regionMarkers.get(side).forEach(regionMarker -> regionMarker.setRegionMouseExitHandler(handler));
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
     * Set the base grid mouse entered handler.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The base grid mouse entered handler.
     */
    public void setBaseMouseEnterHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setBaseMouseEnterHandler(handler));
    }

    /**
     * Set the base grid mouse exit handler.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The base grid mouse entered handler.
     */
    public void setBaseMouseExitHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setBaseMouseExitHandler(handler));
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
     * Set the base grid's mission arrow click handler.
     *
     * @param side The side ALLIES or AXIS.
     * @param handler The base mission arrow click handler.
     */
    public void setMissionArrowClickHandler(final Side side, final EventHandler<? super MouseEvent> handler) {
        baseMarkers.get(side).forEach(baseMarker -> baseMarker.setMissionArrowClickHandler(handler));
    }

    /**
     * Highlight the region marker.
     *
     * @param regionMarker The region marker highlighted.
     */
    public void highlightRegion(final RegionMarker regionMarker) {
        regionMarker.getBaseMarkers().forEach(this::outlineMarker);
    }

    /**
     * Un-highlight the region marker.
     *
     * @param regionMarker The region marker that is un-highlighted.
     */
    public void unHighlightRegion(final RegionMarker regionMarker) {
        regionMarker.getBaseMarkers().forEach(this::unOutlineMarker);
    }

    /**
     * Highlight the given base marker.
     *
     * @param baseMarker A base marker.
     */
    public void highlightBaseMarker(final BaseMarker baseMarker) {
        baseMarker.highlightMarker();
    }

    /**
     * Un-highlight the given base marker.
     *
     * @param baseMarker A base marker.
     */
    public void unHighlightBaseMarker(final BaseMarker baseMarker) {
        baseMarker.unHighlightMarker();
    }

    /**
     * Draw the given base's marker patrol radii.
     *
     * @param baseMarker A base marker.
     *
     * @return True if the marker is selected. False if the marker is not selected.
     */
    public boolean selectBaseMarker(final BaseMarker baseMarker) {
        return baseMarker.selectMarker();
    }

    /**
     * Draw the given airbase patrol radii.
     *
     * @param airbase An airbase.
     */
    public void toggleBaseMarkers(final Airbase airbase) {
        airbases.get(airbase).toggleMarkers();
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
     * Draw the range marker for the given airbase.
     *
     * @param airbase The airbase.
     * @param radius The range radius.
     */
    public void drawRangeMarker(final Airbase airbase, final int radius) {
        airbases.get(airbase).drawRangeMarker(radius);
    }

    /**
     * Hide the range marker for the given airbase.
     *
     * @param airbase The airbase.
     */
    public void hideRangeMarker(final Airbase airbase) {
        airbases.get(airbase).hideRangeMarker();
    }

    /**
     * Toggle the human side's region markers.
     */
    public void toggleRegionMarkers() {
        regionMarkers
                .get(game.getHumanSide())
                .forEach(this::toggleMarker);
    }

    /**
     * Toggle the given side's base markers.
     *
     * @param side The side ALLIES or AXIS.
     */
    public void toggleBaseMarkers(final Side side) {
        baseMarkers
                .get(side)
                .forEach(this::toggleMarker);
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
     * Draw an outline around the base marker.
     *
     * @param baseMarker The base marker that is outlined.
     */
    private void outlineMarker(final BaseMarker baseMarker) {
        baseMarker.outline();
    }

    /**
     * Remove the outline from around the base marker.
     *
     * @param baseMarker The base marker that has its outline removed.
     */
    private void unOutlineMarker(final BaseMarker baseMarker) {
        baseMarker.unOutline();
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
     * Draw all of the side's region markers.
     *
     * @param side The side: ALLIES or AXIS.
     */
    private void drawRegionMarkers(final Side side) {
        gameMap.getRegionGrids(side).forEach(this::drawRegionMarker);
    }

    /**
     * Draw an individual region marker.
     *
     * @param regionGrid The region's central grid where's the region's
     *                   name is displayed on the game map.
     */
    private void drawRegionMarker(final RegionGrid regionGrid) {
        RegionMarker regionMarker = markerFactory.createRegionMarker(regionGrid, mapView);

        regionMarkers.get(regionGrid.getSide()).add(regionMarker);

        regionGrid
                .getRegions()
                .stream().flatMap(region -> region.getAirfields().stream())
                .distinct()
                .map(airbases::get)
                .forEach(regionMarker::add);

        if (displayRegionMarker()) {
            regionMarker.draw();
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
     * Toggle the given region marker.
     *
     * @param regionMarker The region marker.
     */
    private void toggleMarker(final RegionMarker regionMarker) {
        if (displayRegionMarker()) {
            regionMarker.draw();
        } else {
            regionMarker.hide();
        }
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
     * Determine if the region marker should be displayed on the map.
     *
     * @return True if the region marker should be displayed. False otherwise.
     */
    private boolean displayRegionMarker() {
        return menuProvider.get().getShowRegions().isSelected();
    }

    /**
     * Determine if the base marker should be displayed on the map.
     *
     * @param type The type of base that the marker represents.
     * @return True if the base marker should be displayed. False otherwise.
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
