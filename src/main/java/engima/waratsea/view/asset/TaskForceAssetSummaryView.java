package engima.waratsea.view.asset;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.InfoPane;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.view.util.GridPaneMap;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.Getter;

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
    private final ViewProps props;
    private final ResourceProvider resourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();
    private final TitledPane managementPane = new TitledPane();
    private final TitledPane landingTypesPane = new TitledPane();

    @Getter private final Button airOperations = new Button("Air Operations");
    @Getter private final Button navalOperations = new Button("Naval Operations");

    private final InfoPane shipSummary;

    private final ImageView assetImage = new ImageView();
    private Map<Nation, ImageView> flagImageViews;

    @Getter private TaskForceViewModel viewModel;

    @Getter private HBox node;

    @Inject
    public TaskForceAssetSummaryView(final ViewProps props,
                                     final ResourceProvider resourceProvider,
                                     final InfoPane taskForceInfo) {
        this.props = props;
        this.resourceProvider = resourceProvider;

        this.shipSummary = taskForceInfo;
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

        BoundTitledGridPane shipSummaryNode = shipSummary.build("Ship Summary");
        shipSummaryNode.setMinHeight(props.getInt("asset.pane.component.height"));
        shipSummaryNode.getStyleClass().add("asset-component-pane");

        node = new HBox(summaryPane, shipSummaryNode, managementPane, landingTypesPane);
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
     * @param taskForceViewModel The new task force view model that this object is bound too.
     */
    public void reset(final TaskForceViewModel taskForceViewModel) {
        viewModel = taskForceViewModel;
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
     * Bind the view model.
     **/
    private void bind() {
        bindSummary();
        bindLandingTypes();

        shipSummary.bindIntegers(viewModel
                .getTaskForceNavalViewModel()
                .getShipCounts());

        airOperations
                .disableProperty()
                .bind(viewModel
                        .getTaskForceAirViewModel()
                        .getSquadronsPresent()
                        .not());

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
}
