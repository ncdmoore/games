package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.map.TaskForcePreviewMapView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the squadron deployment view.
 */
public class SquadronView {
    private static final String CSS_FILE = "squadronView.css";
    private static final String ROUNDEL_SIZE = "20x20.png";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    @Getter
    private TabPane nationsTabPane;

    @Getter
    private Map<Nation, ChoiceBox<Region>> regions = new HashMap<>();

    @Getter
    private Map<Nation, ChoiceBox<Airfield>> airfields = new HashMap<>();

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    private Game game;
    private GameMap gameMap;

    private TaskForcePreviewMapView taskForceMap;

    private Map<Side, String> flags = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     * @param game The game.
     * @param gameMap The game map.
     * @param taskForceMap The task force preview map.
     */
    @Inject
    public SquadronView(final ViewProps props,
                        final CssResourceProvider cssResourceProvider,
                        final ImageResourceProvider imageResourceProvider,
                        final Game game,
                        final GameMap gameMap,
                        final TaskForcePreviewMapView taskForceMap) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.game = game;
        this.gameMap = gameMap;
        this.taskForceMap = taskForceMap;

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
        Label title = new Label("Squadrons: " + scenario.getTitle());
        title.setId("title");
        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node objectivesPane = buildObjectives(scenario);

        Node nationTabPane = buildNationTabs();
        Node pushButtons = buildPushButtons();

        Node map = taskForceMap.draw();

        HBox mapPane = new HBox(nationTabPane, map);
        mapPane.setId("map-pane");

        VBox vBox = new VBox(titlePane, objectivesPane, mapPane, pushButtons);

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Build the selected scenario objective's text.
     *
     * @param scenario The selected scenario.
     * @return The node that contains the selected scenario objective information.
     */
    private Node buildObjectives(final Scenario scenario) {
        Label objectiveLabel = new Label("Objectives:");
        Label objectiveValue = new Label(scenario.getObjectives());
        ImageView flag = imageResourceProvider.getImageView(scenario.getName(), flags.get(game.getHumanSide()));

        HBox hBox = new HBox(flag, objectiveLabel, objectiveValue);
        hBox.setId("objective-pane");

        return hBox;
    }

    /**
     * Build the nations tab pane.
     *
     * @return The nations tab pane.
     */
    private Node buildNationTabs() {
        nationsTabPane = new TabPane();
        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        nationsTabPane.setMinWidth(props.getInt("squadron.tabPane.width"));
        nationsTabPane.setMaxWidth(props.getInt("squadron.tabPane.width"));

        game
                .getHumanPlayer()
                .getNations()
                .stream()
                .map(this::buildTab)
                .forEach(tab -> nationsTabPane.getTabs().add(tab));

        return nationsTabPane;
    }

    /**
     * Build a nation tab.
     *
     * @param nation The nation.
     * @return The nation tab.
     */
    private Tab buildTab(final Nation nation) {
        Side side = game.getHumanSide();

        Tab tab = new Tab(nation.toString());

        Label regionLabel = new Label("Region:");
        ChoiceBox<Region> regionChoiceBox = new ChoiceBox<>();
        regionChoiceBox.getItems().addAll(gameMap.getNationRegions(side, nation));
        regionChoiceBox.setMinWidth(props.getInt("squadron.tabPane.width"));
        regionChoiceBox.setMaxWidth(props.getInt("squadron.tabPane.width"));
        VBox regionVBox = new VBox(regionLabel, regionChoiceBox);

        regions.put(nation, regionChoiceBox);

        Label airfieldLabel = new Label("Airfield:");
        ChoiceBox<Airfield> airfieldChoiceBox = new ChoiceBox<>();
        airfieldChoiceBox.getItems().addAll(gameMap.getNationAirfields(side, nation));
        airfieldChoiceBox.setMinWidth(props.getInt("squadron.tabPane.width"));
        airfieldChoiceBox.setMaxWidth(props.getInt("squadron.tabPane.width"));
        VBox airfieldVBox = new VBox(airfieldLabel, airfieldChoiceBox);

        airfields.put(nation, airfieldChoiceBox);

        VBox vBox = new VBox(regionVBox, airfieldVBox);
        vBox.setId("squadron-vbox");

        tab.setContent(vBox);

        ImageView roundel = imageResourceProvider.getImageView(nation + ROUNDEL_SIZE);

        tab.setGraphic(roundel);
        return tab;
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
