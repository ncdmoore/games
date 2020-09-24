package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.BaseGridType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Represents a single base marker on the main game map.
 * A base marker occupies a game grid.
 */
@Slf4j
public class BaseMarker {

    private static final int SHADOW_RADIUS = 3;

    @Getter private final BaseGrid baseGrid;

    private final MapView mapView;
    private final Game game;
    private final ViewProps props;
    private final VBox imageView;
    private final VBox roundel;
    private final VBox flag;
    private final Node title;

    private final PatrolMarkers patrolMarkers;
    private final MissionMarkers missionMarkers;
    private final RangeMarker rangeMarker;

    @Getter private MenuItem airfieldMenuItem;
    @Getter private MenuItem taskForceMenuOperations;
    @Getter private MenuItem taskForceMenuDetach;
    @Getter private MenuItem taskForceMenuJoin;

    private boolean selected = false;

    /**
     * The constructor.
     *
     * @param baseGrid The base's map grid.
     * @param mapView The map view.
     * @param game The game.
     * @param imageResourceProvider Provides images for this marker.
     * @param props The view properties.
     */
    @Inject
    public BaseMarker(@Assisted final BaseGrid baseGrid,
                      @Assisted final MapView mapView,
                      final Game game,
                      final ImageResourceProvider imageResourceProvider,
                      final ViewProps props) {
        this.baseGrid = baseGrid;
        this.mapView = mapView;
        this.game = game;
        this.props = props;

        String scenarioName = game.getScenario().getName();

        BaseGridType type = baseGrid.getType();

        final String imagePrefix = baseGrid.getSide().getValue().toLowerCase();
        final String humanPrefix = game.getHumanSide().getValue().toLowerCase();

        this.imageView = new VBox(imageResourceProvider.getImageView(scenarioName, props.getString(imagePrefix + "." + type.toLower() + ".base.icon")));
        this.roundel = new VBox(imageResourceProvider.getImageView(scenarioName, props.getString(humanPrefix + ".roundel.small.image")));
        this.flag = new VBox(imageResourceProvider.getImageView(scenarioName, props.getString(humanPrefix + ".flag.small.image")));

        int gridSize = props.getInt("taskforce.mainMap.gridSize");
        GridView gridView = new GridView(gridSize, baseGrid.getGameGrid());

        imageView.setLayoutX(gridView.getX());
        imageView.setLayoutY(gridView.getY());
        imageView.setViewOrder(ViewOrder.MARKER.getValue());
        imageView.setUserData(this);
        imageView.setId("map-base-grid-marker");

        InnerShadow innerShadow = new InnerShadow(SHADOW_RADIUS, Color.WHITE);
        imageView.setEffect(innerShadow);

        roundel.setLayoutX(gridView.getX() + props.getInt("main.map.base.marker.roudel.x.offset"));
        roundel.setLayoutY(gridView.getY() + 1);
        roundel.setViewOrder(ViewOrder.MARKER_DECORATION.getValue());
        roundel.setUserData(this);

        flag.setLayoutX(gridView.getX() + 1);
        flag.setLayoutY(gridView.getY() + props.getInt("main.map.base.marker.flag.y.offset"));
        flag.setViewOrder(ViewOrder.MARKER_DECORATION.getValue());
        flag.setUserData(this);

        title = buildTitle(gridView);

        patrolMarkers = new PatrolMarkers(mapView, baseGrid, gridView);
        missionMarkers = new MissionMarkers(mapView, baseGrid, gridView);
        rangeMarker = new RangeMarker(mapView, gridView);

        setUpContextMenus();
    }

    /**
     * Draw the base marker.
     */
    public void draw() {
        mapView.add(imageView);

        if (areSquadronsPresent() && game.getHumanPlayer().getSide() == baseGrid.getSide()) {
            mapView.add(roundel);
        }

        if (areTaskForcesPresent() && game.getHumanPlayer().getSide() == baseGrid.getSide()) {
            mapView.add(flag);
        }
    }

    /**
     * Highlight the base marker. Show the base's name.
     */
    public void highlightMarker() {
        if (!selected) {
            showTitle();
        }
    }

    /**
     * Un-highlight the base marker. Hide the base's name.
     */
    public void unHighlightMarker() {
        if (!selected) {
            hideTitle();
        }
    }

    /**
     * This base marker has been selected.
     *
     * @return True if the marker is selected. False if the marker is not selected.
     */
    public boolean selectMarker() {
        selected = !selected;

        if (selected) {
            imageView.setEffect(null);
            showTitle();
        } else {
            InnerShadow innerShadow = new InnerShadow(SHADOW_RADIUS, Color.WHITE);
            imageView.setEffect(innerShadow);
            hideTitle();
        }

        toggleMarkers();
        return selected;
    }

    /**
     * Draw this base marker's patrol range.
     */
    public void toggleMarkers() {
        if (selected) {
            patrolMarkers.draw();
            missionMarkers.draw();
        } else {
            patrolMarkers.hide();
            missionMarkers.hide();
            rangeMarker.hide();
        }
    }

    /**
     * Hide the base marker.
     */
    public void hide() {
        mapView.remove(imageView);
    }

    /**
     * Draw an outline around the base marker.
     */
    public void outline() {
        BorderWidths borderWidth = new BorderWidths(props.getDouble("range.marker.border.thickness"));
        String colorName = baseGrid.getSide().toLower() + ".base.outline.color";

        Color color = Color.web(props.getString(colorName));
        BorderStroke borderStroke = new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, borderWidth);

        imageView.setBorder(new Border(borderStroke));
    }

    /**
     * Remove the outline around the base marker.
     */
    public void unOutline() {
        imageView.setBorder(Border.EMPTY);
    }

    /**
     * Set the Base marker clicked handler.
     *
     * @param handler The mouse click event handler.
     */
    public void setBaseClickHandler(final EventHandler<? super MouseEvent> handler) {
        imageView.setOnMouseClicked(handler);
        roundel.setOnMouseClicked(handler);
        flag.setOnMouseClicked(handler);
    }

    /**
     * Set the Base marker mouse enter handler.
     *
     * @param handler The mouse entered event handler.
     */
    public void setBaseMouseEnterHandler(final EventHandler<? super MouseEvent> handler) {
        imageView.setOnMouseEntered(handler);
        roundel.setOnMouseEntered(handler);
        flag.setOnMouseEntered(handler);
    }

    /**
     * Set the Base marker mouse exit handler.
     *
     * @param handler The mouse exit event handler.
     */
    public void setBaseMouseExitHandler(final EventHandler<? super MouseEvent> handler) {
        imageView.setOnMouseExited(handler);
        roundel.setOnMouseExited(handler);
        flag.setOnMouseExited(handler);
    }

    /**
     * Set the base airfield menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setAirfieldMenuHandler(final EventHandler<ActionEvent> handler) {
        airfieldMenuItem.setOnAction(handler);
    }

    /**
     * Set the base task force operations menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setTaskForceMenuOperations(final EventHandler<ActionEvent> handler) {
        taskForceMenuOperations.setOnAction(handler);
    }

    /**
     * Set the base task force detach menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setTaskForceMenuDetach(final EventHandler<ActionEvent> handler) {
        taskForceMenuDetach.setOnAction(handler);
    }

    /**
     * Set the base task force join menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setTaskForceMenuJoin(final EventHandler<ActionEvent> handler) {
        taskForceMenuJoin.setOnAction(handler);
    }

    /**
     * Set the base patrol radius clicked handler.
     *
     * @param handler The mouse click handler.
     */
    public void setPatrolRadiusClickHandler(final EventHandler<? super MouseEvent> handler) {
        patrolMarkers.setRadiusMouseHandler(handler);
    }

    /**
     * Set the base mission arrow clicked handler.
     *
     * @param handler The mouse click handler.
     */
    public void setMissionArrowClickHandler(final EventHandler<? super MouseEvent> handler) {
        missionMarkers.setArrowMouseHandler(handler);
    }

    /**
     * Highlight a patrol radius for this base.
     *
     * @param radius The radius to highlight.
     */
    public void highlightRadius(final int radius) {
        patrolMarkers.highlightRadius(radius);
    }

    /**
     * Remove this base's highlighted patrol radius.
     */
    public void unhighlightRadius() {
        patrolMarkers.unhighlightRadius();
    }

    /**
     * Draw the base's range marker.
     *
     * @param radius The radius of the range marker.
     */
    public void drawRangeMarker(final int radius) {
        rangeMarker.draw(radius);
    }

    /**
     * Draw the base's range marker.
     */
    public void hideRangeMarker() {
        rangeMarker.hide();
    }

    /**
     * Determine if any squadrons are present at the this marker's airfield, if
     * this marker contains an airfield.
     *
     * @return True if this marker's airfield contains squadrons. False otherwise.
     */
    private boolean areSquadronsPresent() {
        return baseGrid
                .getAirfield()
                .map(Airfield::areSquadronsPresent)
                .orElse(false);
    }

    /**
     * Determine if any task forces are present at this marker's port, if
     * this marker contains a port.
     *
     * @return True if this marker's port contains task forces. False otherwise.
     */
    private boolean areTaskForcesPresent() {
        return baseGrid
                .getPort()
                .map(Port::areTaskForcesPresent)
                .orElse(false);
    }

    /**
     * Build the base's title.
     *
     * @param gridView The grid view of this base.
     * @return A node containing the base's title.
     */
    private Node buildTitle(final GridView gridView) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(getToolTipText());

        Label label = new Label(baseGrid.getTitle());
        label.setTooltip(tooltip);
        VBox vBox = new VBox(label);
        vBox.setLayoutY(gridView.getY() + gridView.getSize());
        vBox.setLayoutX(gridView.getX());
        vBox.setId("basemarker-title");
        return vBox;
    }

    /**
     * Show this base's title.
     */
    private void showTitle() {
        mapView.add(title);
    }

    /**
     * Hide this base's title.
     */
    private void hideTitle() {
        mapView.remove(title);
    }

    /**
     * Get the base marker's tool tip text.
     *
     * @return The base marker's tool tip text.
     */
    private String getToolTipText() {
        String bullet = "  \u2022  ";

        String squadronText = getBaseSquadrons()
                .entrySet()
                .stream()
                .map(entry -> bullet + entry.getKey().toString() + " : " + entry.getValue().size())
                .collect(joining("\n"));

        String taskForceText = getBaseTaskForces()
                .stream()
                .map(taskForce -> bullet + taskForce.getName() + " " + taskForce.getTitle())
                .collect(joining("\n"));

        String toolTipSquadrons = baseGrid
                .getAirfield()
                .map(Airfield::areSquadronsPresent)
                .orElse(false)
                ? "Squadrons Present\n" + squadronText
                : "No Squadrons";

        String taskForceSquadrons = baseGrid
                .getPort()
                .map(Port::areTaskForcesPresent)
                .orElse(false)
                ? "Task Forces Present\n" + taskForceText
                : "No Task Forces";

        return toolTipSquadrons + "\n\n" + taskForceSquadrons;
    }

    /**
     * Get the air base's squadrons per nation.
     *
     * @return A map of the air base's nation to list of squadrons for that nation.
     */
    private Map<Nation, List<Squadron>> getBaseSquadrons() {
        return baseGrid
                .getAirfield()
                .map(Airbase::getSquadronMap)
                .orElse(Collections.emptyMap());
    }

    /**
     * Get the port's task forces.
     *
     * @return A list of task forces.
     */
    private List<TaskForce> getBaseTaskForces() {
        return baseGrid
                .getPort()
                .map(Port::getTaskForces)
                .orElse(Collections.emptyList());
    }

    /**
     * Setup the right click context menus for the base marker.
     */
    private void setUpContextMenus() {
        if (baseGrid.getSide()  == game.getHumanSide()) {

            ContextMenu contextMenu = new ContextMenu();

            airfieldMenuItem = new MenuItem("Airfield...");
            airfieldMenuItem.setUserData(getBaseGrid().getAirfield());

            Menu taskForceMenu = new Menu("Task Force");
            taskForceMenuOperations = new MenuItem("Operations...");
            taskForceMenuDetach = new MenuItem("Detach...");
            taskForceMenuJoin = new MenuItem("Join...");

            taskForceMenu.getItems().addAll(taskForceMenuOperations, taskForceMenuDetach, taskForceMenuJoin);

            List<TaskForce> taskForces = getBaseGrid()
                    .getPort()
                    .map(Port::getTaskForces)
                    .orElseGet(Collections::emptyList);

            taskForceMenuOperations.setDisable(taskForces.isEmpty());
            taskForceMenuOperations.setUserData(taskForces);

            taskForceMenuDetach.setDisable(taskForces.isEmpty());
            taskForceMenuDetach.setUserData(taskForces);

            taskForceMenuJoin.setDisable(taskForces.size() < 2);
            taskForceMenuJoin.setUserData(taskForces);

            contextMenu.getItems().addAll(airfieldMenuItem, taskForceMenu);

            imageView.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
            roundel.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
            flag.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));

            title.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));

        }
    }
}
