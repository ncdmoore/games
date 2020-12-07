package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * Represents the naval operations tab of a given task force view.
 */
public class TaskForceNavalOperations {

    private TaskForceViewModel viewModel;

    private final TaskForceSummaryView summaryView;


    @Getter private final ChoiceBox<SeaMissionType> missionType = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param summaryView The task force summary view.
     */
    @Inject
    public TaskForceNavalOperations(final ViewProps props,
                                    final TaskForceSummaryView summaryView) {

        this.summaryView = summaryView;

        missionType.setMinWidth(props.getInt("mission.type.list.width"));
    }

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Tab createOperationTab(final TaskForceViewModel taskForceVM) {
        viewModel = taskForceVM;

        Tab tab = new Tab();
        tab.setText("Naval Operations");

        Node summary = buildSummary();

        //Node missionNode = buildMissionNode();

        HBox hBox = new HBox(summary);
        hBox.setId("main-pane");

        tab.setContent(hBox);

        return tab;
    }


    private Node buildSummary() {
        return summaryView
                .build()
                .bind(viewModel);
    }

    /**
     * Build the mission node.
     *
     * @return A node containing the mission selection controls.
     */
    private Node buildMissionNode() {
        Label missionLabel = new Label("Select Mission Type:");

        missionType.getItems().addAll(viewModel.getMissionTypes().getValue());
        missionType.getSelectionModel().select(viewModel.getMission().getValue());

        return new VBox(missionLabel, missionType);
    }
}
