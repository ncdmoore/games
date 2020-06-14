package engima.waratsea.view.taskforce;

import engima.waratsea.viewmodel.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

public class TaskForcesView {

    private final TabPane taskforcePane = new TabPane();

    /**
     * Show the airbase details.
     *
     * @param taskForces A list of task forces.
     * @return A node containing the airbase details.
     */
    public Node build(final List<TaskForceViewModel> taskForces) {

        taskforcePane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        taskForces
                .stream()
    //            .sorted()
                .map(this::createTaskForceTab)
                .forEach(tab -> taskforcePane.getTabs().add(tab));

        return taskforcePane;
    }

    /**
     * Build the task force tab.
     *
     * @param taskForce The task force.
     * @return The task force's tab.
     */
    private Tab createTaskForceTab(final TaskForceViewModel taskForce) {
        Tab tab = new Tab();
        tab.setText(taskForce.getName().getValue() + " " + taskForce.getTitle().getValue());

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

        List<String> operations = List.of("Naval Operations", "Air Operations");

        operations
                .stream()
                .map(this::createOperationTab)
                .forEach(tab -> operationsTabPane.getTabs().add(tab));

        return operationsTabPane;
    }

    /**
     * Create the operation tab.
     *
     * @param operation The operation.
     * @return A tab for the given operation.
     */
    private Tab createOperationTab(final String operation) {
        Tab tab = new Tab();
        tab.setText(operation);
        return tab;
    }
}
