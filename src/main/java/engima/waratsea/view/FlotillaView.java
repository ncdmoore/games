package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.map.FlotillaPreviewMapView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
    private TabPane flotillaTabPane;

    @Getter
    private Map<FlotillaType, ChoiceBox<Flotilla>> flotillas = new HashMap<>();

    private Map<FlotillaType, Label> stateValue = new HashMap<>();
    private Map<FlotillaType, Label> locationValue = new HashMap<>();

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    @Getter
    private List<Button> vesselButtons;

    private TilePane vesselPane;

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

        Node objectivesPane = buildObjectives(scenario);

        Label labelPane = new Label("Flotillas:");
        labelPane.setId("label-pane");

        Node taskForceList = buildTabPane();
        Node pushButtons = buildPushButtons();

        Node submarineButtons = buildVesselButtons();

        Node map = flotillaMap.draw();

        HBox mapPane = new HBox(taskForceList, map);
        mapPane.setId("map-pane");

        VBox vBox = new VBox(titlePane, objectivesPane, labelPane, mapPane, submarineButtons, pushButtons);

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
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @param items The flotillas.
     */
    public void setFlotillas(final FlotillaType flotillaType, final List<Flotilla> items) {
        flotillas.get(flotillaType).getItems().clear();
        flotillas.get(flotillaType).getItems().addAll(items);
    }

    /**
     * Clear the selected flotilla marker.
     *
     * @param flotilla the flotilla whose marker is cleared.
     */
    public void clearFlotilla(final Flotilla flotilla) {
        String name = flotilla.getName();
        flotillaMap.clearMarker(name);
    }

    /**
     * Remove the given flotilla marker.
     *
     * @param flotilla the flotilla whose marker is removed.
     */
    public void removeFlotilla(final Flotilla flotilla) {
        String name = flotilla.getName();
        flotillaMap.removeMarker(name);
    }

    /**
     * Mark the flotilla on the preview map.
     *
     * @param dto Asset marker data transfer object.
     */
    public void markFlotillaOnMap(final TaskForceMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        flotillaMap.markFlotilla(dto);
    }

    /**
     * Set the selected flotilla.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     * @param flotilla The selected flotilla
     */
    public void setSelectedFlotilla(final FlotillaType flotillaType, final Flotilla flotilla) {
        flotillaMap.selectMarker(flotilla.getName());

        stateValue.get(flotillaType).setText(flotilla.getState().toString());

        String prefix = flotilla.atFriendlyBase() ? "At port " : "At sea zone ";

        locationValue.get(flotillaType).setText(prefix + flotilla.getMappedLocation());

        setVesselButtons(flotilla);
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
        flotillaMap.closePopup(event);
    }

    /**
     * Build the selected scenario objective's text.
     *
     * @param scenario The selected scenario
     * @return The node that contains the selected scenario objective information.
     */
    private Node buildObjectives(final Scenario scenario) {
        Label objectiveLabel = new Label("Objectives: Current Submarine Deployment.");
        ImageView flag = imageResourceProvider.getImageView(scenario.getName(), flags.get(game.getHumanSide()));

        HBox hBox = new HBox(flag, objectiveLabel);
        hBox.setId("objectives-pane");

        return hBox;
    }

    /**
     * Build the flotilla tab pane.
     *
     * @return The flotilla tab pane.
     */
    private Node buildTabPane() {
        flotillaTabPane = new TabPane();
        flotillaTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        flotillaTabPane.setMinWidth(props.getInt("flotilla.tabPane.width"));
        flotillaTabPane.setMaxWidth(props.getInt("flotilla.tabPane.width"));

        Stream.of(FlotillaType.values())
                .filter(this::hasTab)
                .map(this::buildTab)
                .forEach(tab -> flotillaTabPane.getTabs().add(tab));

        return flotillaTabPane;
    }

    /**
     * Determine if the given flotilla type is present and a tab is needed for it.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return True if the player has a flotilla of the given type. False otherwise.
     */
    private boolean hasTab(final FlotillaType flotillaType) {
        return game.getHumanPlayer().hasFlotilla(flotillaType);
    }

    /**
     * Build a flotilla tab.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return A flotilla tab.
     */
    private Tab buildTab(final FlotillaType flotillaType) {
        Tab tab = new Tab(flotillaType.toString());

        tab.setContent(buildFlotillaList(flotillaType));

        return tab;
    }
    /**
     * Build the flotilla choice box.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return The flotilla choice box.
     */
    private ChoiceBox<Flotilla> buildChoiceBox(final FlotillaType flotillaType) {
        ChoiceBox<Flotilla> submarineChoiceBox = new ChoiceBox<>();

        flotillas.put(flotillaType, submarineChoiceBox);

        return submarineChoiceBox;
    }

    /**
     * build the task force list.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     * @return Node containing the task force list.
     */
    private Node buildFlotillaList(final FlotillaType flotillaType) {
        ChoiceBox<Flotilla> flotillaChoiceBox = buildChoiceBox(flotillaType);

        flotillaChoiceBox.setMaxWidth(props.getInt("taskForce.list.width"));
        flotillaChoiceBox.setMinWidth(props.getInt("taskForce.list.width"));

        VBox vBox = new VBox(flotillaChoiceBox, buildFlotillaDetails(flotillaType), buildLegend());
        vBox.setId("flotilla-vbox");

        return vBox;
    }

    /**
     * Build the flotilla details.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return A node containing the flotilla details.
     */
    private Node buildFlotillaDetails(final FlotillaType flotillaType) {

        Text stateLabel = new Text("State:");
        Text locationLabel = new Text("Location:");

        stateValue.put(flotillaType, new Label());
        locationValue.put(flotillaType, new Label());


        GridPane gridPane = new GridPane();
        gridPane.setId("flotilla-details-grid");
        gridPane.add(stateLabel, 0, 0);
        gridPane.add(stateValue.get(flotillaType), 1, 0);
        gridPane.add(locationLabel, 0, 1);
        gridPane.add(locationValue.get(flotillaType), 1, 1);

        VBox vBox = new VBox(gridPane);
        vBox.setId("flotilla-details-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Flotilla Details");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));
        titledPane.setId("flotilla-details-pane");

        return titledPane;
    }

    /**
     * Build the task force preview map legend.
     *
     * @return The node that contains the task force preview map legend.
     */
    private Node buildLegend() {

        VBox vBox = new VBox(flotillaMap.getLegend());
        vBox.setId("map-legend-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Map Legend");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));

        return titledPane;
    }

    /**
     * Build the node that contains the vessel buttons.
     *
     * @return The node that contains the vessel flotilla buttons.
     */
    private Node buildVesselButtons() {
        vesselPane = new TilePane();
        vesselPane.setId("flotilla-pane");

        ScrollPane sp = new ScrollPane();
        sp.setContent(vesselPane);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return sp;
    }

    /**
     * Build the flotilla vessel buttons.
     *
     * @param flotilla The selected vessel flotilla
     */
    private void setVesselButtons(final Flotilla flotilla) {
        vesselPane.getChildren().clear();
        vesselButtons = new ArrayList<>();

        flotilla.getVessels()
                .forEach(vessel -> {
                    Button button = new Button(vessel.getName());
                    button.setUserData(vessel);
                    button.setMinWidth(props.getInt("taskForce.ship.label.width"));
                    button.setMaxWidth(props.getInt("taskForce.ship.label.width"));
                    vesselPane.getChildren().add(button);
                    vesselButtons.add(button);
                });
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
