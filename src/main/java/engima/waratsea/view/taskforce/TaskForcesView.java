package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.viewmodel.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

public class TaskForcesView {

    private final Provider<TaskForceView> taskForceViewProvider;

    private final TabPane taskforcePane = new TabPane();

    /**
     * Constructor called by guice.
     *
     * @param taskForceViewProvider The task force view provider.
     */
    @Inject
    public TaskForcesView(final Provider<TaskForceView> taskForceViewProvider) {
        this.taskForceViewProvider = taskForceViewProvider;
    }

    /**
     * Show the task force details.
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
        return taskForceViewProvider
                .get()
                .build(taskForce);
    }
}
