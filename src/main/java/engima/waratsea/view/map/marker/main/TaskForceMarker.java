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

public class TaskForceMarker {
    @Getter
    private final TaskForceGrid taskForceGrid;

    private final Game game;
    private final MapView mapView;
    private final VBox image;

    @Getter private MenuItem operationsMenuItem;

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
     * Setup the right click context menus for the base marker.
     */
    private void setUpContextMenus() {
        if (taskForceGrid.getSide()  == game.getHumanSide()) {

            ContextMenu contextMenu = new ContextMenu();

            operationsMenuItem = new MenuItem("Operations...");
            operationsMenuItem.setUserData(taskForceGrid.getTaskForces());

            contextMenu.getItems().addAll(operationsMenuItem);

            image.setOnContextMenuRequested(e -> contextMenu.show(image, e.getScreenX(), e.getScreenY()));
        }
    }
}
