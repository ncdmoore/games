package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Each nation has its on unique airfield view model. Thus, all of the property values of this view model are for
 * a given nation.
 */
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

    @Getter private ObjectProperty<ObservableList<AirMissionViewModel>> missions = new SimpleObjectProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<PatrolType, PatrolViewModel> patrolViewModels;
    private List<AirMissionViewModel> missionViewModels;

    @Getter private final Map<String, IntegerProperty> squadronCounts = new HashMap<>();
    @Getter private final Map<String, IntegerProperty> missionCounts = new HashMap<>();
    @Getter private final Map<String, IntegerProperty> patrolCounts = new HashMap<>();       // Per nation.
    @Getter private final Map<String, IntegerProperty> readyCounts = new HashMap<>();

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
                .forEach(this::initializeReady);

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
        setSquadronCounts();

        return this;
    }

    /**
     * Set the airbase mission view models.
     *
     * @param airbaseMissions The airbase mission view models.
     */
    public void setMissionViewModels(final List<AirMissionViewModel> airbaseMissions) {
        missionViewModels = airbaseMissions;
        missions.set(FXCollections.observableArrayList(airbaseMissions));

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
        removeFromReady(viewModel);            // Remove the mission squadrons from the ready list.
        airbaseViewModel.updateTotalMissions();
    }

    /**
     * Edit a mission within this view model.
     *
     * @param viewModel The mission view model.
     */
    public void editMission(final AirMissionViewModel viewModel) {
        removeFromReady(viewModel);       // Remove the mission squadrons from the ready list.
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
     * Initialize the ready squadrons for the given view type.
     *
     * @param type The squadron view type.
     */
    private void initializeReady(final SquadronViewType type) {
        readySquadrons.put(type, new SimpleObjectProperty<>());
        readyCounts.put(type.toString(), new SimpleIntegerProperty(0));
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
     * Set the squadron counts.
     */
    private void setSquadronCounts() {
        Stream
                .of(SquadronViewType.values())
                .forEach(this::setSquadronCount);
    }

    /**
     * Set the given type's squadron count.
     *
     * @param type A squadron view type.
     */
    private void setSquadronCount(final SquadronViewType type) {
        int count = (int) squadrons
                .getValue()
                .stream()
                .filter(squadron -> SquadronViewType.get(squadron.getType()) == type)
                .count();

        squadronCounts.put(type.toString(), new SimpleIntegerProperty(count));
    }
}
