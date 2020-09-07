package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.TaskForceGrid;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * Represents a task force marker on the main game map.
 * Multiple task forces that occupy the same game grid are represented by a single task force marker.
 */
public class TaskForceMarker {
    @Getter
    private final TaskForceGrid taskForceGrid;

    private final Game game;
    private final MapView mapView;
    private final VBox image;

    @Getter private MenuItem operationsMenuItem;
    @Getter private MenuItem detachMenuItem;
    @Getter private MenuItem joinMenuItem;

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

        String scenarioName = game.getScenario().getName();
        Side side = taskForceGrid.getSide();

        this.image = new VBox(imageResourceProvider.getImageView(scenarioName, props.getString("taskforce." + side.toString().toLowerCase() + ".marker")));

        int gridSize = props.getInt("taskforce.mainMap.gridSize");
        GridView gridView = new GridView(gridSize, taskForceGrid.getGameGrid());

        image.setLayoutX(gridView.getX() + props.getInt("taskforce.marker.xoffset"));
        image.setLayoutY(gridView.getY() + props.getInt("taskforce.marker.yoffset"));
        image.setViewOrder(ViewOrder.MARKER.getValue());
        image.setUserData(this);
        image.setId("map-taskforce-grid-marker");

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
     * Remove the task force marker.
     */
    public void hide() {
        mapView.remove(image);
    }

    /**
     * Set the operations menu handler.
     *
     * @param handler The operations menu handler.
     */
    public void setOperationsMenuHandler(final EventHandler<ActionEvent> handler) {
        operationsMenuItem.setOnAction(handler);
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

            operationsMenuItem = new MenuItem("Operations...");
            detachMenuItem = new MenuItem("Detach...");
            joinMenuItem = new MenuItem("Join...");

            operationsMenuItem.setUserData(taskForceGrid.getTaskForces());
            detachMenuItem.setUserData(taskForceGrid.getTaskForces());
            joinMenuItem.setUserData(taskForceGrid.getTaskForces());
            joinMenuItem.setDisable(taskForceGrid.getTaskForces().size() < 2);

            contextMenu.getItems().addAll(operationsMenuItem, detachMenuItem, joinMenuItem);

            image.setOnContextMenuRequested(e -> contextMenu.show(image, e.getScreenX(), e.getScreenY()));
        }
    }
}
