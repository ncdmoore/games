package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.enemy.views.taskForce.TaskForceView;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class TargetEnemyTaskForce implements Target {

    private Game game;

    @Getter
    private String name;

    private Side side;

    private TaskForceView taskForceView;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param game The game.
     */
    @Inject
    public TargetEnemyTaskForce(@Assisted final TargetData data,
                                          final Game game) {
        this.game = game;

        name = data.getName();
        side = data.getSide();
    }

    /**
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getLocation() {
        return Optional
                .ofNullable(taskForceView)
                .orElseGet(this::getTaskForceViewView)
                .getLocation();
    }

    /**
     * Get the target persistent data.
     *
     * @return The target's persistent data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setName(name);
        data.setType(TargetType.ENEMY_TASK_FORCE);
        data.setSide(side);
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }

    /**
     * Get the port view for this target.
     *
     * @return This target's port view.
     */
    private TaskForceView getTaskForceViewView() {
        taskForceView = game.getPlayer(side)
                .getEnemyTaskForceMap()
                .get(name);

        if (taskForceView == null) {
            log.error("Cannot find task force view: '{}' for side: '{}'", name, side);
        }

        return taskForceView;
    }
}
