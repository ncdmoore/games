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
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Represents a single base marker on the main game map.
 * A base marker occupies a game grid.
 */
@Slf4j
public class BaseMarker implements Marker, AirOperationsMarker {
    private static final int SHADOW_RADIUS = 3;

    @Getter private final BaseGrid baseGrid;

    private final MapView mapView;
    private final Game game;
    private final ViewProps props;
    private final VBox imageView;
    private final VBox roundel;
    private final VBox flag;
    private final VBox title;

    private final PatrolMarkers patrolMarkers;
    private final MissionMarkers missionMarkers;
    private final RangeMarker rangeMarker;

    @Getter private MenuItem airfieldMenuItem;
    private final List<MenuItem> taskForceNavalOperationsMenuItems = new ArrayList<>();
    private final List<MenuItem> taskForceDetachMenuItems = new ArrayList<>();
    @Getter private MenuItem taskForceJoinMenuItem;

    private boolean selected = false;

    private final Text activeText = new Text();
    private final Text inactiveText = new Text();

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
                      final ResourceProvider imageResourceProvider,
                      final ViewProps props) {
        this.baseGrid = baseGrid;
        this.mapView = mapView;
        this.game = game;
        this.props = props;

        BaseGridType type = baseGrid.getType();

        final String imagePrefix = baseGrid.getSide().getValue().toLowerCase();
        final String humanPrefix = game.getHumanSide().getValue().toLowerCase();

        this.imageView = new VBox(imageResourceProvider.getImageView(props.getString(imagePrefix + "." + type.toLower() + ".base.icon")));

        this.roundel = new VBox(imageResourceProvider.getImageView(props.getString(humanPrefix + ".roundel.small.image")));
        this.flag = new VBox(imageResourceProvider.getImageView(props.getString(humanPrefix + ".flag.tiny.image")));

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

        activeText.setFill(Color.BLUE);
        activeText.setText(baseGrid.getTitle());
        inactiveText.setFill(Color.BLACK);
        inactiveText.setText(baseGrid.getTitle());
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
     * This base marker has been selected/unselected.
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
            Airfield airfield = baseGrid.getAirfield().orElseThrow();
            patrolMarkers.draw(airfield);
            missionMarkers.draw(airfield);
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
     * Set the marker as the current active marker. Only a single marker may be the active marker.
     */
    @Override
    public void setActive() {
        title.getChildren().clear();
        title.getChildren().add(activeText);
    }

    /**
     * Set the marker as inactive.
     */
    @Override
    public void setInactive() {
        title.getChildren().clear();
        title.getChildren().add(inactiveText);
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
     * Set the base task force naval operations menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setTaskForceNavalMenuOperations(final EventHandler<ActionEvent> handler) {
        taskForceNavalOperationsMenuItems.forEach(menuItem -> menuItem.setOnAction(handler));
    }

    /**
     * Set the base task force detach menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setTaskForceMenuDetach(final EventHandler<ActionEvent> handler) {
        taskForceDetachMenuItems.forEach(menuItem -> menuItem.setOnAction(handler));
    }

    /**
     * Set the base task force join menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setTaskForceJoinMenuItem(final EventHandler<ActionEvent> handler) {
        taskForceJoinMenuItem.setOnAction(handler);
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
     * Highlight a patrol radius for this marker.
     *
     * @param radius The radius to highlight.
     */
    public void highlightRadius(final int radius) {
        patrolMarkers.highlightRadius(radius);
    }

    /**
     * Remove this marker's highlighted patrol radius.
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
    private VBox buildTitle(final GridView gridView) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(getToolTipText());

        Tooltip.install(activeText, tooltip);
        Tooltip.install(inactiveText, tooltip);

        VBox vBox = new VBox(inactiveText);
        vBox.setLayoutY(gridView.getY() + gridView.getSize());
        vBox.setLayoutX(gridView.getX());
        vBox.setId("base-marker-title");
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
                .map(taskForce -> bullet + taskForce.getNameAndTitle())
                .collect(joining("\n"));

        String toolTipSquadrons = baseGrid
                .getAirfield()
                .map(Airfield::areSquadronsPresent)
                .orElse(false)
                ? "Squadrons Present\n" + squadronText
                : "No Squadrons";

        String toolTipTaskForces = baseGrid
                .getPort()
                .map(Port::areTaskForcesPresent)
                .orElse(false)
                ? "Task Forces Present\n" + taskForceText
                : "No Task Forces";

        return toolTipSquadrons + "\n\n" + toolTipTaskForces;
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

            contextMenu.getItems().add(airfieldMenuItem);

            List<TaskForce> taskForces = getBaseGrid()
                    .getPort()
                    .map(Port::getTaskForces)
                    .orElseGet(Collections::emptyList);

            List<Menu> taskForceMenus = buildTaskForceMenus(taskForces);
            contextMenu.getItems().addAll(taskForceMenus);

            taskForceJoinMenuItem = new MenuItem("Detach...");
            taskForceJoinMenuItem.setDisable(taskForces.size() < 2);
            taskForceJoinMenuItem.setUserData(taskForces);

            contextMenu.getItems().add(taskForceJoinMenuItem);

            imageView.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
            roundel.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
            flag.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));

            title.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
        }
    }

    private List<Menu> buildTaskForceMenus(final List<TaskForce> taskForces) {
        return taskForces
                .stream()
                .map(this::buildTaskForceMenuItems)
                .collect(Collectors.toList());

    }

    private Menu buildTaskForceMenuItems(final TaskForce taskForce) {
        Menu taskForceMenu = new Menu(taskForce.getName());

        MenuItem navalOperationsMenuItem = new MenuItem("Naval Operations...");
        navalOperationsMenuItem.setUserData(taskForce);
        taskForceNavalOperationsMenuItems.add(navalOperationsMenuItem);

        MenuItem detachMenuItem = new MenuItem("Detach...");
        detachMenuItem.setUserData(taskForce);
        taskForceDetachMenuItems.add(detachMenuItem);

        taskForceMenu
                .getItems()
                .addAll(navalOperationsMenuItem, detachMenuItem);

        return taskForceMenu;
    }
}
