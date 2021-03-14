package engima.waratsea.view.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.InfoPane;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.view.util.GridPaneMap;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Task force summary asset view.
 *
 * CSS styles used.
 *
 * - asset-component-pane
 * - asset-hbox
 * - component-grid
 * - task-force-summary-flag
 */
public class TaskForceAssetSummaryView implements AssetView {
    private static final String ROUNDEL = ".roundel.image";

    private final ViewProps props;
    private final ResourceProvider resourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();
    private final TitledPane managementPane = new TitledPane();
    private final TitledPane landingTypesPane = new TitledPane();

    @Getter private final Button airOperations = new Button("Air Operations");
    @Getter private final Button navalOperations = new Button("Naval Operations");

    @Getter private final TabPane shipTabPane = new TabPane();

    private final Map<String, Tab> tabMap = new HashMap<>();

    private final Provider<InfoPane> infoProvider;

    @Getter private TaskForceViewModel viewModel;

    private final InfoPane shipSummary;
    private final Map<String, InfoPane> squadronInfo = new HashMap<>();
    private final Map<String, InfoPane> missionInfo = new HashMap<>();
    private final Map<String, InfoPane> patrolInfo = new HashMap<>();
    private final Map<String, InfoPane> readyInfo = new HashMap<>();

    private final ImageView assetImage = new ImageView();

    private Map<Nation, ImageView> flagImageViews;


    @Getter private HBox node;

    @Inject
    public TaskForceAssetSummaryView(final ViewProps props,
                                     final ResourceProvider resourceProvider,
                                     final InfoPane taskForceInfo,
                                     final Provider<InfoPane> infoProvider) {
        this.props = props;
        this.resourceProvider = resourceProvider;
        this.shipSummary = taskForceInfo;
        this.infoProvider = infoProvider;

    }

    /**
     * Build the task force's summary for the game's asset pane.
     *
     * @param taskForceViewModel The task force view model.
     * @return The node that contains the task force's asset summary.
     */
    public TaskForceAssetSummaryView build(final TaskForceViewModel taskForceViewModel) {
        viewModel = taskForceViewModel;

        buildSummary();
        buildManagementPane();
        buildLandingTypes();
        buildShipsTabPane();

        BoundTitledGridPane shipSummaryNode = shipSummary.build("Ship Summary");
        shipSummaryNode.setMinHeight(props.getInt("asset.pane.component.height"));
        shipSummaryNode.getStyleClass().add("asset-component-pane");

        node = new HBox(summaryPane, shipSummaryNode, managementPane, landingTypesPane, shipTabPane);
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
     * @param airbaseViewModel The air base view model.
     */
    public void setShip(final AirbaseViewModel airbaseViewModel) {
        String title = airbaseViewModel
                .getAirbase()
                .getValue()
                .getTitle();

        Tab tab = tabMap.get(title);
        shipTabPane.getSelectionModel().select(tab);
    }

    /**
     * Reset the model binding.
     *
     * @param taskForceViewModel The new task force view model that this object is bound too.
     */
    public void reset(final TaskForceViewModel taskForceViewModel) {
        viewModel = taskForceViewModel;

        viewModel
                .getTaskForceAirViewModel()
                .getAirbases()
                .forEach(this::bindShip);
    }

    private void buildSummary() {
        summaryPane.setText("Task Force Summary");
        summaryPane.getStyleClass().add("asset-component-pane");

        summaryGrid.setGridStyleId("component-grid");
        summaryGrid.setWidth(props.getInt("asset.pane.grid.component.width"));

        Node grid = summaryGrid.buildGrid();

        flagImageViews = viewModel
                .getTaskForceNavalViewModel()
                .getTaskForce()
                .getValue()
                .getNations()
                .stream()
                .map(nation -> new Pair<>(nation, new ImageView()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        List<VBox> flags = flagImageViews
                .values()
                .stream()
                .map(VBox::new)
                .peek(vb -> vb.getStyleClass().add("task-force-summary-flag"))
                .collect(Collectors.toList());

        VBox flagVBox = new VBox();
        flagVBox.getChildren().addAll(flags);
        flagVBox.getStyleClass().add("spacing-5");

        VBox imageVBox = new VBox(assetImage, flagVBox);
        imageVBox.getStyleClass().add("spacing-20");

        HBox hBox = new HBox(imageVBox, grid);
        hBox.getStyleClass().add("component-background");
        hBox.getStyleClass().add("spacing-10");

        summaryPane.setContent(hBox);
    }

    private void buildManagementPane() {
        managementPane.setText("Task Force Management");

        TaskForce taskForce = viewModel
                .getTaskForceNavalViewModel()
                .getTaskForce()
                .getValue();

        airOperations.setUserData(taskForce);
        navalOperations.setUserData(taskForce);

        airOperations.setMinWidth(props.getInt("asset.pane.button.long.width"));
        navalOperations.setMinWidth(props.getInt("asset.pane.button.long.width"));

        VBox vbox = new VBox(airOperations, navalOperations);

        if (taskForce.atPort()) {
            Tooltip airOptsTooltip = new Tooltip("Air operations not allowed while in port.");
            Tooltip.install(vbox, airOptsTooltip);
        }

        vbox.setId("asset-management-vbox");

        managementPane.setContent(vbox);

        managementPane.getStyleClass().add("asset-component-pane");
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
    private void buildShipsTabPane() {
        shipTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        shipTabPane.setId("asset-nations-tab-pane");

        viewModel
                .getTaskForceAirViewModel()
                .getAirbases()
                .stream()
                .sorted()
                .map(this::buildShipTab)
                .forEach(tab -> shipTabPane.getTabs().add(tab));
    }

    /**
     * Build a ship's tab.
     *
     * @param airbaseViewModel The air base view model.
     * @return The ship's tab.
     */
    private Tab buildShipTab(final AirbaseViewModel airbaseViewModel) {
        String title = airbaseViewModel
                .getAirbase()
                .getValue()
                .getTitle();

        Tab tab = new Tab();
        tab.setText(title);
        tab.setUserData(airbaseViewModel);

        Nation nation = getNation(airbaseViewModel);
        ImageView roundel = resourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));

        tab.setGraphic(roundel);

        squadronInfo.put(title, infoProvider.get());
        TitledPane squadronInfoNode = squadronInfo.get(title).build("Squadron Summary");
        squadronInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        squadronInfoNode.getStyleClass().add("asset-component-pane");

        missionInfo.put(title, infoProvider.get());
        TitledPane missionInfoNode = missionInfo.get(title).build("Mission Summary");
        missionInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        missionInfoNode.getStyleClass().add("asset-component-pane");

        patrolInfo.put(title, infoProvider.get());
        TitledPane patrolInfoNode = patrolInfo.get(title).build("Patrol Summary");
        patrolInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        patrolInfoNode.getStyleClass().add("asset-component-pane");

        readyInfo.put(title, infoProvider.get());
        TitledPane readyInfoNode = readyInfo.get(title).build("Ready Summary");
        readyInfoNode.setMinHeight(props.getInt("asset.pane.nation.component.height"));
        readyInfoNode.getStyleClass().add("asset-component-pane");

        HBox hBox = new HBox(squadronInfoNode, readyInfoNode, missionInfoNode, patrolInfoNode);
        hBox.setId("asset-nation-tab-hbox");

        hBox.setFillHeight(false);

        tab.setContent(hBox);

        tabMap.put(title, tab);

        return tab;
    }

    /**
     * Bind the view model.
     **/
    private void bind() {
        bindSummary();
        bindLandingTypes();

        shipSummary.bindIntegers(viewModel
                .getTaskForceNavalViewModel()
                .getShipCounts());

        BooleanProperty squadronsPresent = viewModel
                .getTaskForceAirViewModel()
                .getSquadronsPresent();

        BooleanProperty atPort = viewModel
                .getTaskForceAirViewModel()
                .getAtPort();

        airOperations
                .disableProperty()
                .bind(squadronsPresent.not().or(atPort));


        viewModel
                .getTaskForceAirViewModel()
                .getAirbases()
                .forEach(this::bindShip);
    }

    /**
     * Show the summary for the selected airfield.
     */
    private void bindSummary() {
        Image image = resourceProvider.getImage(props.getString("anchor.medium.icon"));
        assetImage.setImage(image);

        viewModel
                .getTaskForceNavalViewModel()
                .getTaskForce()
                .getValue()
                .getNations()
                .forEach(nation -> {
                    Image flag = resourceProvider.getImage(props.getString(nation.toString() + ".naval.flag.small.image"));
                    flagImageViews.get(nation).setImage(flag);
                });

        summaryGrid.updateGrid(getTaskForceData());
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
     * Bind the patrol view model for the given nation.
     *
     * @param airbaseViewModel The air base view model.
     */
    private void bindShip(final AirbaseViewModel airbaseViewModel) {
        Nation nation = getNation(airbaseViewModel);
        NationAirbaseViewModel nationAirbaseViewModel = airbaseViewModel
                .getNationViewModels()
                .get(nation);

        String title = airbaseViewModel
                .getAirbase()
                .getValue()
                .getTitle();

        squadronInfo
                .get(title)
                .bindIntegers(nationAirbaseViewModel.getSquadronCounts());

        missionInfo
                .get(title)
                .bindIntegers(nationAirbaseViewModel.getMissionCounts());

        patrolInfo
                .get(title)
                .bindIntegers(nationAirbaseViewModel.getPatrolCounts());

        readyInfo
                .get(title)
                .bindIntegers(nationAirbaseViewModel.getReadyCounts());
    }

    /**
     * Build the landing type check boxes that indicate which
     * landing types the airfield support.
     *
     * @param landingType An aircraft/squadron landing type.
     * @return A checkbox corresponding to the given landing type.
     */
    private CheckBox buildCheckBox(final LandingType landingType) {
        TaskForce taskForce = viewModel
                .getTaskForceNavalViewModel()
                .getTaskForce()
                .getValue();

        CheckBox checkBox = new CheckBox(landingType.toString());
        if (taskForce.getLandingType().contains(landingType)) {
            checkBox.setSelected(true);
        }
        checkBox.setDisable(true);
        return checkBox;
    }

    /**
     * Get the Airbase's data.
     *
     * @return The given airbase's data.
     */
    private Map<String, String> getTaskForceData() {
        TaskForce taskForce = viewModel
                .getTaskForceNavalViewModel()
                .getTaskForce()
                .getValue();

        Map<String, String> data = new LinkedHashMap<>();

        String space1 = "";

        data.put("Name:", taskForce.getName());
        data.put("Title:", taskForce.getTitle());
        data.put(space1, "");
        data.put("Mission:", taskForce.getMission().getType().toString());
        data.put("State:", taskForce.getState().toString());
        data.put("Location:", taskForce.getLocation());

        return data;
    }

    /**
     * Get the nation of the airbase. For task force's each airbase is a single ship.
     * A single ship has only one nation.
     *
     * @param airbaseViewModel The airbase view model.
     * @return The nation of the airbase view model. The backing model is a single ship.
     */
    private Nation getNation(final AirbaseViewModel airbaseViewModel) {
        List<Nation> nations = List.copyOf(airbaseViewModel.getNations());

        return nations.get(0); // There is only one nation for ships.
    }
}
