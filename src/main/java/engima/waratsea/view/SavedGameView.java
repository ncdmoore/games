package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.List;

public class SavedGameView {
    private static final String CSS_FILE = "savedGameView.css";

    @Getter
    private ListView<GameData> savedGames = new ListView<>();

    private ImageView scenarioImage = new ImageView();
    private Text dateValue = new Text();
    private Text turnValue = new Text();
    private Text descriptionValue = new Text();
    private Text sideValue = new Text();

    @Getter
    private Button continueButton;

    @Getter
    private Button backButton;

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    /**
     * Constructor called by guice.
     *
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     */
    @Inject
    public SavedGameView(final ViewProps props,
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
        Label title = new Label("Select a saved game");
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Label label = new Label("Saved Games:");

        Node savedGamesList = buildSavedGamesList();
        Node scenarioDetails = buildScenarioDetails();

        HBox savedGamesBox = new HBox(savedGamesList, scenarioDetails);

        Node pushButtons = buildPushButtons();

        VBox mainPane = new VBox(label, savedGamesBox, pushButtons);
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
     * Set the saved games in the list view.
     *
     * @param games the saved games.
     */
    public void setSavedGames(final List<GameData> games) {
        savedGames.getItems().clear();
        savedGames.getItems().addAll(games);
    }

    /**
     * Update the view with the currently selected saved game.
     *
     * @param savedGame currently selected saved game.
     */
    public void setSelectedSavedGame(final GameData savedGame) {
        Scenario scenario = savedGame.getScenario();
        setScenarioImages(scenario);
        setScenarioDetails(scenario);

        sideValue.setText(savedGame.getHumanSide().toString());
    }

    /**
     * Set the scenario details.
     * @param scenario currently selected scenario.
     */
    private void setScenarioDetails(final Scenario scenario) {
        turnValue.setText(Integer.toString(scenario.getMaxTurns()));
        dateValue.setText(scenario.getDate());
        descriptionValue.setText(scenario.getDescription());
    }

    /**
     * Build the scenario image and list box.
     * @return The node that contains the scenario image and list box.
     */
    private Node buildSavedGamesList() {
        savedGames.setMaxWidth(props.getInt("scenario.list.width"));
        savedGames.setMaxHeight(props.getInt("scenario.list.height"));

        return new VBox(scenarioImage, savedGames);
    }

    /**
     * Build the scenario details node.
     * @return The node that contains the scenario details.
     */
    private GridPane buildScenarioDetails() {

        Text dateLabel = new Text("Date:");
        Text turnLabel = new Text("Number of Turns:");
        Text descriptionLabel = new Text("Description:");
        Text sideLabel = new Text("Side:");

        descriptionValue.setWrappingWidth(props.getInt("scenario.description.wrap"));

        final int row3 = 3;

        GridPane gridPane = new GridPane();
        gridPane.add(dateLabel, 0, 0);
        gridPane.add(dateValue, 1, 0);
        gridPane.add(turnLabel, 0, 1);
        gridPane.add(turnValue, 1, 1);
        gridPane.add(descriptionLabel, 0, 2);
        gridPane.add(descriptionValue, 1, 2);
        gridPane.add(sideLabel, 0, row3);
        gridPane.add(sideValue, 1, row3);


        gridPane.setId("scenario-details");
        GridPane.setValignment(descriptionLabel, VPos.TOP);

        return gridPane;
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

    /**
     * Set the scenario image.
     *
     * @param scenario currently selected scenario.
     */
    private void setScenarioImages(final Scenario scenario) {
        String name = scenario.getName();

        Image image = imageResourceProvider.getImage(name, scenario.getImage());
        scenarioImage.setImage(image);
    }
}
