package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
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
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Each nation has its on unique airfield view model. Thus, all of the property values of this view model are for
 * a given nation. This class serves as a wrapper around the airfield view model in that this class provides
 * a single nations view of the airfield view model.
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
    @Getter private final Map<String, StringProperty> regionCounts = new LinkedHashMap<>();

    @Getter private final Map<SquadronState, SquadronStateViewModel> squadronStateViewModel = new HashMap<>();
    @Getter private final RangeViewModel rangeViewModel;

    @Getter private final Map<String, IntegerProperty> missionCounts = new HashMap<>();                   // Per nation.
    @Getter private final Map<String, IntegerProperty> patrolCounts = new HashMap<>();                    // Per nation.

    @Getter private final BooleanProperty squadronsPresent = new SimpleBooleanProperty(false); // Per nation.
    @Getter private final BooleanProperty noMissionsExist = new SimpleBooleanProperty(true);   // Per nation.

    @Getter private final ListProperty<ProbabilityStats> airOperationStats = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final BooleanProperty airOperationsAffectedByWeather = new SimpleBooleanProperty(false);

    @Getter private final ObjectProperty<Airbase> airbase = new SimpleObjectProperty<>();                 // Need this to support binding in constructor.

    @Getter private AirbaseViewModel airbaseViewModel;                                                    // Parent airbase view model.

    @Getter private Nation nation;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider  Provides images.
     * @param props The view properties.
     * @param provider A squadron state view model provider.
     * @param rangeViewModel The range view model for this nation.
     **/
    @Inject
    public NationAirbaseViewModel(final ResourceProvider imageResourceProvider,
                                  final ViewProps props,
                                  final Provider<SquadronStateViewModel> provider,
                                  final RangeViewModel rangeViewModel) {
        bindTitles();
        bindDetails();
        bindRegion();
        bindImages(imageResourceProvider, props);
        bindAirOperationStats();
        bindSquadronsPresent();

        squadronStateViewModel.put(SquadronState.READY, provider.get());
        squadronStateViewModel.put(SquadronState.ALL, provider.get());
        this.rangeViewModel = rangeViewModel;
    }

    /**
     * Set the model.
     *
     * @param selectedNation The nation: BRITISH, ITALIAN, etc...
     * @param newAirbaseViewModel The airbase.
     * @return This airbase view model.
     */
    public NationAirbaseViewModel setModel(final Nation selectedNation, final AirbaseViewModel newAirbaseViewModel) {
        airbase.bind(newAirbaseViewModel.getAirbase());  // bind to the airbase property in the parent airbase view model.

        airbaseViewModel = newAirbaseViewModel;
        nation = selectedNation;

        squadronStateViewModel.get(SquadronState.READY).setModel(this, nation, SquadronState.READY);
        squadronStateViewModel.get(SquadronState.ALL).setModel(this, nation, SquadronState.ALL);
        rangeViewModel.setModel(nation, this);

        return this;
    }

    /**
     * Set the airbase mission view models.
     *
     * @param airbaseMissions The airbase mission view models.
     */
    public void setMissionViewModels(final Map<Nation, ListProperty<AirMissionViewModel>> airbaseMissions) {
        // We have to wait until here to bind the mission counts, since the mission view models are only now known.
        bindMissionCounts(airbaseMissions.get(nation));
    }

    /**
     * Set the airbase patrol view models.
     *
     * @param airbasePatrols The airbase patrol view models.
     */
    public void setPatrolViewModels(final Map<PatrolType, PatrolViewModel> airbasePatrols) {
        // We have to wait until here to bind the patrol counts, since the patrol view models are only now known.
        bindPatrolCounts(airbasePatrols);
    }

    /**
     * Get the squadron map for the given squadron state.
     *
     * @param state The squadron state.
     * @return A map of squadron view type to list of squadrons at the given state.
     */
    public Map<SquadronViewType, ListProperty<SquadronViewModel>> getSquadronMap(final SquadronState state) {
        return  squadronStateViewModel
                .get(state)
                .getSquadronMap();
    }

    /**
     * Ge the squadron view models.
     *
     * @return The squadron view models.
     */
    public SquadronsViewModel getSquadrons() {
        return airbaseViewModel.getSquadronsViewModel();
    }

    /**
     * Get the patrols.
     *
     * @return The patrols.
     */
    public ListProperty<AirMissionViewModel> getMissionViewModels() {
        return airbaseViewModel.getMissionViewModels().get(nation);
    }

    /**
     * Get the patrols.
     *
     * @return The patrols.
     */
    public Map<PatrolType, PatrolViewModel> getPatrolsViewModels() {
        return airbaseViewModel.getPatrolViewModels();
    }

    /**
     * Get the squadron view models given a squadrons.
     *
     * @param squadrons The squadrons.
     * @return The corresponding squadron view models of the given squadrons.
     */
    public Map<AircraftType, List<SquadronViewModel>> getSquadronViewModels(final Map<AircraftType, List<Squadron>> squadrons) {
        return airbaseViewModel.getSquadronsViewModel().get(squadrons);
    }

    /**
     * Get the squadrons assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @return The squadrons assigned to the given patrol type.
     */
    public SimpleListProperty<SquadronViewModel> getAssignedPatrolSquadrons(final PatrolType type) {
        return airbaseViewModel.getAssignedPatrolSquadrons(type, nation);
    }

    /**
     * Get the squadrons available for the given patrol type.
     *
     * @param type The patrol type.
     * @return The squadrons available for the given patrol type.
     */
    public SimpleListProperty<SquadronViewModel> getAvailablePatrolSquadrons(final PatrolType type) {
        return airbaseViewModel.getAvailablePatrolSquadrons(type, nation);
    }

    /**
     * Indicates if any squadron are assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @return True if squadrons are assigned to the given patrol type. False otherwise.
     */
    public BooleanProperty getAssignedPatrolExists(final PatrolType type) {
        return airbaseViewModel.getAssignedPatrolExists(type, nation);
    }

    /**
     * Indicates if any squadrons are available for the given patrol type.
     *
     * @param type The patrol type.
     * @return True if squadrons are available for the given patrol type. False otherwise.
     */
    public BooleanProperty getAvailablePatrolExists(final PatrolType type) {
        return airbaseViewModel.getAvailablePatrolExists(type, nation);
    }

    /**
     * Get the total ready squadrons.
     *
     * @return The total ready squadrons.
     */
    public ListProperty<SquadronViewModel> getTotalReadySquadrons() {
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
        Optional<AirMissionViewModel> oldViewModel = airbaseViewModel
                .getMissionViewModels()
                .get(nation)
                .stream()
                .filter(vm -> vm.getId() == viewModel.getId())
                .findFirst();

        // The 'old' non updated mission view model is removed.
        oldViewModel.ifPresent(oldMissionVM -> airbaseViewModel.removeMission(nation, oldMissionVM));

        // The 'updated' mission view model is then added back in.
        airbaseViewModel.addMission(nation, viewModel);

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
        addToReady(viewModel);
    }

    /**
     * Add the given squadron to a patrol.
     *
     * @param type The type of patrol.
     * @param squadron The squadron added to the patrol.
     */
    public void addToPatrol(final PatrolType type, final SquadronViewModel squadron) {
        airbaseViewModel.addToPatrol(type, squadron);
        removeFromReady(squadron);
    }

    /**
     * Remove the given squadron from a patrol.
     *
     * @param type The of patrol.
     * @param squadron The squadron removed from the patrol.
     */
    public void removeFromPatrol(final PatrolType type, final SquadronViewModel squadron) {
        airbaseViewModel.removeFromPatrol(type, squadron);
        addToReady(squadron);
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
    private void addToReady(final SquadronViewModel squadron) {
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
    private void removeFromReady(final SquadronViewModel squadron) {
        squadronStateViewModel.get(SquadronState.READY).remove(squadron);
    }

    /**
     * Set the mission counts.
     *
     * @param missionViewModels The mission view models for this nation.
     */
    private void bindMissionCounts(final ListProperty<AirMissionViewModel> missionViewModels) {
        AirMissionType
                .stream()
                .forEach(type -> bindMissionCount(type, missionViewModels));

        bindNoMissionsExist();
    }

    /**
     * Bind the mission count for the given type of mission.
     *
     * @param type The mission type.
     * @param missionViewModels The mission view models for this nation.
     */
    private void bindMissionCount(final AirMissionType type, final ListProperty<AirMissionViewModel> missionViewModels) {
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
        ListProperty<AirMissionViewModel> missionViewModels = airbaseViewModel
                .getMissionViewModels()
                .get(nation);

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
                .map(a -> a.getAirbaseType().getTitle())
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

        regionCounts.put("Minimum (Steps):", regionMinimum);
        regionCounts.put("Maximum (Steps):", regionMaximum);
        regionCounts.put("Current (Steps):", regionCurrent);
    }

    private void bindImages(final ResourceProvider imageResourceProvider, final ViewProps props) {
        image.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> imageResourceProvider.getImage(getImageName(a, props)))
                .orElse(null), airbase));
    }

    private void bindAirOperationStats() {
        airOperationStats.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(Airbase::getAirOperationStats)
                .map(FXCollections::observableArrayList)
                .orElse(FXCollections.emptyObservableList()), airbase));

        airOperationsAffectedByWeather.bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(Airbase::getAirOperationStats)
                .map(stats -> stats
                        .stream()
                        .flatMap(stat -> stat.getProbability().values().stream())
                        .anyMatch(prob -> prob > 0))
                .orElse(false), airbase));
    }

    private void bindSquadronsPresent() {
        squadronsPresent.bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> a.areSquadronsPresent(nation))
                .orElse(false), airbase));
    }

    /**
     * Set the patrol counts.
     *
     * @param airbasePatrols The airbase patrol view models.
     */
    private void bindPatrolCounts(final Map<PatrolType, PatrolViewModel> airbasePatrols) {
        PatrolType
                .stream()
                .forEach(type -> bindPatrolCount(type, airbasePatrols.get(type)));
    }

    /**
     * Bind the patrol count for the given patrol type.
     *
     * @param type The patrol type.
     * @param patrolViewModel The corresponding patrol view model for the given type.
     */
    private void bindPatrolCount(final PatrolType type, final PatrolViewModel patrolViewModel) {
        patrolCounts.put(type.getValue(), new SimpleIntegerProperty(0));

        patrolCounts
                .get(type.getValue())
                .bind(patrolViewModel
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
        AirbaseType airfieldType = base.getAirbaseType();
        return props.getString(nation + ".airfield." + airfieldType + ".image");
    }
}
