package engima.waratsea.model.taskForce;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a group of task forces. This occurs when more than one task force occupies the same
 * game grid. This class is needed to properly model the patrols that orginate from a 'group' of task forces
 * that occupy the same game grid.
 *
 * When task forces occupy the same game grid, it is the aggregate of their squadrons on patrol that determine
 * how the patrol performs; i.e., all task forces squadrons contribute to a single patrol. This is modeled with
 * this class's patrol group.
 *
 * This class is essentially just an adaptor between a group of task forces (list) and a patrol group/airbase group.
 *
 * This is just a transient class. It does not live in the model other than to adapt a list of task forces to
 * an airbase group. Thus, there is no code to keep it updated with the current list of task forces and such.
 */
public class TaskForceGroup implements AirbaseGroup {
    @Getter private List<Airbase> airbases;
    @Getter private final PatrolGroups patrolGroups;

    @Inject
    public TaskForceGroup(final PatrolGroups patrolGroups) {
        this.patrolGroups = patrolGroups;
    }

    /**
     * Set the task forces for this task forces group.
     *
     * @param taskForces The task forces that make up this task forces group.
     * @return This task force group.
     */
    public TaskForceGroup build(final List<TaskForce> taskForces) {
        airbases = taskForces
                .stream()
                .flatMap(taskForce -> taskForce
                        .getAirbases()
                        .stream())
                .collect(Collectors.toList());

        patrolGroups.build(this);

        // Override the default home group. This is necessary so that the task forces marker may be related to
        // the task force's patrol group. Task force groups all occupy the same game map location/grid. Thus,
        // any task force in this task force group may serve as the home group.
        patrolGroups.setHomeGroup(taskForces.get(0));

        return this;
    }
}
