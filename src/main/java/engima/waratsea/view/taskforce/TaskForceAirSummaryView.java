package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.InfoPane;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Task force air summary view.
 *
 * CSS Styles used.
 *
 *  - spacing-5
 */
public class TaskForceAirSummaryView {
    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    private final InfoPane squadronSummary;

    private final ViewProps props;

    @Inject
    public TaskForceAirSummaryView(final Provider<InfoPane> infoProvider,
                                   final ViewProps props) {
        squadronSummary = infoProvider.get();
        this.props = props;
    }

    /**
     * Build the task force summary view.
     *
     * @return The Task force summary view.
     */
    public TaskForceAirSummaryView build() {
        titledPane.getStyleClass().add("title-pane-non-collapsible");

        Node squadronSummaryNode = squadronSummary
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .build("Squadron Summary");

        leftVBox.getChildren().addAll(titledPane, imageView, squadronSummaryNode);
        leftVBox.getStyleClass().add("spacing-5");

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
        squadronSummary.bindIntegers(viewModel.getSquadronCounts());

        return leftVBox;
    }
}
