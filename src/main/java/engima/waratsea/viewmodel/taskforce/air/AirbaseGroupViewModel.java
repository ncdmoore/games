package engima.waratsea.viewmodel.taskforce.air;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.model.taskForce.patrol.PatrolGroupDAO;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.airfield.RealAirbaseViewModel;
import engima.waratsea.viewmodel.airfield.VirtualAirbaseViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Represents a collection of airbases. This is used by the task forces as task forces
 * may have several ships that may act as an airbase. This class represents a collection
 * of all the ships that may act as airbases.
 */
public class AirbaseGroupViewModel {
    private final Provider<RealAirbaseViewModel> realAirbaseViewModelProvider;
    private final Provider<VirtualAirbaseViewModel> virtualAirbaseViewModelProvider;
    private final Provider<PatrolGroups> patrolGroupsProvider;

    private final ObjectProperty<TaskForce> taskForce = new SimpleObjectProperty<>();

    @Getter private final ListProperty<RealAirbaseViewModel> realAirbases = new SimpleListProperty<>();
    @Getter private final ObjectProperty<VirtualAirbaseViewModel> virtualAirbase = new SimpleObjectProperty<>();

    //This is the total squadrons on patrol of all airbases within a given task force.
    @Getter private final Map<PatrolType, ListProperty<SquadronViewModel>> totalSquadronsOnPatrol =
            Map.of(PatrolType.ASW, new SimpleListProperty<>(),
                    PatrolType.CAP, new SimpleListProperty<>(),
                    PatrolType.SEARCH, new SimpleListProperty<>());

    @Setter private TaskForceAirViewModel taskForceAirViewModel;   // Parent view model.

    private final PatrolGroupDAO patrolGroupDAO;

    /**
     * Constructor called by guice.
     *
     * @param realAirbaseViewModelProvider Provides real airbase view models.
     * @param virtualAirbaseViewModelProvider Provides virtual airbase view models.
     * @param patrolGroupsProvider Provides patrol groups.
     * @param patrolGroupDAO Provides patrol groups.
     */
    @Inject
    public AirbaseGroupViewModel(final Provider<RealAirbaseViewModel> realAirbaseViewModelProvider,
                                 final Provider<VirtualAirbaseViewModel> virtualAirbaseViewModelProvider,
                                 final Provider<PatrolGroups> patrolGroupsProvider,
                                 final PatrolGroupDAO patrolGroupDAO) {
        this.realAirbaseViewModelProvider = realAirbaseViewModelProvider;
        this.virtualAirbaseViewModelProvider = virtualAirbaseViewModelProvider;
        this.patrolGroupsProvider = patrolGroupsProvider;
        this.patrolGroupDAO = patrolGroupDAO;

        bindAirbases();
    }

    /**
     * Set the backing task force model.
     *
     * @param newTaskForce The new task force model.
     */
    public void setModel(final TaskForce newTaskForce) {
        taskForce.setValue(newTaskForce);

        // The patrols are not bound as they are modified when the additional squadrons are placed on patrol.
        // The airbases are guaranteed to be set as they are bound to the task force which was just set.
        // Thus, we can now set the patrols.
        setPatrols();
    }

    /**
     * Get all of the airbases including the virtual airbase.
     *
     * @return All of the airbases.
     */
    public ListProperty<AirbaseViewModel> getAirbases() {
        List<AirbaseViewModel> allAirBases = realAirbases
                .getValue()
                .stream()
                .map(a -> (AirbaseViewModel) a)
                .collect(Collectors.toList());

        allAirBases.add(virtualAirbase.getValue());

        return new SimpleListProperty<>(FXCollections.observableArrayList(allAirBases));
    }

    /**
     *
     * Save the task force's airbases data to the model.
     */
    public void save() {
        realAirbases.forEach(RealAirbaseViewModel::save);
    }

    /**
     * Add a squadron to the air base group's patrol.
     *
     * @param patrolType        The type of patrol.
     * @param squadronViewModel The squadron view model added.
     */
    public void addToPatrol(final PatrolType patrolType, final SquadronViewModel squadronViewModel) {
        totalSquadronsOnPatrol
                .get(patrolType)
                .add(squadronViewModel);
    }

    /**
     * Remove a squadron from the air base group's patrol.
     *
     * @param patrolType        The type of patrol.
     * @param squadronViewModel The squadron view model removed.
     */
    public void removeFromPatrol(final PatrolType patrolType, final SquadronViewModel squadronViewModel) {
        totalSquadronsOnPatrol
                .get(patrolType)
                .remove(squadronViewModel);
    }

    /**
     * Get the patrol group of the given type for this collection of airbases.
     *
     * @param patrolType The patrol type.
     * @return The patrol group for this collection of airbases for the given type of patrol.
     */
    public PatrolGroup getPatrolGroup(final PatrolType patrolType) {
        PatrolGroupData data = new PatrolGroupData();

        List<Squadron> totalOnPatrol = totalSquadronsOnPatrol
                .get(patrolType)
                .getValue()
                .stream()
                .map(SquadronViewModel::get)
                .collect(Collectors.toList());

        // Fake a patrol group so that classes that need access to the patrol group's
        // airbase group can have access to it. The patrol group's airbase group
        // is this classes parent task force.
        PatrolGroups patrolGroups = patrolGroupsProvider.get();
        patrolGroups.setAirbaseGroup(taskForceAirViewModel.getTaskForce().getValue());

        data.setType(patrolType);
        data.setSquadrons(totalOnPatrol);
        data.setGroups(patrolGroups);

        return patrolGroupDAO.load(data);    }

    /**
     * bind the airbases.
     */
    private void bindAirbases() {
        bindRealAirbases();
        bindVirtualAirbase();
    }

    private void bindRealAirbases() {
        Callable<ObservableList<RealAirbaseViewModel>> bindingFunction = () -> Optional
                .ofNullable(taskForce.getValue())
                .map(this::getRealViewModels)
                .map(FXCollections::observableArrayList)
                .orElse(FXCollections.emptyObservableList());

        realAirbases.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
    }

    private void bindVirtualAirbase() {
        Callable<VirtualAirbaseViewModel> bindingFunction = () -> Optional
                .ofNullable(taskForce.getValue())
                .map(this::getVirtualViewModel)
                .orElse(null);

        virtualAirbase.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
    }

    /**
     * Get a the airbase view models from the given task force.
     *
     * @param force A task force.
     * @return The corresponding airbase view models of the given task force.
     */
    private List<RealAirbaseViewModel> getRealViewModels(final TaskForce force) {
        return force
                .getRealAirbases()
                .stream()
                .filter(Airbase::areSquadronsPresent)
                .map(airbase -> realAirbaseViewModelProvider
                        .get()
                        .setModel(airbase)
                        .setGroup(this))
                .collect(Collectors.toList());
    }

    private VirtualAirbaseViewModel getVirtualViewModel(final TaskForce force) {
        return virtualAirbaseViewModelProvider
                .get()
                .setModel(force.getVirtualAirbase())
                .setGroup(this);
    }

    /**
     * Set the patrols (all types) for this collection of airbases.
     */
    private void setPatrols() {
        PatrolType
                .stream()
                .forEach(this::setPatrol);
    }

    /**
     * Set the patrol for the given patrol type.
     *
     * @param patrolType The patrol type.
     */
    private void setPatrol(final PatrolType patrolType) {
        totalSquadronsOnPatrol
                .get(patrolType)
                .setValue(getAllSquadronsOnPatrol(patrolType));
    }

    /**
     * Get all of the squadrons on patrol of the given type from all the airbases.
     *
     * @param patrolType The patrol type,
     * @return The squadrons on the given patrol type from all airbases.
     */
    private ObservableList<SquadronViewModel> getAllSquadronsOnPatrol(final PatrolType patrolType) {
        return realAirbases
                .stream()
                .map(RealAirbaseViewModel::getPatrolViewModels)
                .map(m -> m.get(patrolType))
                .flatMap(patrolVM -> patrolVM.getAssignedAllNations().stream())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }


}
