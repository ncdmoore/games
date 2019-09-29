package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.util.TitledGridPane;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.ListUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AirfieldDetailsView {
    private static final String ROUNDEL_SIZE = "20x20.png";

    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private Airfield airfield;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     */
    @Inject
    public AirfieldDetailsView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the airfield details.
     *
     * @param field The airfield whose details are shown.
     * @return A node containing the airfield details.
     */
    public Node show(final Airfield field) {
        airfield = field;

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        airfield
                .getNations()
                .stream()
                .sorted()
                .map(this::createNationTab)
                .forEach(tab -> tabPane.getTabs().add(tab));

        return tabPane;
    }

    /**
     * Create the given nation's tab.
     *
     * @param nation The nation.
     * @return The nation's tab.
     */
    private Tab createNationTab(final Nation nation) {
        Tab tab = new Tab(nation.toString());

        AirfieldType airfieldType = airfield.getAirfieldType();

        ImageView airfieldView = imageResourceProvider.getImageView(nation + "Airfield" + airfieldType + "Details.png");
        ImageView roundel = imageResourceProvider.getImageView(nation + ROUNDEL_SIZE);

        TitledGridPane airfieldDetails = buildAirfieldDetails();
        TitledGridPane airfieldSteps = buildAirfieldSteps(nation);

        VBox leftVBox = new VBox(airfieldView, airfieldDetails, airfieldSteps);
        leftVBox.setId("left-vbox");


        TitledPane missions = buildMissionDetails();
        TitledPane patrols = buildPatrolDetails();
        TitledPane ready = buildReadyDetails(nation);

        Accordion accordion = new Accordion();

        accordion.getPanes().addAll(missions, patrols, ready);
        accordion.setExpandedPane(missions);

        HBox hBox = new HBox(leftVBox, accordion);
        hBox.setId("main-pane");

        tab.setGraphic(roundel);
        tab.setContent(hBox);


        return tab;
    }

    /**
     * Build the airfield details.
     *
     * @return A titled grid pane containing the airfield details.
     */
    private TitledGridPane buildAirfieldDetails() {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .buildPane(airfield.getTitle(), getAirfieldDetails());
    }

    /**
     * Get the airfield details.
     *
     * @return A map of the airfield details.
     */
    private Map<String, String> getAirfieldDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Max Step Capacity:", airfield.getMaxCapacity() + "");
        details.put("Current Step Capacity:", airfield.getCapacity() + "");
        details.put("Current Steps deployed:", airfield.getCurrentSteps() + "");
        details.put("AA Rating:", airfield.getAntiAir() + "");

        return details;
    }

    /**
     * Build the airfield squadron step summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled grid pane containing the airfield step summary.
     */
    private TitledGridPane buildAirfieldSteps(final Nation nation) {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .buildPane("Airfield Step Summary", getAirfieldSteps(nation));
    }

    /**
     * Get the airfield step summary for each type of squadron.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A map of the airfield steps where the key is the type
     * of squadron and the value is the total number of steps of that
     * type of squadron.
     */
    private Map<String, String> getAirfieldSteps(final Nation nation) {
       Map<SquadronViewType, BigDecimal> steps = airfield.getStepMap(nation)
               .entrySet()
               .stream()
               .collect(Collectors.toMap(e -> SquadronViewType.get(e.getKey()),
                                         Map.Entry::getValue,
                                         BigDecimal::add,
                                         LinkedHashMap::new));

       // Convert the map to a string key value pair for display on the GUI.
       return steps
               .entrySet()
               .stream()
               .collect(Collectors.toMap(e -> e.getKey().toString(),
                                         e -> e.getValue().toString(),
                                         (oldValue, newValue) -> oldValue,
                                         LinkedHashMap::new));
    }

    /**
     * Build the mission details pane.
     *
     * @return A titled pane containing the mission details of the airfield.
     */
    private TitledPane buildMissionDetails() {
        TitledPane titledPane = new TitledPane();

        titledPane.setText("Missions");

        Label label = new Label("mission data");

        titledPane.setContent(label);


        return titledPane;
    }

    /**
     * Build the patrol details pane.
     *
     * @return A titled pane containing the partol details of the airfield.
     */
    private TitledPane buildPatrolDetails() {
        TitledPane titledPane = new TitledPane();

        titledPane.setText("Patrols");

        Label label = new Label("patrol data");

        titledPane.setContent(label);

        return titledPane;
    }

    /**
     * Build the ready details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the ready details of the airfield.
     */
    private TitledPane buildReadyDetails(final Nation nation) {
        TitledPane titledPane = new TitledPane();

        titledPane.setText("Ready");

        Map<SquadronViewType, List<Squadron>> squadrons = airfield.getSquadronMap(nation)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> SquadronViewType.get(e.getKey()),
                                          Map.Entry::getValue,
                                          ListUtils::union,
                                          LinkedHashMap::new));

        TilePane tilePane = new TilePane();
        tilePane.setPrefColumns(props.getInt("airfield.dialog.ready.columns"));

        squadrons.forEach((type, list) -> {

            Label title = new Label(type + ":");

            ListView<Squadron> listView = new ListView<>();
            listView.getItems().addAll(list);
            listView.setMaxHeight(props.getInt("airfield.dialog.ready.list.width"));
            listView.setMinHeight(props.getInt("airfield.dialog.ready.list.width"));

            VBox vBox = new VBox(title, listView);

            tilePane.getChildren().add(vBox);
        });


        titledPane.setContent(tilePane);

        return titledPane;
    }
}
