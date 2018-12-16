package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.ships.TaskForceState;
import engima.waratsea.presenter.map.TaskForceMarkerDTO;
import engima.waratsea.view.map.MapView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;

import java.util.List;

/**
 * Represents the task forces summary view.
 */
@Slf4j
public class TaskForceView {
    private static final String CSS_FILE = "taskForceView.css";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    @Getter
    private ListView<TaskForce> taskForces = new ListView<>();

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    private MapView taskForceMap;

    /**
     * Constructor called by guice.
     *
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     * @param taskForceMap The task force preview map.
     */
    @Inject
    public TaskForceView(final ViewProps props,
                         final CssResourceProvider cssResourceProvider,
                         final ImageResourceProvider imageResourceProvider,
                         final MapView taskForceMap) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.taskForceMap = taskForceMap;

        int numberOfColumns = props.getInt("taskforce.previewMap.columns");
        int numberOfRows = props.getInt("taskforce.previewMap.rows");
        int gridSize = props.getInt("taskforce.previewMap.gridSize");
        int yAdjust = props.getInt("taskforce.previewMap.popup.yScale");

        int yBottomThreshold = props.getInt("taskforce.previewMap.y.size");
        taskForceMap.init(numberOfColumns, numberOfRows, gridSize, yAdjust, yBottomThreshold);
    }

    /**
     * Show the task forces summary view.
     *
     * @param stage The stage on which the task force scene is set.
     */
    public void show(final Stage stage) {
        Label title = new Label("Task Forces");
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node mainPane = buildTaskForceList();
        Node pushButtons = buildPushButtons();

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        ImageView map = imageResourceProvider.getImageView("previewMap.png");

        StackPane p = new StackPane(map, taskForceMap.drawMapGrid());
        p.setAlignment(Pos.TOP_LEFT);

        HBox hBox = new HBox(mainPane, p);

        VBox vBox = new VBox(titlePane, hBox, pushButtons);

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * build the task force list.
     *
     * @return Node containing the task force list.
     */
    private Node buildTaskForceList() {

        taskForces.setCellFactory(param -> new ListCell<TaskForce>() {
            @Override
            protected void updateItem(final TaskForce item, final boolean empty) {
                super.updateItem(item, empty);

                if (item != null) {
                    setText(item.toString());
                    if (item.getState() == TaskForceState.RESERVE) {
                        getStyleClass().add("reserve");
                    } else {
                        getStyleClass().add("active");
                    }
                }
            }
        });

        taskForces.setMaxWidth(props.getInt("taskForce.list.width"));
        taskForces.setMaxHeight(props.getInt("taskForce.list.height"));

        return new VBox(taskForces);
    }

    /**
     * build the task force push buttons.
     *
     * @return Node containing the push buttons.
     */
    private Node buildPushButtons() {
        HBox hBox =  new HBox(backButton, continueButton);
        hBox.setId("push-buttons");
        return hBox;
    }

    /**
     * Set the task forces.
     *
     * @param forces The task forces.
     */
    public void setTaskForces(final List<TaskForce> forces) {
        taskForces.getItems().clear();
        taskForces.getItems().addAll(forces);
    }

    /**
     * Set the selected task force.
     *
     * @param taskForce the selected task force.
     */
    public void setTaskForce(final TaskForce taskForce) {
        String name = taskForce.getName();
        taskForceMap.selectMarker(name);
    }

    /**
     * Clear the selected task force marker.
     *
     * @param taskForce the task force whose marker is cleared.
     */
    public void clearTaskForce(final TaskForce taskForce) {
        String name = taskForce.getName();
        taskForceMap.clearMarker(name);
    }

    /**
     * Place a task force marker on the preview map.
     *
     * @param dto Task force data transfer object.
     */
    public void markTaskForceOnMap(final TaskForceMarkerDTO dto) {
        dto.setGridSize(taskForceMap.getGridSize());
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        taskForceMap.markTaskForce(dto);
    }

    /**
     * Finish the task force preview map.
     */
    public void finish() {
        taskForceMap.finish();
    }

    /**
     * Get the task force name from the task force marker that was clicked.
     *
     * @param clickedMarker The clicked marker.
     * @return The name of the task force that corresponds to the marker.
     */
    public List<String> getTaskForceFromMarker(final Object clickedMarker) {
        return taskForceMap.getNameFromMarker(clickedMarker);
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
       taskForceMap.closePopup(event);
    }
}
