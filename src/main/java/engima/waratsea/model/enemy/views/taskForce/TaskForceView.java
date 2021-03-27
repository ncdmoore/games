package engima.waratsea.model.enemy.views.taskForce;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.enemy.views.taskForce.data.TaskForceViewData;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class TaskForceView implements PersistentData<TaskForceViewData> {
    @Getter
    @Setter
    private TaskForce enemyTaskForce;

    @Getter
    @Setter
    private boolean spotted;

    /**
     * Constructor called by guice.
     *
     * @param data The task force view data read in from JSON or created from mission data.
     */
    @Inject
    public TaskForceView(@Assisted final TaskForceViewData data) {
        this.enemyTaskForce = data.getTaskForce();
        this.spotted = false;
    }

    /**
     * Get the task force's name.
     *
     * @return The task force's name.
     */
    public String getName() {
        return enemyTaskForce.getName();
    }

    /**
     * Get the task force's title.
     *
     * @return The task force's title.
     */
    public String getTitle() {
        return enemyTaskForce.getTitle();
    }

    /**
     * Get the task force's location.
     *
     * @return The task force's map reference: location.
     */
    public String getReference() {
        return enemyTaskForce.getReference();
    }

    /**
     * Get the task force's game grid.
     *
     * @return The task force's game grid.
     */
    public Optional<GameGrid> getGrid() {
        return enemyTaskForce.getGrid();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public TaskForceViewData getData() {
        TaskForceViewData data = new TaskForceViewData();
        data.setName(enemyTaskForce.getName());
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }
}
