package engima.waratsea.view.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.info.AirfieldMissionInfo;
import engima.waratsea.view.airfield.info.AirfieldPatrolInfo;
import engima.waratsea.view.airfield.info.AirfieldRangeInfo;
import engima.waratsea.view.airfield.info.AirfieldReadyInfo;
import engima.waratsea.view.airfield.info.AirfieldRegionInfo;
import engima.waratsea.view.airfield.info.AirfieldSquadronInfo;
import engima.waratsea.view.util.GridPaneMap;
import engima.waratsea.viewmodel.AirbaseViewModel;
import engima.waratsea.viewmodel.NationAirbaseViewModel;
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
import java.util.stream.Stream;

@Slf4j
public class AirfieldAssetSummaryView implements AssetView {
    private static final String ROUNDEL = ".roundel.image";

    private final ViewProps props;
    private final ImageResourceProvider imageResourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();
    private final TitledPane landingTypesPane = new TitledPane();
    private final TitledPane managementPane = new TitledPane();

    @Getter private final Button missionButton = new Button("Missions");
    @Getter private final Button patrolButton = new Button("Patrols");

    @Getter private final TabPane nationsTabPane = new TabPane();

    private final Provider<AirfieldRegionInfo> airfieldRegionInfoProvider;
    private final Provider<AirfieldSquadronInfo> airfieldSquadronInfoProvider;
    private final Provider<AirfieldMissionInfo> airfieldMissionInfoProvider;
    private final Provider<AirfieldPatrolInfo> airfieldPatrolInfoProvider;
    private final Provider<AirfieldReadyInfo> airfieldReadyInfoProvider;
    private final Provider<AirfieldRangeInfo> airfieldRangeInfoProvider;

    @Getter private AirbaseViewModel viewModel;
    private Airbase airbase;
    @Getter private final Map<Nation, AirfieldRangeInfo> rangeInfo = new HashMap<>();
    private final Map<Nation, AirfieldRegionInfo> regionInfo = new HashMap<>();
    private final Map<Nation, AirfieldSquadronInfo> squadronInfo = new HashMap<>();
    private final Map<Nation, AirfieldMissionInfo> missionInfo = new HashMap<>();
    private final Map<Nation, AirfieldPatrolInfo> patrolInfo = new HashMap<>();
    private final Map<Nation, AirfieldReadyInfo> readyInfo = new HashMap<>();

    private final ImageView assetImage = new ImageView();

    private Map<Nation, ImageView> flagImageViews;

    @Getter private HBox node;

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param imageResourceProvider Provides images.
     * @param airfieldRangeInfoProvider Provides airfield range information.
     * @param airfieldRegionInfoProvider Provides airfield region information.
     * @param airfieldSquadronInfoProvider Provides airfield squadron information.
     * @param airfieldMissionInfoProvider Provides airfield mission information.
     * @param airfieldPatrolInfoProvider Provides airfield patrol information.
     * @param airfieldReadyInfoProvider Provides airfield ready information.
     */
    //CHECKSTYLE:OFF
    @Inject
    public AirfieldAssetSummaryView(final ViewProps props,
                                    final ImageResourceProvider imageResourceProvider,
                                    final Provider<AirfieldRangeInfo> airfieldRangeInfoProvider,
                                    final Provider<AirfieldRegionInfo> airfieldRegionInfoProvider,
                                    final Provider<AirfieldSquadronInfo> airfieldSquadronInfoProvider,
                                    final Provider<AirfieldMissionInfo> airfieldMissionInfoProvider,
                                    final Provider<AirfieldPatrolInfo> airfieldPatrolInfoProvider,
                                    final Provider<AirfieldReadyInfo> airfieldReadyInfoProvider) {
        //CHECKSTYLE:ON
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.airfieldRangeInfoProvider = airfieldRangeInfoProvider;
        this.airfieldRegionInfoProvider = airfieldRegionInfoProvider;
        this.airfieldSquadronInfoProvider = airfieldSquadronInfoProvider;
        this.airfieldMissionInfoProvider = airfieldMissionInfoProvider;
        this.airfieldPatrolInfoProvider = airfieldPatrolInfoProvider;
        this.airfieldReadyInfoProvider = airfieldReadyInfoProvider;
    }

    /**
     * Build the airfield's summary for the game's asset pane.
     *
     * @param airbaseViewModel The airbase view model.
     * @return The node that contains the airfield's asset summary.
     */
    public AirfieldAssetSummaryView build(final AirbaseViewModel airbaseViewModel) {
        viewModel = airbaseViewModel;
        airbase = viewModel.getAirbase();

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
     * Reset the model binding.
     *
     * @param airbaseViewModel The new airbase view model that this object is bound too.
     */
    public void reset(final AirbaseViewModel airbaseViewModel) {
        viewModel = airbaseViewModel;
        airbase = viewModel.getAirbase();

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
                .bind(nationAirbaseViewModel);

        squadronInfo
                .get(nation)
                .bind(nationAirbaseViewModel);

        missionInfo
                .get(nation)
                .bind(nationAirbaseViewModel);

        patrolInfo
                .get(nation)
                .bind(nationAirbaseViewModel);

        readyInfo
                .get(nation)
                .bind(nationAirbaseViewModel);
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
        flagVBox.setId("airfield-summary-flag-vbox");

        VBox imageVBox = new VBox(assetImage, flagVBox);

        imageVBox.setId("airfield-summary-image-vbox");

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
                .map(this::buildNationTab)
                .forEach(tab -> nationsTabPane.getTabs().add(tab));
    }

    /**
     * Show the summary for the selected airfield.
     */
    private void bindSummary() {
        Image image = imageResourceProvider.getImage(props.getString(airbase.getSide().toLower() + ".airfield.medium.icon"));
        assetImage.setImage(image);

        airbase.getNations().forEach(nation -> {
            Image flag = imageResourceProvider.getImage(props.getString(nation.toString() + ".flag.small.image"));
            flagImageViews.get(nation).setImage(flag);
        });

        summaryGrid.updateGrid(getAirbaseData());
    }

    /**
     * Show landing types.
     */
    private void bindLandingTypes() {
        List<CheckBox> checkBoxes = Stream.of(LandingType.values())
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
        ImageView roundel = imageResourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));

        tab.setGraphic(roundel);

        rangeInfo.put(nation, airfieldRangeInfoProvider.get());
        TitledPane rangeInfoNode = rangeInfo.get(nation).build();
        rangeInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        rangeInfoNode.getStyleClass().add("asset-component-pane");

        regionInfo.put(nation, airfieldRegionInfoProvider.get());
        TitledPane regionInfoNode = regionInfo.get(nation).build();
        regionInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        regionInfoNode.getStyleClass().add("asset-component-pane");

        squadronInfo.put(nation, airfieldSquadronInfoProvider.get());
        TitledPane squadronInfoNode = squadronInfo.get(nation).build();
        squadronInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        squadronInfoNode.getStyleClass().add("asset-component-pane");

        missionInfo.put(nation, airfieldMissionInfoProvider.get());
        TitledPane missionInfoNode = missionInfo.get(nation).build();
        missionInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        missionInfoNode.getStyleClass().add("asset-component-pane");

        patrolInfo.put(nation, airfieldPatrolInfoProvider.get());
        TitledPane patrolInfoNode = patrolInfo.get(nation).build();
        patrolInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        patrolInfoNode.getStyleClass().add("asset-component-pane");

        readyInfo.put(nation, airfieldReadyInfoProvider.get());
        TitledPane readyInfoNode = readyInfo.get(nation).build();
        readyInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        readyInfoNode.getStyleClass().add("asset-component-pane");

        HBox hBox = new HBox(rangeInfoNode, regionInfoNode, squadronInfoNode, readyInfoNode, missionInfoNode, patrolInfoNode);
        hBox.setId("airfield-nation-tab-hbox");

        hBox.setFillHeight(false);

        tab.setContent(hBox);

        return tab;
    }
}
