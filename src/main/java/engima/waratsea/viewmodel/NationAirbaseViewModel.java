package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Each nation has its on unique airfield view model. Thus, all of the property values of this view model are for
 * a given nation.
 */
@Slf4j
public class NationAirbaseViewModel {
    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty typeTitle = new SimpleStringProperty();
    @Getter private final StringProperty regionTitle = new SimpleStringProperty();

    @Getter private final StringProperty maxCapacity = new SimpleStringProperty();
    @Getter private final StringProperty current = new SimpleStringProperty();
    @Getter private final StringProperty antiAir = new SimpleStringProperty();

    @Getter private final StringProperty regionMaximum = new SimpleStringProperty();
    @Getter private final StringProperty regionMinimum = new SimpleStringProperty();
    @Getter private final StringProperty regionCurrent = new SimpleStringProperty();

    @Getter private final ObjectProperty<ObservableList<Squadron>> squadrons = new SimpleObjectProperty<>(FXCollections.emptyObservableList());

    @Getter private final Map<SquadronViewType, ObjectProperty<ObservableList<Squadron>>> readySquadrons = new HashMap<>();
    @Getter private final ObjectProperty<ObservableList<Squadron>> totalReadySquadrons = new SimpleObjectProperty<>(FXCollections.emptyObservableList());

    @Getter private final Map<SquadronViewType, ObjectProperty<ObservableList<Squadron>>> allSquadrons = new HashMap<>();

    @Getter private final ObjectProperty<ObservableList<AirMissionViewModel>> missions = new SimpleObjectProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<PatrolType, PatrolViewModel> patrolViewModels;
    private List<AirMissionViewModel> missionViewModels;

    @Getter private final Map<String, IntegerProperty> squadronCounts = new HashMap<>();     // Per nation.
    @Getter private final Map<String, IntegerProperty> missionCounts = new HashMap<>();      // Per nation.
    @Getter private final Map<String, IntegerProperty> patrolCounts = new HashMap<>();       // Per nation.
    @Getter private final Map<String, IntegerProperty> readyCounts = new HashMap<>();        // Per nation.

    @Getter private final BooleanProperty noSquadronsReady = new SimpleBooleanProperty(true); // Per nation
    @Getter private final BooleanProperty noMissionsExist = new SimpleBooleanProperty(true); // Per nation.

    @Getter private final ObjectProperty<ObservableList<Aircraft>> aircraftModels = new SimpleObjectProperty<>();       // List of all aircraft models present at this airbase.
    @Getter private final ObjectProperty<Aircraft> selectedAircraft = new SimpleObjectProperty<>();                             // The selected aircraft model.
    @Getter private final ObjectProperty<ObservableList<SquadronConfig>> squadronConfigs = new SimpleObjectProperty<>(FXCollections.emptyObservableList());
    @Getter private final ObjectProperty<SquadronConfig> selectedConfig = new SimpleObjectProperty<>();

    @Getter private AirbaseViewModel airbaseViewModel;

    @Getter private Airbase airbase;
    @Getter private Nation nation;

    /**
     * Constructor called by guice.
     **/
    @Inject
    public NationAirbaseViewModel() {
        Stream
                .of(SquadronViewType.values())
                .forEach(this::initializeSquadrons);

        Stream
                .of(AirMissionType.values())
                .forEach(this::initializeMission);

        Stream
                .of(PatrolType.values())
                .forEach(this::initializePatrol);
    }

    /**
     * Set the model.
     *
     * @param selectedNation The nation: BRITISH, ITALIAN, etc...
     * @param selectedAirbase The airbase.
     * @return This airbase view model.
     */
    public NationAirbaseViewModel setModel(final Nation selectedNation, final AirbaseViewModel selectedAirbase) {
        airbaseViewModel = selectedAirbase;
        nation = selectedNation;
        airbase = selectedAirbase.getAirbase();

        title.set(airbase.getTitle());
        typeTitle.set(airbase.getAirfieldType().getTitle());
        regionTitle.set(airbase.getRegionTitle());

        maxCapacity.set(airbase.getMaxCapacity() + "");
        current.set(airbase.getCurrentSteps() + "");
        antiAir.set(airbase.getAntiAirRating() + "");

        regionMaximum.set(airbase.getRegion(nation).getMaxSteps() + "");
        regionMinimum.set(airbase.getRegion(nation).getMinSteps() + "");
        regionCurrent.set(airbase.getRegion(nation).getCurrentSteps() + "");

        squadrons.set(FXCollections.observableArrayList(airbase.getSquadrons(nation)));

        setReadySquadrons();
        setAllSquadrons();
        setAircraftModels();

        return this;
    }

    /**
     * Set the airbase mission view models.
     *
     * @param airbaseMissions The airbase mission view models.
     */
    public void setMissionViewModels(final List<AirMissionViewModel> airbaseMissions) {
        missionViewModels = airbaseMissions;
        missions.set(FXCollections.observableArrayList(missionViewModels));

        String ids = missionViewModels.stream().map(AirMissionViewModel::getId).map(i -> Integer.toString(i)).collect(Collectors.joining(","));

        log.debug("Initialize mission view models with ids: '{}'", ids);

        setMissionCounts();
    }

    /**
     * Set the airbase patrol view models.
     *
     * @param airbasePatrols The airbase patrol view models.
     */
    public void setPatrolViewModels(final Map<PatrolType, PatrolViewModel> airbasePatrols) {
        patrolViewModels = airbasePatrols;

        setPatrolCounts();
    }

    /**
     * Get the squadron map for the given squadron state.
     *
     * @param state The squadron state.
     * @return A map of squadron view type to list of squadrons at the given state.
     */
    public Map<SquadronViewType, ObjectProperty<ObservableList<Squadron>>> getSquadronMap(final SquadronState state) {
        return (state == null) ? allSquadrons : readySquadrons;
    }

    /**
     * Get the squadrons assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @return The squadrons assigned to the given patrol type.
     */
    public ObjectProperty<ObservableList<Squadron>> getAssignedPatrolSquadrons(final PatrolType type) {
        return patrolViewModels.get(type).getAssigned().get(nation);
    }

    /**
     * Get the squadrons available for the given patrol type.
     *
     * @param type The patrol type.
     * @return The squadrons available for the given patrol type.
     */
    public ObjectProperty<ObservableList<Squadron>> getAvailablePatrolSquadrons(final PatrolType type) {
        return patrolViewModels.get(type).getAvailable().get(nation);
    }

    /**
     * Indicates if any squadron are assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @return True if squadrons are assigned to the given patrol type. False otherwise.
     */
    public BooleanProperty getAssignedPatrolExists(final PatrolType type) {
        return patrolViewModels.get(type).getAssignedExists().get(nation);
    }

    /**
     * Indicates if any squadrons are available for the given patrol type.
     *
     * @param type The patrol type.
     * @return True if squadrons are available for the given patrol type. False otherwise.
     */
    public BooleanProperty getAvailablePatrolExists(final PatrolType type) {
        return patrolViewModels.get(type).getAvailableExists().get(nation);
    }

    /**
     * Add a mission to this view model.
     *
     * @param viewModel The mission view model.
     */
    public void addMission(final AirMissionViewModel viewModel) {
        missionViewModels.add(viewModel);
        missions.set(FXCollections.observableArrayList(missionViewModels));

        String ids = missionViewModels
                .stream()
                .map(AirMissionViewModel::getId)
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(","));

        log.debug("Add mission with id: '{}'", viewModel.getId());
        log.debug("All missions view models: '{}'", ids);

        removeFromReady(viewModel);            // Remove the mission squadrons from the ready list.
        airbaseViewModel.updateTotalMissions();
    }

    /**
     * Edit a mission within this view model. The old air mission view model is removed
     * from the list of mission view models. The updated air mission view model is then
     * added in. This is necessary to correctly reflect the current air missions.
     *
     * @param viewModel The mission view model.
     */
    public void editMission(final AirMissionViewModel viewModel) {
        // The unique mission id is used to find the mission view model.
        Optional<AirMissionViewModel> oldViewModel = missionViewModels
                .stream()
                .filter(vm -> vm.getId() == viewModel.getId())
                .findFirst();

        // The 'old' non updated mission view model is removed.
        oldViewModel.ifPresent(oldMissionVM -> {
            log.debug("Remove out of date mission with id: '{}'", oldMissionVM.getId());
            missionViewModels.remove(oldMissionVM);
        });

        // The 'updated' mission view model is then added back in.

        log.debug("Add newly updated mission with id: '{}'", viewModel.getId());
        missionViewModels.add(viewModel);
        missions.set(FXCollections.observableArrayList(missionViewModels));

        String ids = missionViewModels
                .stream().map(AirMissionViewModel::getId)
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(","));

        log.debug("All missions view models: '{}'", ids);

        removeFromReady(viewModel);       // Remove the mission squadrons from the ready list.
        airbaseViewModel.updateTotalMissions();

        editReady(viewModel);             // Add any squadrons removed from the mission to the ready list.
    }

    /**
     * Remove a mission from this view model.
     *
     * @param viewModel The mission view model.
     */
    public void removeMission(final AirMissionViewModel viewModel) {
        missionViewModels.remove(viewModel);
        missions.set(FXCollections.observableArrayList(missionViewModels));

        String ids = missionViewModels.stream().map(AirMissionViewModel::getId).map(i -> Integer.toString(i)).collect(Collectors.joining(","));

        log.debug("remove mission with id: '{}'", viewModel.getId());
        log.debug("All missions view models: '{}'", ids);

        addToReady(viewModel);
        airbaseViewModel.updateTotalMissions();
    }

    /**
     * Add the given squadron to a patrol.
     *
     * @param type The type of patrol.
     * @param squadron The squadron added to the patrol.
     */
    public void addToPatrol(final PatrolType type, final Squadron squadron) {
        patrolViewModels.get(type).addToPatrol(squadron);
        removeFromReady(squadron);
    }

    /**
     * Remove the given squadron from a patrol.
     *
     * @param type The of patrol.
     * @param squadron The squadron removed from the patrol.
     */
    public void removeFromPatrol(final PatrolType type, final Squadron squadron) {
        patrolViewModels.get(type).removeFromPatrol(squadron);
        addToReady(squadron);
    }

    /**
     * Determine the state of the given squadron. The state is determined by where the squadron is located
     * in the view model. It is not determined by the model. Also, note that QUEUED_FOR_MISSION is the same
     * as ON_MISSION for our purposes here. The same is true of the patrol states.
     *
     * @param squadron The squadron whose state is determined.
     * @return The state of the squadron.
     */
    public SquadronState determineSquadronState(final Squadron squadron) {
        return missionViewModels.stream().anyMatch(mission -> mission.isSquadronOnMission(squadron)) ? SquadronState.ON_MISSION
                : patrolViewModels.values().stream().anyMatch(patrol -> patrol.isSquadronOnPatrol(squadron)) ? SquadronState.ON_PATROL
                : readySquadrons.values().stream().flatMap(p -> p.getValue().stream()).anyMatch(s -> s == squadron) ? SquadronState.READY
                : SquadronState.HANGER;
    }

    /**
     * Set the selected aircraft.
     *
     * @param aircraft The selected aircraft.
     */
    public void setSelectedAircraft(final Aircraft aircraft) {
        selectedAircraft.setValue(aircraft);
    }

    /**
     * Set the selected squadron configuration.
     *
     * @param config The selected squadron configuration.
     */
    public void setSelectedConfig(final SquadronConfig config) {
        selectedConfig.setValue(config);
    }

    /**
     * Set the aircraft models present at this airbase.
     * This is a unique list of aircraft of all nations stationed at this airbase.
     */
    private void setAircraftModels() {
        aircraftModels.setValue(FXCollections.observableArrayList(airbase.getAircraftModelsPresent(nation)));

        Callable<ObservableList<SquadronConfig>> bindingFunction = () -> {
            List<SquadronConfig> configs = Optional
                    .ofNullable(selectedAircraft.getValue())
                    .map(aircraft -> aircraft.getConfiguration()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            return FXCollections.observableArrayList(configs);
        };

        squadronConfigs.bind(Bindings.createObjectBinding(bindingFunction, selectedAircraft));

        selectedAircraft.setValue(aircraftModels.getValue().get(0));
    }

    /**
     * Initialize the squadrons for the given view type.
     *
     * @param type The squadron view type.
     */
    private void initializeSquadrons(final SquadronViewType type) {
        readySquadrons.put(type, new SimpleObjectProperty<>());
        readyCounts.put(type.toString(), new SimpleIntegerProperty(0));

        allSquadrons.put(type, new SimpleObjectProperty<>());
        squadronCounts.put(type.toString(), new SimpleIntegerProperty(0));

        bindNoSquadronsReady();
    }

    /**
     * Initialize the mission squadron count for the given mission type.
     *
     * @param type The mission type.
     */
    private void initializeMission(final AirMissionType type) {
        missionCounts.put(type.toString(), new SimpleIntegerProperty(0));
    }

    /**
     * Initialize the patrol squadron count for the given patrol type.
     *
     * @param type The patrol type.
     */
    private void initializePatrol(final PatrolType type) {
        patrolCounts.put(type.getValue(), new SimpleIntegerProperty(0));
    }

    /**
     * Set the airbase's ready squadrons.
     */
    private void setReadySquadrons() {
        getSquadrons(SquadronState.READY).forEach(this::setReadySquadron);

        List<Squadron> totalReady = readySquadrons
                .values()
                .stream()
                .map(ObjectProperty::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        totalReadySquadrons.set(FXCollections.observableArrayList(totalReady));
    }

    private void setAllSquadrons() {
        getSquadrons(null).forEach(this::setAllSquadron);
    }

    /**
     * Get the squadrons that are in the given state from the airbase.
     *
     * @param state A squadron state.
     * @return A map of squadron types to squadrons of that type that are in the given state.
     */
    private Map<SquadronViewType, List<Squadron>> getSquadrons(final SquadronState state) {
        return airbase.getSquadronMap(nation, state)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> SquadronViewType.get(e.getKey()),
                        Map.Entry::getValue,
                        ListUtils::union,
                        LinkedHashMap::new));
    }

    /**
     * Set the ready squadron of the given squadron type.
     *
     * @param type The squadron type.
     * @param ready The ready squadrons of the given type.
     */
    private void setReadySquadron(final SquadronViewType type, final List<Squadron> ready) {
        readySquadrons.get(type).set(FXCollections.observableArrayList(ready));
        readyCounts.get(type.toString()).setValue(ready.size());
    }

    private void setAllSquadron(final SquadronViewType type, final List<Squadron> all) {
        allSquadrons.get(type).set(FXCollections.observableArrayList(all));
        squadronCounts.get(type.toString()).setValue(all.size());
    }

    /**
     * Add all of the air mission squadrons to the ready list.
     *
     * @param viewModel The air mission view model.
     */
    private void addToReady(final AirMissionViewModel viewModel) {
        viewModel
                .getAssigned()
                .values()
                .stream()
                .flatMap(list -> list.getValue().stream())
                .forEach(this::addToReady);
    }

    /**
     * Add all of the air mission ready squadrons to the ready list.
     * If a squadron is already in the ready list it is not added again.
     *
     * @param viewModel The air mission view model.
     */
    private void editReady(final AirMissionViewModel viewModel) {
        viewModel
                .getReady()
                .getValue()
                .forEach(this::addToReady);
    }

    /**
     * Add the given squadron to the ready list of the squadron's type.
     *
     * @param squadron The squadron added.
     */
    private void addToReady(final Squadron squadron) {
        if (!totalReadySquadrons.get().contains(squadron)) {   // If the squadron is already in the ready list don't add it again.

            SquadronViewType type = SquadronViewType.get(squadron.getType());

            readySquadrons.get(type).get().add(squadron);
            readyCounts.get(type.toString()).setValue(readySquadrons.get(type).getValue().size());

            // Have to set the value of the totalReadySquadrons property to trigger the custom object binding used by observers.
            // Modifying the list by calling add or remove does not work. This seems like a Javafx bug.
            List<Squadron> totalReady = totalReadySquadrons.get();
            totalReady.add(squadron);

            totalReadySquadrons.set(FXCollections.observableArrayList(totalReady));
        }
    }

    /**
     * Remove all of the air mission squadrons from the ready list.
     *
     * @param viewModel The air mission view model.
     */
    private void removeFromReady(final AirMissionViewModel viewModel) {
        // Remove the mission squadrons from the ready list.
        viewModel
                .getAssigned()
                .values()
                .stream()
                .flatMap(list -> list.getValue().stream())
                .forEach(this::removeFromReady);
    }

    /**
     * Remove the given squadron from the ready list of the squadron's type.
     *
     * @param squadron The squadron removed.
     */
    private void removeFromReady(final Squadron squadron) {
        SquadronViewType type = SquadronViewType.get(squadron.getType());

        readySquadrons.get(type).get().remove(squadron);
        readyCounts.get(type.toString()).setValue(readySquadrons.get(type).getValue().size());

        // Have to set the value of the totalReadySquadrons property to trigger the custom object binding used by observers.
        // Modifying the list by calling add or remove does not work. This seems like a Javafx bug.
        List<Squadron> totalReady = totalReadySquadrons.get();
        totalReady.remove(squadron);

        totalReadySquadrons.set(FXCollections.observableArrayList(totalReady));
    }

    /**
     * Set the mission counts.
     */
    private void setMissionCounts() {
        Stream
                .of(AirMissionType.values())
                .forEach(this::bindMissionCount);

        bindNoMissionsExist();
    }

    /**
     * Bind the mission count for the given type of mission.
     *
     * @param type The mission type.
     */
    private void bindMissionCount(final AirMissionType type) {
        Callable<Integer> bindingFunction = () -> missions
                .getValue()
                .stream()
                .filter(mission -> mission.getMissionType().getValue() == type)
                .map(mission -> mission.getTotalAssignedCount().getValue())
                .reduce(0, Integer::sum);

        missionCounts.get(type.toString()).bind(Bindings.createIntegerBinding(bindingFunction, missions));
    }

    /**
     * Bind whether this nation has any missions at this airbase.
     */
    private void bindNoMissionsExist() {
        Callable<Boolean> bindingFunction = () -> missions
                .getValue()
                .isEmpty();

        noMissionsExist.bind(Bindings.createBooleanBinding(bindingFunction, missions));
    }

    /**
     * Set the patrol counts.
     */
    private void setPatrolCounts() {
        Stream
                .of(PatrolType.values())
                .forEach(this::bindPatrolCount);
    }

    /**
     * Bind the patrol count for the given patrol type.
     *
     * @param type The patrol type.
     */
    private void bindPatrolCount(final PatrolType type) {
        patrolCounts
                .get(type.getValue())
                .bind(patrolViewModels
                        .get(type)
                        .getAssignedCount()
                        .get(nation));
    }

    /**
     * Bind the no squadrons ready property. This property indicates if any squadrons are ready
     * for this nation at this airbase.
     */
    private void bindNoSquadronsReady() {
        Callable<Boolean> bindingFunction = () -> totalReadySquadrons.getValue().isEmpty();

        noSquadronsReady.bind(Bindings.createBooleanBinding(bindingFunction, totalReadySquadrons));
    }
}
