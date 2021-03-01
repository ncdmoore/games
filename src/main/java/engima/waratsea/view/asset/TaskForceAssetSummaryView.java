package engima.waratsea.view.asset;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.taskforce.info.TaskForceInfo;
import engima.waratsea.view.util.GridPaneMap;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.Node;
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

public class TaskForceAssetSummaryView implements AssetView {
    private final ViewProps props;
    private final ImageResourceProvider imageResourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();

    private final TaskForceInfo shipSummary;

    private final ImageView assetImage = new ImageView();
    private Map<Nation, ImageView> flagImageViews;

    @Getter private TaskForceViewModel viewModel;

    @Getter private HBox node;

    @Inject
    public TaskForceAssetSummaryView(final ViewProps props,
                                     final ImageResourceProvider imageResourceProvider,
                                     final TaskForceInfo taskForceInfo) {
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;

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

        Node shipSummaryNode = shipSummary.build("Ship Summary");

        node = new HBox(summaryPane, shipSummaryNode);
        node.setId("asset-hbox");

        summaryPane.setMinHeight(props.getInt("asset.pane.component.height"));

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
                .peek(vb -> vb.setId("task-force-summary-flag"))
                .collect(Collectors.toList());

        VBox flagVBox = new VBox();
        flagVBox.getChildren().addAll(flags);
        flagVBox.setId("task-force-summary-flag-vbox");

        VBox imageVBox = new VBox(assetImage, flagVBox);
        imageVBox.setId("task-force-summary-image-vbox");

        HBox hBox = new HBox(imageVBox, grid);
        hBox.setId("task-force-summary-hbox");

        summaryPane.setContent(hBox);
    }

    /**
     * Bind the view model.
     **/
    private void bind() {
        bindSummary();

        shipSummary.bind(viewModel
                .getTaskForceNavalViewModel()
                .getShipCounts());
    }

    /**
     * Show the summary for the selected airfield.
     */
    private void bindSummary() {
        Image image = imageResourceProvider.getImage(props.getString("anchor.medium.icon"));
        assetImage.setImage(image);

        viewModel
                .getTaskForceNavalViewModel()
                .getTaskForce()
                .getValue()
                .getNations()
                .forEach(nation -> {
                    Image flag = imageResourceProvider.getImage(props.getString(nation.toString() + ".naval.flag.small.image"));
                    flagImageViews.get(nation).setImage(flag);
                });

        summaryGrid.updateGrid(getTaskForceData());
    }

    /**
     * Get the Airbase's data.
     *
     * @return The given airbase's data.
     */
    private Map<String, String> getTaskForceData() {
        TaskForce taskForce = viewModel.getTaskForceNavalViewModel().getTaskForce().getValue();

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
