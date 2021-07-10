package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.presenter.airfield.AirbasePresenter;
import engima.waratsea.viewmodel.airfield.RealAirbaseViewModel;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * Task force air operations view. Used by task force air operations dialog box.
 *
 * CSS Styles used.
 *
 * - airfield-main-pane
 */
public class TaskForceAirOperations {
    private final AirbasePresenter airbasePresenter;

    @Getter private final ChoiceBox<RealAirbaseViewModel> airbases = new ChoiceBox<>();

    private final TaskForceAirSummaryView summaryView;

    private final VBox airbaseNode = new VBox();

    private TaskForceAirViewModel viewModel;

    @Inject
    public TaskForceAirOperations(final AirbasePresenter airbasePresenter,
                                  final TaskForceAirSummaryView summaryView) {
        this.airbasePresenter = airbasePresenter;
        this.summaryView = summaryView;
    }

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Node build(final TaskForceAirViewModel taskForceVM) {
        viewModel = taskForceVM;

        Label label = new Label("Ships with aircraft:");

        airbases.itemsProperty().bind(taskForceVM.getRealAirbases());

        VBox airbaseSelectionVBox = new VBox(label, airbases);

        VBox vBox = new VBox(airbaseSelectionVBox, airbaseNode);

        vBox.setId("airfield-main-pane");

        return vBox;
    }

    /**
     * An airbase has been selected.
     *
     * @param airbase The selected airbase view model.
     */
    public void airbaseSelected(final RealAirbaseViewModel airbase) {
        Node contents = airbasePresenter.build(airbase, false);
        airbaseNode.getChildren().clear();
        airbaseNode.getChildren().add(contents);
    }

    private Node buildSummary() {
        return summaryView
                .build()
                .bind(viewModel);
    }
}
