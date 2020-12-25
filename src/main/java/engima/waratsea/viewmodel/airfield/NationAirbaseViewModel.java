package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Each nation has its on unique airfield view model. Thus, all of the property values of this view model are for
 * a given nation.
 */
@Slf4j
public class NationAirbaseViewModel {
    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty typeTitle = new SimpleStringProperty();
    @Getter private final StringProperty regionTitle = new SimpleStringProperty();

    @Getter private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    @Getter private final StringProperty maxCapacity = new SimpleStringProperty();
    @Getter private final StringProperty current = new SimpleStringProperty();
    @Getter private final StringProperty antiAir = new SimpleStringProperty();

    @Getter private final StringProperty regionMaximum = new SimpleStringProperty();
    @Getter private final StringProperty regionMinimum = new SimpleStringProperty();
    @Getter private final StringProperty regionCurrent = new SimpleStringProperty();

    // A list of air mission view models for this nation. This is 'somewhat' bound to the air mission view models
    // stored in the airbase view model. The airbase view model is the source of truth regarding air missions.
    // The view binds to this property to show the missions in the UI.
    @Getter private final ListProperty<AirMissionViewModel> missionViewModels = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private Map<PatrolType, PatrolViewModel> patrolViewModels;
    @Getter private final Map<SquadronState, SquadronStateViewModel> squadronStateViewModel = new HashMap<>();

    @Getter private final Map<String, IntegerProperty> missionCounts = new HashMap<>();                   // Per nation.
    @Getter private final Map<String, IntegerProperty> patrolCounts = new HashMap<>();                    // Per nation.

    @Getter private final BooleanProperty noMissionsExist = new SimpleBooleanProperty(true);   // Per nation.

    @Getter private final ListProperty<Aircraft> aircraftModels = new SimpleListProperty<>();             // List of all aircraft models present at this airbase.
    @Getter private final ObjectProperty<Aircraft> selectedAircraft = new SimpleObjectProperty<>();       // The selected aircraft model.

    // List of squadron configs allowed for the selected aircraft model.
    @Getter private final ListProperty<SquadronConfig> squadronConfigs = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final ObjectProperty<SquadronConfig> selectedConfig = new SimpleObjectProperty<>();   // The selected squadron configuration for the selected aircraft model.

    @Getter private AirbaseViewModel airbaseViewModel;                                                    // Parent airbase view model.

    @Getter private final ObjectProperty<Airbase> airbase = new SimpleObjectProperty<>();

    @Getter private Nation nation;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider  Provides images.
     * @param props The view properties.
     * @param provider A squadron state view model provider.
     **/
    @Inject
    public NationAirbaseViewModel(final ImageResourceProvider imageResourceProvider,
                                  final ViewProps props,
                                  final Provider<SquadronStateViewModel> provider) {

        bindTitles();
        bindDetails();
        bindRegion();
        bindAircraftModels();
        bindImages(imageResourceProvider, props);
        bindMissionCounts();

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
        airbase.setValue(selectedAirbase.getAirbase());

        airbaseViewModel = selectedAirbase;
        nation = selectedNation;
        Airbase base = selectedAirbase.getAirbase();

        selectAircraftModel();

        squadronStateViewModel.get(SquadronState.READY).init(base, nation, SquadronState.READY);
        squadronStateViewModel.get(SquadronState.ALL).init(base, nation, SquadronState.ALL);

        return this;
    }

    /**
     * Set the airbase mission view models.
     *
     * @param airbaseMissions The airbase mission view models.
     */
    public void setMissionViewModels(final List<AirMissionViewModel> airbaseMissions) {
        missionViewModels.set(FXCollections.observableArrayList(airbaseMissions));

        String ids = missionViewModels
                .stream()
                .map(AirMissionViewModel::getId)
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(","));

        log.debug("Initialize mission view models with ids: '{}'", ids);
    }

    /**
     * Set the airbase patrol view models.
     *
     * @param airbasePatrols The airbase patrol view models.
     */
    public void setPatrolViewModels(final Map<PatrolType, PatrolViewModel> airbasePatrols) {
        patrolViewModels = airbasePatrols;

        // We have to wait until here to bind the patrol counts, since the patrol view models are only now known.
        bindPatrolCounts();
    }

    /**
     * Get the squadron map for the given squadron state.
     *
     * @param state The squadron state.
     * @return A map of squadron view type to list of squadrons at the given state.
     */
    public Map<SquadronViewType, ListProperty<Squadron>> getSquadronMap(final SquadronState state) {
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
    public ListProperty<Squadron> getTotalReadySquadrons() {
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
        airbaseViewModel.addMission(nation, viewModel);
        missionViewModels.set(FXCollections.observableArrayList(airbaseViewModel.getMissions(nation)));

        String ids = missionViewModels
                .stream()
                .map(AirMissionViewModel::getId)
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(","));

        log.debug("Add mission with id: '{}'", viewModel.getId());
        log.debug("All missions view models: '{}'", ids);

        removeFromReady(viewModel);            // Remove the mission squadrons from the ready list.
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
            airbaseViewModel.removeMission(nation, oldMissionVM);
        });

        // The 'updated' mission view model is then added back in.
        log.debug("Add newly updated mission with id: '{}'", viewModel.getId());
        airbaseViewModel.addMission(nation, viewModel);

        missionViewModels.set(FXCollections.observableArrayList(airbaseViewModel.getMissions(nation)));

        String ids = missionViewModels
                .stream().map(AirMissionViewModel::getId)
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(","));

        log.debug("All missions view models: '{}'", ids);

        removeFromReady(viewModel);       // Remove the mission squadrons from the ready list.

        editReady(viewModel);             // Add any squadrons removed from the mission to the ready list.
    }

    /**
     * Remove a mission from this view model.
     *
     * @param viewModel The mission view model.
     */
    public void removeMission(final AirMissionViewModel viewModel) {
        airbaseViewModel.removeMission(nation, viewModel);
        missionViewModels.set(FXCollections.observableArrayList(airbaseViewModel.getMissions(nation)));

        String ids = missionViewModels
                .stream()
                .map(AirMissionViewModel::getId)
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(","));

        log.debug("remove mission with id: '{}'", viewModel.getId());
        log.debug("All missions view models: '{}'", ids);

        addToReady(viewModel);
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
    private void bindAircraftModels() {
        Callable<ObservableList<Aircraft>> modelBindingFunction = () -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> FXCollections.observableArrayList(a.getAircraftModelsPresent(nation)))
                .orElse(FXCollections.emptyObservableList());

        aircraftModels.bind(Bindings.createObjectBinding(modelBindingFunction, airbase));

        Callable<ObservableList<SquadronConfig>> configBindingFunction = () -> {
            List<SquadronConfig> configs = Optional
                    .ofNullable(selectedAircraft.getValue())
                    .map(aircraft -> aircraft.getConfiguration()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            return FXCollections.observableArrayList(configs);
        };

        squadronConfigs.bind(Bindings.createObjectBinding(configBindingFunction, selectedAircraft));
    }

    private void selectAircraftModel() {
        if (!aircraftModels.getValue().isEmpty()) {
            selectedAircraft.setValue(aircraftModels.getValue().get(0));
        }
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
    private void bindMissionCounts() {
        AirMissionType
                .stream()
                .forEach(this::bindMissionCount);

        bindNoMissionsExist();
    }

    /**
     * Bind the mission count for the given type of mission.
     *
     * @param type The mission type.
     */
    private void bindMissionCount(final AirMissionType type) {
        missionCounts.put(type.toString(), new SimpleIntegerProperty(0));

        Callable<Integer> bindingFunction = () -> Optional.ofNullable(missionViewModels.getValue())
                .orElse(FXCollections.emptyObservableList())
                .stream()
                .filter(mission -> mission.getMissionType().getValue() == type)
                .map(mission -> mission.getTotalAssignedCount().getValue())
                .reduce(0, Integer::sum);

        missionCounts.get(type.toString()).bind(Bindings.createIntegerBinding(bindingFunction, missionViewModels));
    }

    /**
     * Bind whether this nation has any missions at this airbase.
     */
    private void bindNoMissionsExist() {
        Callable<Boolean> bindingFunction = () -> missionViewModels
                .getValue()
                .isEmpty();

        noMissionsExist.bind(Bindings.createBooleanBinding(bindingFunction, missionViewModels));
    }

    private void bindTitles() {
        title.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(Base::getTitle)
                .orElse(""), airbase));

        typeTitle.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getAirfieldType().getTitle())
                .orElse(""), airbase));

        regionTitle.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(Airbase::getRegionTitle)
                .orElse(""), airbase));
    }

    private void bindDetails() {
        maxCapacity.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getMaxCapacity() + "")
                .orElse(""), airbase));

        current.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getCurrentSteps() + "")
                .orElse(""), airbase));

        antiAir.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getAntiAirRating() + "")
                .orElse(""), airbase));
    }

    private void bindRegion() {
        regionMaximum.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getRegion(nation).getMaxSteps() + "")
                .orElse(""), airbase));

        regionMinimum.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getRegion(nation).getMinSteps() + "")
                .orElse(""), airbase));

        regionCurrent.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.getRegion(nation).getCurrentSteps() + "")
                .orElse(""), airbase));
    }

    private void bindImages(final ImageResourceProvider imageResourceProvider, final ViewProps props) {
        image.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> imageResourceProvider.getImage(getImageName(a, props)))
                .orElse(null), airbase));
    }

    /**
     * Set the patrol counts.
     */
    private void bindPatrolCounts() {
        PatrolType
                .stream()
                .forEach(this::bindPatrolCount);
    }

    /**
     * Bind the patrol count for the given patrol type.
     *
     * @param type The patrol type.
     */
    private void bindPatrolCount(final PatrolType type) {
        patrolCounts.put(type.getValue(), new SimpleIntegerProperty(0));

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

    private String getImageName(final Airbase base, final ViewProps props) {
        AirfieldType airfieldType = base.getAirfieldType();
        return props.getString(nation + ".airfield." + airfieldType + ".image");
    }
}
