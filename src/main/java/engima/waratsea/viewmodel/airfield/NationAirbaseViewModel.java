package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
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

    @Getter private final SimpleListProperty<AirMissionViewModel> missions = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<PatrolType, PatrolViewModel> patrolViewModels;
    private List<AirMissionViewModel> missionViewModels;
    @Getter private final Map<SquadronState, SquadronStateViewModel> squadronStateViewModel = new HashMap<>();

    @Getter private final Map<String, IntegerProperty> missionCounts = new HashMap<>();      // Per nation.
    @Getter private final Map<String, IntegerProperty> patrolCounts = new HashMap<>();       // Per nation.

    @Getter private final BooleanProperty noMissionsExist = new SimpleBooleanProperty(true); // Per nation.

    @Getter private final SimpleListProperty<Aircraft> aircraftModels = new SimpleListProperty<>();       // List of all aircraft models present at this airbase.
    @Getter private final ObjectProperty<Aircraft> selectedAircraft = new SimpleObjectProperty<>();                     // The selected aircraft model.
    @Getter private final SimpleListProperty<SquadronConfig> squadronConfigs = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final ObjectProperty<SquadronConfig> selectedConfig = new SimpleObjectProperty<>();

    @Getter private AirbaseViewModel airbaseViewModel;

    @Getter private Airbase airbase;
    @Getter private Nation nation;

    /**
     * Constructor called by guice.
     *
     * @param provider A squadron state view model provider.
     **/
    @Inject
    public NationAirbaseViewModel(final Provider<SquadronStateViewModel> provider) {
        Stream
                .of(AirMissionType.values())
                .forEach(this::initializeMission);

        Stream
                .of(PatrolType.values())
                .forEach(this::initializePatrol);

        squadronStateViewModel.put(SquadronState.READY, provider.get());
        squadronStateViewModel.put(SquadronState.ALL, provider.get());
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

        setAircraftModels();

        squadronStateViewModel.get(SquadronState.READY).init(airbase, nation, SquadronState.READY);
        squadronStateViewModel.get(SquadronState.ALL).init(airbase, nation, SquadronState.ALL);

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
    public Map<SquadronViewType, SimpleListProperty<Squadron>> getSquadronMap(final SquadronState state) {
        return  squadronStateViewModel
                .get(state)
                .getSquadronMap();
    }

    /**
     * Get the squadrons assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @return The squadrons assigned to the given patrol type.
     */
    public SimpleListProperty<Squadron> getAssignedPatrolSquadrons(final PatrolType type) {
        return patrolViewModels.get(type).getAssigned().get(nation);
    }

    /**
     * Get the squadrons available for the given patrol type.
     *
     * @param type The patrol type.
     * @return The squadrons available for the given patrol type.
     */
    public SimpleListProperty<Squadron> getAvailablePatrolSquadrons(final PatrolType type) {
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
     * Get the total ready squadrons.
     *
     * @return The total ready squadrons.
     */
    public SimpleListProperty<Squadron> getTotalReadySquadrons() {
        return squadronStateViewModel.get(SquadronState.READY).getSquadrons();
    }

    /**
     * Get the total ready counts.
     *
     * @return The total ready counts.
     */
    public Map<String, IntegerProperty> getReadyCounts() {
         return getCounts(SquadronState.READY);
    }

    /**
     * Get the total squadron counts.
     *
     * @return The total squadron counts.
     */
    public Map<String, IntegerProperty> getSquadronCounts() {
        return getCounts(SquadronState.ALL);
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
                : squadronStateViewModel.get(SquadronState.READY).isPresent(squadron) ? SquadronState.READY
                : SquadronState.HANGER;
    }

    /**
     * Get whether there are any ready squadrons.
     *
     * @return Property that indicates if there are any ready squadrons.
     */
    public BooleanProperty getNoSquadronsReady() {
        return squadronStateViewModel.get(SquadronState.READY).getNoSquadronsPresent();
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

        if (!aircraftModels.getValue().isEmpty()) {
            selectedAircraft.setValue(aircraftModels.getValue().get(0));
        }
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
        squadronStateViewModel.get(SquadronState.READY).add(squadron);
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
        squadronStateViewModel.get(SquadronState.READY).remove(squadron);
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

    private Map<String, IntegerProperty> getCounts(final SquadronState state) {
        return squadronStateViewModel
                .get(state)
                .getCountMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
