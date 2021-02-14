package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

/**
 * This represents the task forces view as shown in the task force dialog box.
 * This class basically contains all of the individual task force views - one for each task force.
 */
public class TaskForcesNavalView {

    private final Provider<TaskForceNavalView> taskForceViewProvider;

    private final TabPane taskforcePane = new TabPane();

    /**
     * Constructor called by guice.
     *
     * @param taskForceViewProvider The task force view provider.
     */
    @Inject
    public TaskForcesNavalView(final Provider<TaskForceNavalView> taskForceViewProvider) {
        this.taskForceViewProvider = taskForceViewProvider;
    }

    /**
     * Show the task force details.
     *
     * @param taskForces A list of task forces.
     * @return A node containing the airbase details.
     */
    public Node build(final List<TaskForceNavalViewModel> taskForces) {

        taskforcePane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        taskForces
                .stream()
                .sorted()
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
    private Tab createTaskForceTab(final TaskForceNavalViewModel taskForce) {
        return taskForceViewProvider
                .get()
                .build(taskForce);
    }
}
