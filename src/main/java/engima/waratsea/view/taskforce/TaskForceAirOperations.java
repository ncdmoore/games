package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.presenter.airfield.AirbasePresenter;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Task force air operations view. Used by task force air operations dialog box.
 *
 * CSS Styles used.
 *
 * - airfield-main-pane
 */
public class TaskForceAirOperations {
    private final AirbasePresenter airbasePresenter;

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

        ChoiceBox<AirbaseViewModel> airbases = new ChoiceBox<>();

        airbases.itemsProperty().bind(taskForceVM.getAirbases());

        airbases
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ov, nv) -> airbaseSelected(nv));

        airbases.getSelectionModel().selectFirst();

        VBox airbaseSelectionVBox = new VBox(label, airbases);

        VBox vBox = new VBox(airbaseSelectionVBox, airbaseNode);

        vBox.setId("airfield-main-pane");

        return vBox;
    }

    private Node buildSummary() {
        return summaryView
                .build()
                .bind(viewModel);
    }

    private void airbaseSelected(final AirbaseViewModel airbase) {
        Node contents = airbasePresenter.build(airbase, false);
        airbaseNode.getChildren().clear();
        airbaseNode.getChildren().add(contents);
    }

}
