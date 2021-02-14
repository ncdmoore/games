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
import javafx.scene.control.MenuItem;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.List;

/**
 * Represents a task force marker on the main game map.
 * Multiple task forces that occupy the same game grid are represented by a single task force marker.
 */
public class TaskForceMarker {
    private static final int SHADOW_RADIUS = 3;

    @Getter private final TaskForceGrid taskForceGrid;

    private final Game game;
    private final MapView mapView;
    private final VBox image;
    private final Node title;

    private final PatrolMarkers patrolMarkers;

    @Getter private MenuItem navalOperationsMenuItem;
    @Getter private MenuItem airOperationsMenuItem;
    @Getter private MenuItem detachMenuItem;
    @Getter private MenuItem joinMenuItem;

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
            patrolMarkers.draw();
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
        selected = !selected;

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
     * Set the naval operations menu handler.
     *
     * @param handler The naval operations menu handler.
     */
    public void setNavalOperationsMenuHandler(final EventHandler<ActionEvent> handler) {
        navalOperationsMenuItem.setOnAction(handler);
    }

    /**
     * Set the air operations menu handler.
     *
     * @param handler The air operations menu handler.
     */
    public void setAirOperationsMenuHandler(final EventHandler<ActionEvent> handler) {
        airOperationsMenuItem.setOnAction(handler);
    }

    /**
     * Set the detach menu handler.
     *
     * @param handler The detach menu handler.
     */
    public void setDetachMenuHandler(final EventHandler<ActionEvent> handler) {
        detachMenuItem.setOnAction(handler);
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
     * Setup the right click context menus for the base marker.
     */
    private void setUpContextMenus() {
        if (taskForceGrid.getSide()  == game.getHumanSide()) {

            ContextMenu contextMenu = new ContextMenu();

            navalOperationsMenuItem = new MenuItem("Naval Operations...");
            airOperationsMenuItem = new MenuItem("Air Operations...");
            detachMenuItem = new MenuItem("Detach...");
            joinMenuItem = new MenuItem("Join...");

            List<TaskForce> taskForces = taskForceGrid.getTaskForces();

            navalOperationsMenuItem.setUserData(taskForces);
            airOperationsMenuItem.setUserData(taskForces);
            airOperationsMenuItem.setDisable(noSquadronsPresent(taskForces));
            detachMenuItem.setUserData(taskForces);
            joinMenuItem.setUserData(taskForces);
            joinMenuItem.setDisable(taskForces.size() < 2);

            contextMenu.getItems().addAll(navalOperationsMenuItem, airOperationsMenuItem, detachMenuItem, joinMenuItem);

            image.setOnContextMenuRequested(e -> contextMenu.show(image, e.getScreenX(), e.getScreenY()));
        }
    }

    /**
     * Determine if the given task forces have any squadrons stationed.
     *
     * @param taskForces The task forces represented by this task force marker.
     * @return True if these task forces contain squadrons. False otherwise.
     */
    private boolean noSquadronsPresent(final List<TaskForce> taskForces) {
        return taskForces
                .stream()
                .noneMatch(TaskForce::areSquadronsPresent);
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

        Label label = new Label(taskForceGrid.getTitle());
        //label.setTooltip(tooltip);
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
}
