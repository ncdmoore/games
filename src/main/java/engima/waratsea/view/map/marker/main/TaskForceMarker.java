package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.TaskForceGrid;
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
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a task force marker on the main game map.
 * Multiple task forces that occupy the same game grid are represented by a single task force marker.
 *
 * Clicking this marker toggles through this list of task forces.
 * Clicking once the last task force has been selected de-selects this marker.
 */
public class TaskForceMarker implements AirOperationsMarker {
    private static final int SHADOW_RADIUS = 3;
    private static final int TASK_FORCE_INITIAL_INDEX = -1;

    @Getter private final TaskForceGrid taskForceGrid;

    private final Game game;
    private final MapView mapView;
    private final VBox image;
    private final Node title;
    private final List<TaskForce> taskForces;   // This is a sorted list of the task forces represented by this marker.

    private final Label titleText = new Label();

    private int selectedTaskForceIndex = TASK_FORCE_INITIAL_INDEX;

    private final PatrolMarkers patrolMarkers;

    private final List<MenuItem> navalOperationsMenuItems = new ArrayList<>();
    private final List<MenuItem> airOperationsMenuItems = new ArrayList<>();
    private final List<MenuItem> detachMenuItems = new ArrayList<>();
    private final MenuItem joinMenuItem = new MenuItem("Join ...");

    private boolean selected = false;

    /**
     * Constructor called by guice.
     *
     * @param taskForceGrid The task force map grid.
     * @param mapView The map view.
     * @param game The game.
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     */
    @Inject
    public TaskForceMarker(@Assisted final TaskForceGrid taskForceGrid,
                           @Assisted final MapView mapView,
                                     final Game game,
                                     final ImageResourceProvider imageResourceProvider,
                                     final ViewProps props) {
        this.game = game;
        this.taskForceGrid = taskForceGrid;
        this.mapView = mapView;

        Side side = taskForceGrid.getSide();

        this.image = new VBox(imageResourceProvider.getImageView(props.getString("taskforce." + side.toString().toLowerCase() + ".marker")));

        int gridSize = props.getInt("taskforce.mainMap.gridSize");
        GridView gridView = new GridView(gridSize, taskForceGrid.getGameGrid());

        image.setLayoutX(gridView.getX() + props.getInt("taskforce.marker.xoffset"));
        image.setLayoutY(gridView.getY() + props.getInt("taskforce.marker.yoffset"));
        image.setViewOrder(ViewOrder.MARKER.getValue());
        image.setUserData(this);
        image.setId("map-taskforce-grid-marker");

        patrolMarkers = new PatrolMarkers(mapView, taskForceGrid, gridView);

        title = buildTitle(gridView);
        taskForces =  taskForceGrid
                .getTaskForces()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        setUpContextMenus();
    }

    /**
     * Draw the task force marker.
     */
    public void draw() {
        if (!taskForceGrid.isBaseGrid()) {
            mapView.add(image);
        }
    }

    /**
     * Draw this base marker's patrol range.
     */
    public void toggleMarkers() {
        if (selected) {
            patrolMarkers.hide();   // Need to hide the previously selected task force patrol markers.
            patrolMarkers.draw(taskForces.get(selectedTaskForceIndex));
        } else {
            patrolMarkers.hide();
        }
    }

    /**
     * This base marker has been selected.
     *
     * @return True if the marker is selected. False if the marker is not selected.
     */
    public boolean selectMarker() {
        selected = determineIfSelected();

        if (selected) {
            image.setEffect(null);
            showTitle();
        } else {
            InnerShadow innerShadow = new InnerShadow(SHADOW_RADIUS, Color.WHITE);
            image.setEffect(innerShadow);
            hideTitle();
        }

        toggleMarkers();
        return selected;
    }

    /**
     * Remove the task force marker.
     */
    public void hide() {
        mapView.remove(image);
    }

    /**
     * Set the Base marker clicked handler.
     *
     * @param handler The mouse click event handler.
     */
    public void setClickHandler(final EventHandler<? super MouseEvent> handler) {
        image.setOnMouseClicked(handler);
    }

    /**
     * Set the naval operations menu items handler.
     *
     * @param handler The naval operations menu items handler.
     */
    public void setNavalOperationsMenuHandler(final EventHandler<ActionEvent> handler) {
        navalOperationsMenuItems.forEach(menuItem -> menuItem.setOnAction(handler));
    }

    /**
     * Set the air operations menu items handler.
     *
     * @param handler The air operations menu items handler.
     */
    public void setAirOperationsMenuHandler(final EventHandler<ActionEvent> handler) {
        airOperationsMenuItems.forEach(menuItem -> menuItem.setOnAction(handler));
    }

    /**
     * Set the detach menu items handler.
     *
     * @param handler The detach menu items handler.
     */
    public void setDetachMenuHandler(final EventHandler<ActionEvent> handler) {
        detachMenuItems.forEach(menuItem -> menuItem.setOnAction(handler));
    }

    /**
     * Set the join menu handler.
     *
     * @param handler The join menu handler.
     */
    public void setJoinMenuHandler(final EventHandler<ActionEvent> handler) {
        joinMenuItem.setOnAction(handler);
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
     * Setup the right click context menus for the base marker.
     */
    private void setUpContextMenus() {
        if (taskForceGrid.getSide()  == game.getHumanSide()) {

            ContextMenu contextMenu = new ContextMenu();
            List<Menu> taskForceMenus = buildTaskForceMenus();

            contextMenu.getItems().addAll(taskForceMenus);

            joinMenuItem.setUserData(taskForces);
            joinMenuItem.setDisable(taskForces.size() < 2);

            contextMenu.getItems().add(joinMenuItem);

            image.setOnContextMenuRequested(e -> contextMenu.show(image, e.getScreenX(), e.getScreenY()));
        }
    }

    private List<Menu> buildTaskForceMenus() {
       return taskForces
                .stream()
                .map(this::buildTaskForceMenuItems)
                .collect(Collectors.toList());

    }

    private Menu buildTaskForceMenuItems(final TaskForce taskForce) {
        Menu taskForceMenu = new Menu(taskForce.getName());

        MenuItem navalOperationsMenuItem = new MenuItem("Naval Operations...");
        navalOperationsMenuItem.setUserData(taskForce);
        navalOperationsMenuItems.add(navalOperationsMenuItem);

        MenuItem airOperationsMenuItem = new MenuItem("Air Operations...");
        airOperationsMenuItem.setUserData(taskForce);
        airOperationsMenuItem.setDisable(!taskForce.areSquadronsPresent());
        airOperationsMenuItems.add(airOperationsMenuItem);

        MenuItem detachMenuItem = new MenuItem("Detach...");
        detachMenuItem.setUserData(taskForce);
        detachMenuItems.add(detachMenuItem);

        taskForceMenu
                .getItems()
                .addAll(navalOperationsMenuItem, airOperationsMenuItem, detachMenuItem);

        return taskForceMenu;
    }

    /**
     * Build the base's title.
     *
     * @param gridView The grid view of this base.
     * @return A node containing the base's title.
     */
    private Node buildTitle(final GridView gridView) {
        //Tooltip tooltip = new Tooltip();
        //tooltip.setText(getToolTipText());

        //label.setTooltip(tooltip);
        VBox vBox = new VBox(titleText);
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

    private boolean determineIfSelected() {
        selectedTaskForceIndex++;
        if (selectedTaskForceIndex < taskForces.size()) {
            titleText.setText(taskForces.get(selectedTaskForceIndex).toString());
            return true;
        } else {
            selectedTaskForceIndex = TASK_FORCE_INITIAL_INDEX;
            return false;
        }
    }
}
