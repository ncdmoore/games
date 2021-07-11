package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.InfoPane;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Task force naval summary view.
 *
 * CSS Styles used.
 *
 * - spacing-5
 */
public class TaskForceNavalSummaryView {
    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    private final InfoPane shipSummary;

    private final ViewProps props;

    @Inject
    public TaskForceNavalSummaryView(final Provider<InfoPane> infoProvider,
                                     final ViewProps props) {
        shipSummary = infoProvider.get();
        this.props = props;
    }

    /**
     * Build the task force summary view.
     *
     * @return The Task force summary view.
     */
    public TaskForceNavalSummaryView build() {
        titledPane.getStyleClass().add("title-pane-non-collapsible");

        Node shipSummaryNode = shipSummary
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .build("Ship Summary");
        shipSummaryNode.getStyleClass().add("title-pane-non-collapsible");

        leftVBox.getChildren().addAll(titledPane, imageView, shipSummaryNode);
        leftVBox.getStyleClass().add("spacing-5");

        return this;
    }

    /**
     * Bind the task force summary view to the task force view model.
     *
     * @param viewModel The task force view model.
     * @return The node containing the task force view.
     */
    public Node bind(final TaskForceNavalViewModel viewModel) {
        titledPane.textProperty().bind(viewModel.getNameAndTitle());
        imageView.imageProperty().bind(viewModel.getImage());
        shipSummary.bindIntegers(viewModel.getShipCounts());

        return leftVBox;
    }
}
