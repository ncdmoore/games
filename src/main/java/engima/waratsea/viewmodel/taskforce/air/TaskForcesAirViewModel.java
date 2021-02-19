package engima.waratsea.viewmodel.taskforce.air;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceGroup;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.model.taskForce.patrol.PatrolGroupDAO;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the view model of a given side's task forces regarding air operations.
 */
public class TaskForcesAirViewModel {
    private final Provider<TaskForceAirViewModel> taskForceAirViewModelProvider;
    private final Provider<PatrolGroups> patrolGroupsProvider;
    private final Provider<TaskForceGroup> taskForceGroupProvider;

    @Getter private List<TaskForceAirViewModel> taskForceViewModels;

    private List<TaskForce> taskForces;
    private final PatrolGroupDAO patrolGroupDAO;

    //This is the total squadrons on patrol of all airbases within a given task force.
    @Getter private final Map<PatrolType, ListProperty<SquadronViewModel>> totalSquadronsOnPatrol =
            Map.of(PatrolType.ASW, new SimpleListProperty<>(),
                    PatrolType.CAP, new SimpleListProperty<>(),
                    PatrolType.SEARCH, new SimpleListProperty<>());
    /**
     * Called by guice.
     *
     * @param taskForceAirViewModelProvider Provides task force air view models.
     * @param patrolGroupsProvider Provides patrol groups.
     * @param taskForceGroupProvider Provides task force groups.
     * @param patrolGroupDAO Provides patrol groups.
     */
    @Inject
    public TaskForcesAirViewModel(final Provider<TaskForceAirViewModel> taskForceAirViewModelProvider,
                                  final Provider<PatrolGroups> patrolGroupsProvider,
                                  final Provider<TaskForceGroup> taskForceGroupProvider,
                                  final PatrolGroupDAO patrolGroupDAO) {
        this.taskForceAirViewModelProvider = taskForceAirViewModelProvider;
        this.patrolGroupsProvider = patrolGroupsProvider;
        this.taskForceGroupProvider = taskForceGroupProvider;
        this.patrolGroupDAO = patrolGroupDAO;
    }

    /**
     * Set the task forces model.
     *
     * @param newTaskForces The task forces.
     * @return This task forces air view model.
     */
    public TaskForcesAirViewModel setModel(final List<TaskForce> newTaskForces) {
        taskForces = newTaskForces;

        taskForceViewModels = newTaskForces
                .stream()
                .map(this::buildTaskForceAirViewModel)
                .collect(Collectors.toList());

        PatrolType
                .stream()
                .forEach(this::setPatrols);

        return this;
    }

    /**
     * Add a squadron to the task force groups patrol.
     *
     * @param patrolType The patrol type.
     * @param squadronViewModel The squadron added.
     */
    public void addToPatrol(final PatrolType patrolType, final SquadronViewModel squadronViewModel) {
        totalSquadronsOnPatrol
                .get(patrolType)
                .add(squadronViewModel);
    }

    /**
     * Remove a squadron from the task force groups patrol.
     *
     * @param patrolType The patrol type.
     * @param squadronViewModel The squadron removed.
     */
    public void removeFromPatrol(final PatrolType patrolType, final SquadronViewModel squadronViewModel) {
        totalSquadronsOnPatrol
                .get(patrolType)
                .remove(squadronViewModel);
    }

    /**
     * Determine if this is a task force group; i.e., more than one task force, or just a single task
     * force.
     *
     * @return True if this represents a task force group. False, if this represents a single task force.
     */
    public boolean isTaskForceGroup() {
        return taskForceViewModels.size() > 1;
    }

    /**
     * Save the task forces air view model to the model.
     */
    public void save() {
        taskForceViewModels.forEach(TaskForceAirViewModel::save);
    }

    /**
     * Get this collection of airbases patrol group for the given type of patrol.
     *
     * @param patrolType The patrol type.
     * @return This collection of airbases patrol group.
     */
    public PatrolGroup getPatrolGroup(final PatrolType patrolType) {
        PatrolGroupData data = new PatrolGroupData();

        List<Squadron> totalOnPatrol = totalSquadronsOnPatrol
                .get(patrolType)
                .getValue()
                .stream()
                .map(SquadronViewModel::get)
                .collect(Collectors.toList());

        // Fake a task force group.
        TaskForceGroup taskForceGroup = taskForceGroupProvider.get();
        taskForceGroup.build(taskForces);

        // Fake a patrol group so classes that need the patrol group can
        // have access to the task force group, which is this patrol group's
        // airbase group.
        PatrolGroups patrolGroups = patrolGroupsProvider.get();
        patrolGroups.setAirbaseGroup(taskForceGroup);

        data.setType(patrolType);
        data.setSquadrons(totalOnPatrol);
        data.setGroups(patrolGroups);

        return patrolGroupDAO.load(data);
    }

    /**
     * Build an individual task force air view model.
     *
     * @param taskForce The underlying task force.
     * @return A task force air view model.
     */
    private TaskForceAirViewModel buildTaskForceAirViewModel(final TaskForce taskForce) {
        return taskForceAirViewModelProvider
                .get()
                .setTaskForcesAirViewModel(this)
                .setModel(taskForce);
    }

    /**
     * Set the given patrol type for this task force group.
     *
     * @param patrolType The patrol type.
     */
    private void setPatrols(final PatrolType patrolType) {
        List<SquadronViewModel> total = taskForceViewModels
                .stream()
                .flatMap(taskForceAirViewModel -> taskForceAirViewModel
                        .getAirbases()
                        .stream())
                .flatMap(a -> a.getPatrolViewModels()
                        .get(patrolType)
                        .getAssignedAllNations()
                        .stream())
                .collect(Collectors.toList());

        totalSquadronsOnPatrol.get(patrolType).setValue(FXCollections.observableArrayList(total));
    }
}
