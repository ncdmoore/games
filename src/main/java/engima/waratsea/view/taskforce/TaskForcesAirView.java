package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

/**
 * This represents the task forces view as shown in the task force dialog box.
 * This class basically contains all of the individual task force views - one for each task force.
 */
public class TaskForcesAirView {

    private final Provider<TaskForceAirView> taskForceViewProvider;

    private final TabPane taskforcePane = new TabPane();

    /**
     * Constructor called by guice.
     *
     * @param taskForceViewProvider The task force view provider.
     */
    @Inject
    public TaskForcesAirView(final Provider<TaskForceAirView> taskForceViewProvider) {
        this.taskForceViewProvider = taskForceViewProvider;
    }

    /**
     * Show the task force details.
     *
     * @param taskForces A list of task forces.
     * @return A node containing the airbase details.
     */
    public Node build(final List<TaskForceAirViewModel> taskForces) {

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
    private Tab createTaskForceTab(final TaskForceAirViewModel taskForce) {
        return taskForceViewProvider
                .get()
                .build(taskForce);
    }
}
