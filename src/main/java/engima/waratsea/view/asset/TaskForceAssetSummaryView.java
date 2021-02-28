package engima.waratsea.view.asset;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.GridPaneMap;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class TaskForceAssetSummaryView implements AssetView {
    private final ViewProps props;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();


    private TaskForceViewModel viewModel;

    @Getter private HBox node;

    @Inject
    public TaskForceAssetSummaryView(final ViewProps props) {
        this.props = props;
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

        node = new HBox(summaryPane);
        node.setId("asset-hbox");

        summaryPane.setMinHeight(props.getInt("asset.pane.component.height"));

        bind();

        return this;
    }

    private void buildSummary() {
        summaryPane.setText("Task Force Summary");
        summaryPane.getStyleClass().add("asset-component-pane");

        summaryGrid.setGridStyleId("component-grid");
        summaryGrid.setWidth(props.getInt("asset.pane.grid.component.width"));

        Node grid = summaryGrid.buildGrid();

        HBox hBox = new HBox(grid);
        hBox.setId("task-force-summary-hbox");

        summaryPane.setContent(hBox);
    }

    /**
     * Bind the view model.
     **/
    private void bind() {
        bindSummary();

    }

    /**
     * Show the summary for the selected airfield.
     */
    private void bindSummary() {
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
