package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Represents an individual task force view.
 */
public class TaskForceView {

    private TaskForceViewModel taskForceViewModel;

    private final TaskForceNavalOperations taskForceNavalOperations;
    private final TaskForceAirOperations taskForceAirOperations;

    /**
     * The constructor called by guice.
     *
     * @param taskForceAirOperations Task Force Air operations view.
     * @param taskForceNavalOperations Task Force Naval operations view.
     */
    @Inject
    public TaskForceView(final TaskForceAirOperations taskForceAirOperations,
                         final TaskForceNavalOperations taskForceNavalOperations) {
        this.taskForceAirOperations = taskForceAirOperations;
        this.taskForceNavalOperations = taskForceNavalOperations;
    }

    /**
     * Build the task force tab.
     *
     * @param taskForce The task force.
     * @return The task force's tab.
     */
    public Tab build(final TaskForceViewModel taskForce) {
        taskForceViewModel = taskForce;

        Tab tab = new Tab();
        tab.textProperty().bind(taskForce.getNameAndTitle());
        tab.setContent(createOperationTabs());

        return tab;
    }

    /**
     * Create the naval and air operation tabs.
     *
     * @return The tab pane containing the naval and air operation tabs.
     */
    private TabPane createOperationTabs() {
        TabPane operationsTabPane = new TabPane();
        operationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab navalOperations = taskForceNavalOperations.createOperationTab(taskForceViewModel);
        Tab airOperations = taskForceAirOperations.createOperationTab(taskForceViewModel);

        operationsTabPane.getTabs().addAll(navalOperations, airOperations);

        return operationsTabPane;
    }
}
