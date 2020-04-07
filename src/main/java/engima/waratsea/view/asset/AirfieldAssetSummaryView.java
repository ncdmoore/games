package engima.waratsea.view.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.info.AirfieldPatrolInfo;
import engima.waratsea.view.airfield.info.AirfieldSquadronInfo;
import engima.waratsea.view.util.GridPaneMap;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AirfieldAssetSummaryView implements AssetView {
    private static final String ROUNDEL = ".roundel.image";

    private final ViewProps props;
    private final ImageResourceProvider imageResourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();

    private final TitledPane landingTypesPane = new TitledPane();

    private final TabPane nationsTabPane = new TabPane();

    private final Provider<AirfieldSquadronInfo> airfieldSquadronInfoProvider;
    private final Provider<AirfieldPatrolInfo> airfieldPatrolInfoProvider;

    private Airbase airbase;
    private AirfieldSquadronInfo squadronInfo;
    private AirfieldPatrolInfo patrolInfo;

    @Getter private HBox node;

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param imageResourceProvider Provides images.
     * @param airfieldSquadronInfoProvider Provides airfield squadron information.
     * @param airfieldPatrolInfoProvider Provides airfield patrol information.
     */
    @Inject
    public AirfieldAssetSummaryView(final ViewProps props,
                                    final ImageResourceProvider imageResourceProvider,
                                    final Provider<AirfieldSquadronInfo> airfieldSquadronInfoProvider,
                                    final Provider<AirfieldPatrolInfo> airfieldPatrolInfoProvider) {
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.airfieldSquadronInfoProvider = airfieldSquadronInfoProvider;
        this.airfieldPatrolInfoProvider = airfieldPatrolInfoProvider;
    }

    /**
     * Build the airfield's summary for the game's asset pane.
     *
     * @return The node that contains the airfield's asset summary.
     */
    public Node build() {
        buildSummary();
        buildLandingTypes();
        buildNationsTabPane();

        node = new HBox(summaryPane, landingTypesPane, nationsTabPane);
        node.setId("asset-hbox");

        summaryPane.setMinHeight(props.getInt("asset.pane.component.height"));
        landingTypesPane.setMinHeight(props.getInt("asset.pane.component.height"));

        return node;
    }

    /**
     * Show the given airbase in the asset summary view.
     *
     * @param base The airbase displayed in the asset summary view.
     */
    public void show(final Airbase base) {
        airbase = base;

        showSummary();
        showLandingTypes();
        showNationsTab();
    }

    /**
     * Update the patrol with the number of squadrons on patrol.
     *
     * @param patrolType The type of patrol.
     * @param numOnPatrol The number of squadron on patrol of the given type.
     */
    public void updatePatrol(final PatrolType patrolType, final int numOnPatrol) {
        patrolInfo.update(patrolType, numOnPatrol);
    }

    /**
     * Update the airfield's asset view based on the current airbase data.
     */
    public void update() {
        patrolInfo.update();
    }

    /**
     * Build the airfield summary.
     **/
    private void buildSummary() {
        summaryPane.setText("Airfield Summary");
        summaryPane.getStyleClass().add("asset-component-pane");

        final int firstColumnWidth = 60;
        final int secondColumnWidth = 40;

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(firstColumnWidth);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(secondColumnWidth);

        summaryGrid.setGridStyleId("component-grid");
        summaryGrid.setWidth(props.getInt("asset.pane.grid.component.width"));
        summaryGrid.setColumnConstraints(List.of(col1, col2));
    }

    /**
     * Build the landing types.
     */
    private void buildLandingTypes() {
        landingTypesPane.setText("Landing Types");
        landingTypesPane.getStyleClass().add("asset-component-pane");
    }

    /**
     * Build the nation's tabs.
     */
    private void buildNationsTabPane() {
        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        nationsTabPane.setId("airfield-nations-tab-pane");
    }

    /**
     * Show the summary for the selected airfield.
     */
    private void showSummary() {
        Image image = imageResourceProvider.getImage(props.getString(airbase.getSide().toLower() + ".airfield.medium.icon"));

        Node imageView = new ImageView(image);
        Node grid = summaryGrid.buildGrid(getAirbaseData());

        HBox hBox = new HBox(imageView, grid);
        hBox.setId("airfield-summary-hbox");

        summaryPane.setContent(hBox);
    }

    /**
     * Show landing types.
     */
    private void showLandingTypes() {
        List<CheckBox> checkBoxes = Stream.of(LandingType.values())
                .map(this::buildCheckBox)
                .collect(Collectors.toList());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(checkBoxes);
        vBox.setId("airfield-landing-type-vbox");

        landingTypesPane.setContent(vBox);
    }

    /**
     * Show the nations tab.
     */
    private void showNationsTab() {
        airbase.
                getNations()
                .stream()
                .map(this::buildNationTab)
                .forEach(tab -> nationsTabPane.getTabs().add(tab));
    }

    /**
     * Get the Airbase's data.
     *
     * @return The given airbase's data.
     */
    private Map<String, String> getAirbaseData() {
        Map<String, String> data = new LinkedHashMap<>();

        String space1 = "";
        String space2 = " ";

        data.put("Name:", airbase.getTitle());
        data.put("Region:", airbase.getRegionTitle());
        data.put(space1, "");
        data.put("Max Capacity (Steps):", airbase.getMaxCapacity() + "");
        data.put("Current Capacity (Steps):", airbase.getCapacity() + "");
        data.put("Current stationed (Steps):", airbase.getCurrentSteps().toBigInteger().toString());
        data.put(space2, "");
        data.put("AA Rating:", airbase.getAntiAirRating() + "");

        return data;
    }

    /**
     * Build the landing type check boxes that indicate which
     * landing types the airfield support.
     *
     * @param landingType An aircraft/squadron landing type.
     * @return A checkbox corresponding to the given landing type.
     */
    private CheckBox buildCheckBox(final LandingType landingType) {
        CheckBox checkBox = new CheckBox(landingType.toString());
        if (airbase.getLandingType().contains(landingType)) {
            checkBox.setSelected(true);
        }
        checkBox.setDisable(true);
        return checkBox;
    }

    /**
     * Build a nation's tab.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The nation's tab.
     */
    private Tab buildNationTab(final Nation nation) {
        Tab tab = new Tab();
        tab.setText(nation.toString());
        ImageView roundel = imageResourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));

        tab.setGraphic(roundel);

        squadronInfo = airfieldSquadronInfoProvider.get();
        Node squadronInfoNode = squadronInfo.build();
        squadronInfo.setAirbase(nation, airbase);

        patrolInfo = airfieldPatrolInfoProvider.get();
        Node patrolInfoNode = patrolInfo.build();
        patrolInfo.setAirbase(nation, airbase);

        HBox hBox = new HBox(squadronInfoNode, patrolInfoNode);
        hBox.setId("airfield-nation-tab-hbox");

        tab.setContent(hBox);

        return tab;
    }
}
