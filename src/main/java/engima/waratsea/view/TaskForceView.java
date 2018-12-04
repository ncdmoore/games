package engima.waratsea.view;

import com.google.inject.Inject;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    /**
     * Constructor called by guice.
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     */
    @Inject
    public TaskForceView(final ViewProps props,
                         final CssResourceProvider cssResourceProvider,
                         final ImageResourceProvider imageResourceProvider) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
    }

    /**
     * Show the task forces summary view.
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

        StackPane p = new StackPane(map, drawColumn());
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
     * @return Node containing the task force list.
     */
    private Node buildTaskForceList() {
        taskForces.setMaxWidth(props.getInt("taskForce.list.width"));
        taskForces.setMaxHeight(props.getInt("taskForce.list.height"));

        return new VBox(taskForces);
    }

    /**
     * build the task force push buttons.
     * @return Node containing the push buttons.
     */
    private Node buildPushButtons() {
        HBox hBox =  new HBox(backButton, continueButton);
        hBox.setId("push-buttons");
        return hBox;
    }

    /**
     * Set the task forces.
     * @param s
     */
    public void setTaskForces(final List<TaskForce> s) {
        taskForces.getItems().clear();
        taskForces.getItems().addAll(s);
    }

    /**
     * Set the selected task force.
     * @param taskForce the selected task force.
     */
    public void setTaskForce(final TaskForce taskForce) {

    }

    /**
     * Draw the map.
     * @return map
     */
    public Node drawColumn() {
        //VBox node = new VBox();

        Group node = new Group();

        for (int count = 0; count < 30; count++) {
            Node r = draw(0, count, 0);
            node.getChildren().add(r);
        }

        for (int count = 0; count < 29; count++) {
            Node r = draw(14, count, 7);
            node.getChildren().add(r);
        }


        for (int count = 0; count < 30; count++) {
            Node r = draw(28, count, 0);
            node.getChildren().add(r);
        }


        ImageView i = imageResourceProvider.getImageView("axisFlag50x34.png");

        i.setX(14);
        i.setY(28);

        node.getChildren().add(i);

        return node;
    }

    /**
     * draw a rectangle.
     * @param xoffset x coordinate offset
     * @param yoffset y coordinate offset
     * @param z
     * @return
     */
    public Node draw(int xoffset, int yoffset, int z) {
        log.info("draw {}", yoffset);

        Rectangle r = new Rectangle(xoffset, yoffset * 14 + 10 + z, 14, 14);
        r.setStroke(Color.BLACK);
        r.setFill(null);
        return r;
    }

}
