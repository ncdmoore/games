package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.taskforce.info.TaskForceInfo;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class TaskForceAirSummaryView {
    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    private final TaskForceInfo squadronSummary;


    @Inject
    public TaskForceAirSummaryView(final Provider<TaskForceInfo> infoProvider) {
        squadronSummary = infoProvider.get();
    }

    /**
     * Build the task force summary view.
     *
     * @return The Task force summary view.
     */
    public TaskForceAirSummaryView build() {
        titledPane.setId("taskforce-title-pane");

        Node squadronSummaryNode = squadronSummary.build("Squadron Summary");

        leftVBox.getChildren().addAll(titledPane, imageView, squadronSummaryNode);
        leftVBox.setId("taskforce-summary-vbox");

        return this;
    }

    /**
     * Bind the task force summary view to the task force view model.
     *
     * @param viewModel The task force view model.
     * @return The node containing the task force view.
     */
    public Node bind(final TaskForceAirViewModel viewModel) {
        titledPane.textProperty().bind(viewModel.getNameAndTitle());
        imageView.imageProperty().bind(viewModel.getImage());
        squadronSummary.bind(viewModel.getSquadronCounts());

        return leftVBox;
    }
}
