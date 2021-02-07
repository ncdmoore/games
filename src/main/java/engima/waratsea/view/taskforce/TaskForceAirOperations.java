package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.presenter.airfield.AirbasePresenter;
import engima.waratsea.view.airfield.AirfieldView;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

public class TaskForceAirOperations {
    private TaskForceViewModel taskForceViewModel;
    private AirbasePresenter airbasePresenter;
    private final Provider<AirfieldView> airfieldProvider;

    private final VBox airbaseNode = new VBox();

    @Inject
    public TaskForceAirOperations(final AirbasePresenter airbasePresenter,
                                  final Provider<AirfieldView> airfieldProvider) {
        this.airbasePresenter = airbasePresenter;
        this.airfieldProvider = airfieldProvider;
    }

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Tab createOperationTab(final TaskForceViewModel taskForceVM) {
        taskForceViewModel = taskForceVM;

        Tab tab = new Tab();
        tab.setText("Air Operations");

        ChoiceBox<AirbaseViewModel> airbases = new ChoiceBox<>();

        airbases.itemsProperty().bind(taskForceVM.getAirbases());

        airbases
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ov, nv) -> airbaseSelected(nv));

        airbases.getSelectionModel().selectFirst();

        Label label = new Label("Ships with aircraft:");
        VBox vBox = new VBox(label, airbases, airbaseNode);

        tab.setContent(vBox);

        return tab;
    }

    private void airbaseSelected(final AirbaseViewModel airbase) {
        Node contents = airbasePresenter.build(airbase, false);
        airbaseNode.getChildren().clear();
        airbaseNode.getChildren().add(contents);
    }

}
