package engima.waratsea.view.preview;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;

import java.util.List;

/**
 * Defines the scenario selection view.
 */
public class ScenarioView {
    private static final String CSS_FILE = "scenarioView.css";

    @Getter private ListView<Scenario> scenarios = new ListView<>();
    @Getter private RadioButton radioButtonAllies;
    @Getter private RadioButton radioButtonAxis;
    @Getter private Button backButton;
    @Getter private Button continueButton;

    private ImageView  scenarioImage = new ImageView();
    private Text dateValue = new Text();
    private Text turnValue = new Text();
    private Text descriptionValue = new Text();

    private ImageView alliesFlag = new ImageView();
    private ImageView axisFlag = new ImageView();

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    /**
     * Constructor called by guice.
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     */
    @Inject
    public ScenarioView(final ViewProps props,
                        final CssResourceProvider cssResourceProvider,
                        final ImageResourceProvider imageResourceProvider) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
    }

    /**
     * Show the scenario selection view.
     * @param stage The stage on which the view is set.
     */
    public void show(final Stage stage) {
        Label title = new Label("Select a Scenario");
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Label label = new Label("Scenarios:");

        Node scenarioList = buildScenarioList();
        Node scenarioDetails = buildScenarioDetails();
        HBox scenarioBox = new HBox(scenarioList, scenarioDetails);

        Node radioButtons = buildRadioButtons();
        Node pushButtons = buildPushButtons();

        VBox mainPane = new VBox(label, scenarioBox, radioButtons, pushButtons);
        mainPane.setId("main-pane");

        VBox vBox = new VBox(titlePane, mainPane);

        int sceneWidth = props.getInt("scenario.scene.width");
        int sceneHeight = props.getInt("scenario.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * .
     * @param s the scenarios
     */
    public void setScenarios(final List<Scenario> s) {
        scenarios.getItems().clear();
        scenarios.getItems().addAll(s);
    }

    /**
     * Update the view with the selected currently scenario.
     * @param scenario selected currently scenario
     */
    public void setScenario(final Scenario scenario) {
        setScenarioImages(scenario);
        setScenarioDetails(scenario);
    }

    /**
     * Set the scenario image.
     * @param scenario currently selected scenario.
     */
    private void setScenarioImages(final Scenario scenario) {
        String name = scenario.getName();

        Image image = imageResourceProvider.getImage(name, scenario.getImage());
        scenarioImage.setImage(image);

        Image axisFlagImage = imageResourceProvider.getImage(name, props.getString("axis.flag.medium.image"));
        axisFlag.setImage(axisFlagImage);

        Image alliesFlagImage = imageResourceProvider.getImage(name, props.getString("allies.flag.medium.image"));
        alliesFlag.setImage(alliesFlagImage);

    }

    /**
     * Set the scenario details.
     * @param scenario currently selected scenario.
     */
    private void setScenarioDetails(final Scenario scenario) {
        turnValue.setText(Integer.toString(scenario.getMaxTurns()));
        dateValue.setText(scenario.getDateString());
        descriptionValue.setText(scenario.getDescription());
    }

    /**
     * Build the scenario image and list box.
     * @return The node that contains the scenario image and list box.
     */
    private Node buildScenarioList() {
        scenarios.setMaxWidth(props.getInt("scenario.list.width"));
        scenarios.setMaxHeight(props.getInt("scenario.list.height"));

        return new VBox(scenarioImage, scenarios);
    }

    /**
     * Build the scenario details node.
     * @return The node that contains the scenario details.
     */
    private GridPane buildScenarioDetails() {

        Text dateLabel = new Text("Date:");
        Text turnLabel = new Text("Number of Turns:");
        Text descriptionLabel = new Text("Description:");

        descriptionValue.setWrappingWidth(props.getInt("scenario.description.wrap"));

        GridPane gridPane = new GridPane();
        gridPane.add(dateLabel, 0, 0);
        gridPane.add(dateValue, 1, 0);
        gridPane.add(turnLabel, 0, 1);
        gridPane.add(turnValue, 1, 1);
        gridPane.add(descriptionLabel, 0, 2);
        gridPane.add(descriptionValue, 1, 2);
        gridPane.setId("scenario-details");
        GridPane.setValignment(descriptionLabel, VPos.TOP);

        return gridPane;
    }

    /**
     * Build the side radio buttons.
     * @return The node that contains the side radio buttons.
     */
    private Node buildRadioButtons() {

        alliesFlag = imageResourceProvider.getImageView(props.getString("allies.flag.medium.image"));
        axisFlag = imageResourceProvider.getImageView(props.getString("axis.flag.medium.image"));

        radioButtonAllies = new RadioButton("Allies");
        radioButtonAxis = new RadioButton("Axis");

        radioButtonAllies.setSelected(true);

        ToggleGroup sideGroup = new ToggleGroup();
        radioButtonAllies.setToggleGroup(sideGroup);
        radioButtonAxis.setToggleGroup(sideGroup);

        HBox hBox = new HBox(alliesFlag, radioButtonAllies, radioButtonAxis, axisFlag);
        hBox.setId("side-radio-buttons");
        return hBox;

    }

    /**
     * Build the push buttons.
     * @return The node that contains the push buttons.
     */
    private Node buildPushButtons() {
        backButton = new Button("Back");
        continueButton = new Button("Continue");

        HBox hBox =  new HBox(backButton, continueButton);
        hBox.setId("push-buttons");
        return hBox;
    }
}
