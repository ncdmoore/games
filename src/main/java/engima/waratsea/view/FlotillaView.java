package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.map.FlotillaPreviewMapView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the flotilla summary view.
 */
public class FlotillaView {
    private static final String CSS_FILE = "flotillaView.css";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    private Game game;
    private FlotillaPreviewMapView flotillaMap;

    @Getter
    private ChoiceBox<Flotilla> flotillas = new ChoiceBox<>();

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    private Map<Side, String> flags = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     * @param game The game.
     * @param flotillaMap The flotilla map.
     */
    @Inject
    public FlotillaView(final ViewProps props,
                        final CssResourceProvider cssResourceProvider,
                        final ImageResourceProvider imageResourceProvider,
                        final Game game,
                        final FlotillaPreviewMapView flotillaMap) {
        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;

        this.game = game;

        this.flotillaMap = flotillaMap;

        flags.put(Side.ALLIES, "alliesFlag50x34.png");
        flags.put(Side.AXIS, "axisFlag50x34.png");
    }
    /**
     * Show the task forces summary view.
     *
     * @param stage The stage on which the task force scene is set.
     * @param scenario The selected scenario.
     */
    public void show(final Stage stage, final Scenario scenario) {
        Label title = new Label("Flotillas: " + scenario.getTitle());
        title.setId("title");
        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node objectivesPane = buildObjectives(scenario.getObjectives());

        Label labelPane = new Label("Flotillas:");
        labelPane.setId("label-pane");

        Node taskForceList = buildFlotillaList();
        Node pushButtons = buildPushButtons();

        Node map = flotillaMap.draw();

        HBox mapPane = new HBox(taskForceList, map);
        mapPane.setId("map-pane");

        VBox vBox = new VBox(titlePane, objectivesPane, labelPane, mapPane, pushButtons);

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Set the minefields.
     *
     * @param fields The minefields.
     */
    public void setFlotillas(final List<Flotilla> fields) {
        flotillas.getItems().clear();
        flotillas.getItems().addAll(fields);
    }

    /**
     * Build the selected scenario objective's text.
     *
     * @param objectives The objective's text.
     * @return The node that contains the selected scenario objective information.
     */
    private Node buildObjectives(final String objectives) {
        Label objectiveLabel = new Label("Objectives: Current Submarine Deployment.");
        ImageView alliesFlag = imageResourceProvider.getImageView(flags.get(game.getHumanSide()));

        HBox hBox = new HBox(alliesFlag, objectiveLabel);
        hBox.setId("objectives-pane");

        return hBox;
    }

    /**
     * build the task force list.
     *
     * @return Node containing the task force list.
     */
    private Node buildFlotillaList() {
        flotillas.setMaxWidth(props.getInt("taskForce.list.width"));
        flotillas.setMinWidth(props.getInt("taskForce.list.width"));

        VBox vBox = new VBox(flotillas/*, buildTaskForceStateDetails(), buildLegend()*/);
        vBox.setId("flotilla-vbox");

        return vBox;
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
}
