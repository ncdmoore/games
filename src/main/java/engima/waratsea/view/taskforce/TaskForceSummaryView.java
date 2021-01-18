package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.taskforce.info.TaskForceInfo;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class TaskForceSummaryView {
    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    private final TaskForceInfo shipSummary;
    private final TaskForceInfo squadronSummary;


    @Inject
    public TaskForceSummaryView(final Provider<TaskForceInfo> infoProvider) {
        shipSummary = infoProvider.get();
        squadronSummary = infoProvider.get();
    }

    /**
     * Build the task force summary view.
     *
     * @return The Task force summary view.
     */
    public TaskForceSummaryView build() {
        titledPane.setId("taskforce-title-pane");

        Node shipSummaryNode = shipSummary.build("Ship Summary");
        Node squadronSummaryNode = squadronSummary.build("Squadron Summary");

        leftVBox.getChildren().addAll(titledPane, imageView, shipSummaryNode, squadronSummaryNode);
        leftVBox.setId("taskforce-summary-vbox");

        return this;
    }

    /**
     * Bind the task force summary view to the task force view model.
     *
     * @param viewModel The task force view model.
     * @return The node containing the task force view.
     */
    public Node bind(final TaskForceViewModel viewModel) {
        titledPane.textProperty().bind(viewModel.getNameAndTitle());
        imageView.imageProperty().bind(viewModel.getImage());
        shipSummary.bind(viewModel.getShipCounts());
        squadronSummary.bind(viewModel.getSquadronCounts());

        return leftVBox;
    }
}
