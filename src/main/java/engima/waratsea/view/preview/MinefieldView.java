package engima.waratsea.view.preview;

import com.google.inject.Inject;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.presenter.dto.map.MinefieldDTO;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.MinefieldPreviewMapView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.List;

/**
 * Represents the minefield view.
 * This allows the user to place minefields in certain sea zones.
 */
public class MinefieldView {
    private static final String CSS_FILE = "minefieldView.css";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    private Game game;

    @Getter
    private ChoiceBox<Minefield> minefields = new ChoiceBox<>();

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    private MinefieldPreviewMapView minefieldMap;

    private Label minesAvailableValue = new Label();
    private Label minesDeployedValue = new Label();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param cssResourceProvider Provides access to the css file.
     * @param imageResourceProvider Image file provider.
     * @param minefieldMap The minefield map.
     * @param game The game.
     */
    @Inject
    public MinefieldView(final ViewProps props,
                         final CssResourceProvider cssResourceProvider,
                         final ImageResourceProvider imageResourceProvider,
                         final MinefieldPreviewMapView minefieldMap,
                         final Game game) {
        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;

        this.minefieldMap = minefieldMap;

        this.game = game;

        minefieldMap.setSide(game.getHumanSide());
    }

    /**
     * Show the minefield view.
     *
     * @param stage The stage on which the minefield scene is set.
     * @param scenario The selected scenario.
     */
    public void show(final Stage stage, final Scenario scenario) {
        Label title = new Label("Minefields: " + scenario.getTitle());
        title.setId("title");
        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node objectivesPane = buildObjectives(scenario);

        Label labelPane = new Label("Minefield Zone:");
        labelPane.setId("label-pane");

        minefields.setMinWidth(props.getInt("minefield.list.width"));
        minefields.setMaxWidth(props.getInt("minefield.list.width"));

        VBox minefieldVbox = new VBox(minefields, buildMinefieldDetails(), buildLegend());
        minefieldVbox.setId("minefield-vbox");

        Node map = minefieldMap.draw();

        HBox mapPane = new HBox(minefieldVbox, map);
        mapPane.setId("map-pane");

        Node pushButtons = buildPushButtons();

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
    public void setMinefields(final List<Minefield> fields) {
        minefields.getItems().clear();
        minefields.getItems().addAll(fields);
    }

    /**
     * Set the selected minefield.
     *
     * @param minefield The selected minefield.
     */
    public void setSelectedMinefield(final Minefield minefield) {
        minesAvailableValue.setText(minefield.hasAvalaible() + "");
        minesDeployedValue.setText(minefield.hasDeployed() + "");
    }

    /**
     * Highlight the currently selected minefield.
     *
     * @param dto The minefield data transfer object.
     */
    public void highlightMinefield(final MinefieldDTO dto) {
        minefieldMap.highlight(dto);
    }

    /**
     * Remove the highlighting from a given minefield.
     *
     * @param dto The minefield data transfer object.
     */
    public void removeMinefieldHighlight(final MinefieldDTO dto) {
        minefieldMap.removeHighLight(dto);
    }

    /**
     * Mark a mine on the map.
     *
     * @param dto The mine data transfer object.
     */
    public void markMine(final MinefieldDTO dto) {
        minefieldMap.markMine(dto);
        Minefield minefield = dto.getMinefield();
        minesAvailableValue.setText(minefield.hasAvalaible() + "");
        minesDeployedValue.setText(minefield.hasDeployed() + "");

    }

    /**
     * Un mark a mine on the map.
     *
     * @param dto The mine data transfer object.
     */
    public void unMarkMine(final MinefieldDTO dto) {
        minefieldMap.unMarkMine(dto);
        Minefield minefield = dto.getMinefield();
        minesAvailableValue.setText(minefield.hasAvalaible() + "");
        minesDeployedValue.setText(minefield.hasDeployed() + "");
    }

    /**
     * Build the selected scenario objective's text.
     *
     * @param scenario The selected scenario.
     * @return The node that contains the selected scenario objective information.
     */
    private Node buildObjectives(final Scenario scenario) {
        Label objectiveLabel = new Label("Objectives: Place available mines in each minefield zone.");
        ImageView flag = imageResourceProvider.getImageView(scenario.getName(), props.getString(game.getHumanSide().toLower() + ".flag.medium.image"));

        HBox hBox = new HBox(flag, objectiveLabel);
        hBox.setId("objectives-pane");

        return hBox;
    }

    /**
     * Build the minefield details pane.
     *
     * @return The minefield details pane.
     */
    private Node buildMinefieldDetails() {

        Text minesAvailableLabel = new Text("Mines Available:");
        Text minesDeployedLabel = new Text("Mines deployed:");

        GridPane gridPane = new GridPane();
        gridPane.setId("minefield-details-grid");
        gridPane.add(minesAvailableLabel, 0, 0);
        gridPane.add(minesAvailableValue, 1, 0);
        gridPane.add(minesDeployedLabel, 0, 1);
        gridPane.add(minesDeployedValue, 1, 1);

        VBox vBox = new VBox(gridPane);
        vBox.setId("minefield-details-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Minefield Details");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("minefield.details.width"));
        titledPane.setMinWidth(props.getInt("minefield.details.width"));
        titledPane.setId("minefield-details-pane");

        return titledPane;
    }

    /**
     * Build the minefield preview map legend.
     *
     * @return The node that contains the minefield preview map legend.
     */
    private Node buildLegend() {

        VBox vBox = new VBox(minefieldMap.getLegend());
        vBox.setId("map-legend-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Map Legend");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("minefield.details.width"));
        titledPane.setMinWidth(props.getInt("minefield.details.width"));
        titledPane.setId("map-legend-pane");

        return titledPane;
    }

    /**
     * build the mine field push buttons.
     *
     * @return Node containing the push buttons.
     */
    private Node buildPushButtons() {
        HBox hBox =  new HBox(backButton, continueButton);
        hBox.setId("push-buttons");
        return hBox;
    }
}
