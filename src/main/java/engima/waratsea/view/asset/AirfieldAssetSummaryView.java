package engima.waratsea.view.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.InfoPane;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.info.AirfieldRangeInfo;
import engima.waratsea.view.util.GridPaneMap;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The airfield asset summary view.
 *
 * CSS Styles used.
 *
 * - spacing-5
 * - spacing-20
 * - asset-component-pane
 * - component-grid
 * - airfield-summary-hbox
 * - airfield-nation-tab-hbox
 */
@Slf4j
public class AirfieldAssetSummaryView implements AssetView {
    private static final String ROUNDEL = ".roundel.image";

    private final ViewProps props;
    private final ResourceProvider resourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();
    private final TitledPane landingTypesPane = new TitledPane();
    private final TitledPane managementPane = new TitledPane();

    @Getter private final Button missionButton = new Button("Missions");
    @Getter private final Button patrolButton = new Button("Patrols");

    @Getter private final TabPane nationsTabPane = new TabPane();

    private final Map<Nation, Tab> tabMap = new HashMap<>();

    private final Provider<InfoPane> infoProvider;
    private final Provider<AirfieldRangeInfo> airfieldRangeInfoProvider;

    @Getter private AirbaseViewModel viewModel;
    private Airbase airbase;
    @Getter private final Map<Nation, AirfieldRangeInfo> rangeInfo = new HashMap<>();
    private final Map<Nation, InfoPane> regionInfo = new HashMap<>();
    private final Map<Nation, InfoPane> squadronInfo = new HashMap<>();
    private final Map<Nation, InfoPane> missionInfo = new HashMap<>();
    private final Map<Nation, InfoPane> patrolInfo = new HashMap<>();
    private final Map<Nation, InfoPane> readyInfo = new HashMap<>();

    private final ImageView assetImage = new ImageView();

    private Map<Nation, ImageView> flagImageViews;

    @Getter private HBox node;

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param resourceProvider Provides images.
     * @param airfieldRangeInfoProvider Provides airfield range information.
     * @param infoProvider Provides airfield mission information.
     */
    //CHECKSTYLE:OFF
    @Inject
    public AirfieldAssetSummaryView(final ViewProps props,
                                    final ResourceProvider resourceProvider,
                                    final Provider<AirfieldRangeInfo> airfieldRangeInfoProvider,
                                    final Provider<InfoPane> infoProvider) {
        //CHECKSTYLE:ON
        this.props = props;
        this.resourceProvider = resourceProvider;
        this.airfieldRangeInfoProvider = airfieldRangeInfoProvider;
        this.infoProvider = infoProvider;
    }

    /**
     * Build the airfield's summary for the game's asset pane.
     *
     * @param airbaseViewModel The airbase view model.
     * @return The node that contains the airfield's asset summary.
     */
    public AirfieldAssetSummaryView build(final AirbaseViewModel airbaseViewModel) {
        viewModel = airbaseViewModel;
        airbase = viewModel.getAirbaseModel();

        buildSummary();
        buildLandingTypes();
        buildManagementPane();
        buildNationsTabPane();

        node = new HBox(summaryPane, landingTypesPane, managementPane, nationsTabPane);
        node.setId("asset-hbox");

        summaryPane.setMinHeight(props.getInt("asset.pane.component.height"));
        managementPane.setMinHeight(props.getInt("asset.pane.component.height"));
        landingTypesPane.setMinHeight(props.getInt("asset.pane.component.height"));

        bind();

        return this;
    }

    /**
     * Select the given nation's tab.
     *
     * @param nation The nation.
     */
    public void setNation(final Nation nation) {
        Tab tab = tabMap.get(nation);
        nationsTabPane.getSelectionModel().select(tab);
    }

    /**
     * Reset the model binding.
     *
     * @param airbaseViewModel The new airbase view model that this object is bound too.
     */
    public void reset(final AirbaseViewModel airbaseViewModel) {
        viewModel = airbaseViewModel;
        airbase = viewModel.getAirbaseModel();

        airbase
                .getNations()
                .forEach(this::bindNation);
    }

    /**
     * Bind the view model.
     **/
    private void bind() {
        bindSummary();
        bindLandingTypes();

        airbase
                .getNations()
                .forEach(this::bindNation);
    }

    /**
     * Bind the patrol view model for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void bindNation(final Nation nation) {
        NationAirbaseViewModel nationAirbaseViewModel = viewModel.getNationViewModels().get(nation);

        rangeInfo
                .get(nation)
                .bind(nationAirbaseViewModel);

        regionInfo
                .get(nation)
                .bindStrings(nationAirbaseViewModel.getRegionCounts());

        squadronInfo
                .get(nation)
                .bindIntegers(nationAirbaseViewModel.getSquadronCounts());

        missionInfo
                .get(nation)
                .bindIntegers(nationAirbaseViewModel.getMissionCounts());

        patrolInfo
                .get(nation)
                .bindIntegers(nationAirbaseViewModel.getPatrolCounts());

        readyInfo
                .get(nation)
                .bindIntegers(nationAirbaseViewModel.getReadyCounts());
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

        Node grid = summaryGrid.buildGrid();

        flagImageViews = airbase
                .getNations()
                .stream()
                .map(nation -> new Pair<>(nation, new ImageView()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        VBox flagVBox = new VBox();
        flagVBox.getChildren().addAll(flagImageViews.values());
        flagVBox.getStyleClass().add("spacing-5");

        VBox imageVBox = new VBox(assetImage, flagVBox);

        imageVBox.getStyleClass().add("spacing-20");

        HBox hBox = new HBox(imageVBox, grid);
        hBox.setId("airfield-summary-hbox");

        summaryPane.setContent(hBox);
    }

    /**
     * Build the landing types.
     */
    private void buildLandingTypes() {
        landingTypesPane.setText("Landing Types");
        landingTypesPane.getStyleClass().add("asset-component-pane");
    }

    private void buildManagementPane() {
        managementPane.setText("Airfield Management");

        missionButton.setUserData(airbase);
        patrolButton.setUserData(airbase);

        missionButton.setMinWidth(props.getInt("asset.pane.button.width"));
        patrolButton.setMinWidth(props.getInt("asset.pane.button.width"));

        VBox vbox = new VBox(missionButton, patrolButton);
        vbox.setId("airfield-management-vbox");
        managementPane.setContent(vbox);

        managementPane.getStyleClass().add("asset-component-pane");
    }

    /**
     * Build the nation's tabs.
     */
    private void buildNationsTabPane() {
        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        nationsTabPane.setId("airfield-nations-tab-pane");

        airbase.
                getNations()
                .stream()
                .sorted()
                .map(this::buildNationTab)
                .forEach(tab -> nationsTabPane.getTabs().add(tab));
    }

    /**
     * Show the summary for the selected airfield.
     */
    private void bindSummary() {
        Image image = resourceProvider.getImage(props.getString(airbase.getSide().toLower() + ".airfield.medium.icon"));
        assetImage.setImage(image);

        airbase.getNations().forEach(nation -> {
            Image flag = resourceProvider.getImage(props.getString(nation.toString() + ".flag.small.image"));
            flagImageViews.get(nation).setImage(flag);
        });

        summaryGrid.updateGrid(getAirbaseData());
    }

    /**
     * Show landing types.
     */
    private void bindLandingTypes() {
        List<CheckBox> checkBoxes = LandingType
                .stream()
                .map(this::buildCheckBox)
                .collect(Collectors.toList());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(checkBoxes);
        vBox.setId("airfield-landing-type-vbox");

        landingTypesPane.setContent(vBox);
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
        tab.setUserData(nation);
        ImageView roundel = resourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));

        tab.setGraphic(roundel);

        rangeInfo.put(nation, airfieldRangeInfoProvider.get());
        TitledPane rangeInfoNode = rangeInfo.get(nation).build();
        rangeInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        rangeInfoNode.getStyleClass().add("asset-component-pane");

        regionInfo.put(nation, infoProvider.get());
        TitledPane regionInfoNode = regionInfo.get(nation).build("Region Step Summary");
        regionInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        regionInfoNode.getStyleClass().add("asset-component-pane");

        squadronInfo.put(nation, infoProvider.get());
        TitledPane squadronInfoNode = squadronInfo.get(nation).build("Squadron Summary");
        squadronInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        squadronInfoNode.getStyleClass().add("asset-component-pane");

        missionInfo.put(nation, infoProvider.get());
        TitledPane missionInfoNode = missionInfo.get(nation).build("Mission Summary");
        missionInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        missionInfoNode.getStyleClass().add("asset-component-pane");

        patrolInfo.put(nation, infoProvider.get());
        TitledPane patrolInfoNode = patrolInfo.get(nation).build("Patrol Summary");
        patrolInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        patrolInfoNode.getStyleClass().add("asset-component-pane");

        readyInfo.put(nation, infoProvider.get());
        TitledPane readyInfoNode = readyInfo.get(nation).build("Ready Summary");
        readyInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        readyInfoNode.getStyleClass().add("asset-component-pane");

        HBox hBox = new HBox(rangeInfoNode, regionInfoNode, squadronInfoNode, readyInfoNode, missionInfoNode, patrolInfoNode);
        hBox.setId("airfield-nation-tab-hbox");

        hBox.setFillHeight(false);

        tab.setContent(hBox);

        tabMap.put(nation, tab);

        return tab;
    }
}
