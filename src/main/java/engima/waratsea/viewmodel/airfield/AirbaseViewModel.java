package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.model.taskForce.patrol.PatrolGroupDAO;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import engima.waratsea.viewmodel.taskforce.air.AirbaseGroupViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Represents the view model for a given airbase.
 * All nations that are allowed to use the airbase are included in this view model.
 */
@Slf4j
public class AirbaseViewModel implements Comparable<AirbaseViewModel> {
    @Getter private final ObjectProperty<Airbase> airbase = new SimpleObjectProperty<>();                               // The underlying airbase model.

    // All missions for all nations originating from this airbase. This is bound to the aggregate sum of
    // all the mission view models of each nation.
    @Getter private final ListProperty<AirMissionViewModel> totalMissions = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<Nation, ListProperty<AirMissionViewModel>> missionViewModels = new HashMap<>();                 // The missions for each nation.
    @Getter private final Map<PatrolType, PatrolViewModel> patrolViewModels = new HashMap<>();                          // The patrols for all nations.
    @Getter private final SquadronsViewModel squadronsViewModel;
    @Getter private final Map<Nation, NationAirbaseViewModel> nationViewModels = new HashMap<>();                       // A given nation's view of this airbase.

    @Getter private final BooleanProperty squadronsPresent = new SimpleBooleanProperty(false);

    private final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider;
    private final Provider<AirMissionViewModel> missionViewModelProvider;
    private final Provider<PatrolViewModel> patrolViewModelProvider;

    private AirbaseGroupViewModel airbaseGroup;

    private final Provider<PatrolGroups> patrolGroupsProvider;
    private final PatrolGroupDAO patrolGroupDAO;

    /**
     * Constructor called by guice.
     *
     * @param nationAirbaseViewModelProvider Provides nation airbase view models.
     * @param missionViewModelProvider Provides mission view models.
     * @param patrolViewModelProvider Provides patrol view models.
     * @param squadronsViewModel The squadrons view model. It houses all the airbase squadron view models.
     * @param patrolGroupsProvider Provides patrol groups.
     * @param patrolGroupDAO Provides patrol groups.
     */
    @Inject
    public AirbaseViewModel(final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider,
                            final Provider<AirMissionViewModel> missionViewModelProvider,
                            final Provider<PatrolViewModel> patrolViewModelProvider,
                            final SquadronsViewModel squadronsViewModel,
                            final Provider<PatrolGroups> patrolGroupsProvider,
                            final PatrolGroupDAO patrolGroupDAO) {
        this.nationAirbaseViewModelProvider = nationAirbaseViewModelProvider;
        this.missionViewModelProvider = missionViewModelProvider;
        this.patrolViewModelProvider = patrolViewModelProvider;
        this.squadronsViewModel = squadronsViewModel;
        this.patrolGroupsProvider = patrolGroupsProvider;
        this.patrolGroupDAO = patrolGroupDAO;

        bindSquadronsPresent();
    }

    /**
     * Set the model.
     *
     * @param base The airbase.
     * @return This airbase view model.
     */
    public AirbaseViewModel setModel(final Airbase base) {
        airbase.setValue(base);

        buildChildViewModels(base);
        relateChildViewModels();

        bindTotalMissions();  // We have to wait to bind until the nations are known.

        return this;
    }

    /**
     * Set the airbase's group. This indicates that this airbase is part of a group.
     * This is currently only used by ships: aircraft carriers and capital ships in
     * a task force where the task force is the airbase group.
     *
     * @param group The airbase group.
     * @return This airbase view model.
     */
    public AirbaseViewModel setGroup(final AirbaseGroupViewModel group) {
        airbaseGroup = group;
        return this;
    }

    /**
     * Get the underlying airbase model.
     *
     * @return The airbase model.
     */
    public Airbase getAirbaseModel() {
        return airbase.getValue();
    }

    /**
     * Get the airbase's nations.
     *
     * @return The airbase's nations.
     */
    public Set<Nation> getNations() {
        return Optional
                .ofNullable(airbase.getValue())
                .map(Airbase::getNations)
                .orElse(Collections.emptySet());
    }

    /**
     * Get the current missions for the given nation.
     *
     * @param nation The nation whose missions are returned.
     * @return The missions of the given nation.
     */
    public List<AirMissionViewModel> getMissions(final Nation nation) {
        return missionViewModels.get(nation);
    }

    /**
     * Add a mission to this airbase for the given nation.
     *
     * @param nation The nation that conducts the mission.
     * @param missionViewModel The mission that is added.
     */
    public void addMission(final Nation nation, final AirMissionViewModel missionViewModel) {
        missionViewModels.get(nation).add(missionViewModel);
    }

    /**
     * Remove a mission from this airbase from the given nation.
     *
     * @param nation The nation that conducts the removed mission.
     * @param missionViewModel The mission that is removed.
     */
    public void removeMission(final Nation nation, final AirMissionViewModel missionViewModel) {
        missionViewModels.get(nation).remove(missionViewModel);
    }

    /**
     * Save the missions to the model. The squadrons are updated first. This allows the missions and patrols
     * to ensure that the squadron state and configuration are set correctly. When a squadron is added to
     * a patrol its configuration is set. When a squadron is added to a mission its configuration is also set.
     * The squadron view model save updates the state and configuration of all squadrons not on a mission
     * or patrol. Thus it is very necessary.
     */
    public void save() {
        airbase.getValue().clear();                                            // Clear the airbase's missions and patrols.
        squadronsViewModel.save();                                             // Save the squadrons: configuration and state.
        totalMissions.forEach(AirMissionViewModel::saveMission);               // Save the new and edited missions.
        patrolViewModels.values().forEach(PatrolViewModel::savePatrol);        // Save the edited patrols.
    }

    /**
     * Get the patrol view models for this airbase.
     *
     * @return The patrol view models for this airbase.
     */
    public Collection<PatrolViewModel> getPatrols() {
        return patrolViewModels.values();
    }

    /**
     * Get the squadrons assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @param nation The nation.
     * @return The squadrons assigned to the given patrol type.
     */
    public SimpleListProperty<SquadronViewModel> getAssignedPatrolSquadrons(final PatrolType type, final Nation nation) {
        return patrolViewModels.get(type).getAssigned().get(nation);
    }

    /**
     * Get the squadrons available for the given patrol type.
     *
     * @param type The patrol type.
     * @param nation The nation.
     * @return The squadrons available for the given patrol type.
     */
    public SimpleListProperty<SquadronViewModel> getAvailablePatrolSquadrons(final PatrolType type, final Nation nation) {
        return patrolViewModels.get(type).getAvailable().get(nation);
    }

    /**
     * Indicates if any squadron are assigned to the given patrol type.
     *
     * @param type The patrol type.
     * @param nation The nation.
     * @return True if squadrons are assigned to the given patrol type. False otherwise.
     */
    public BooleanProperty getAssignedPatrolExists(final PatrolType type, final Nation nation) {
        return patrolViewModels.get(type).getAssignedExists().get(nation);
    }

    /**
     * Indicates if any squadrons are available for the given patrol type.
     *
     * @param type The patrol type.
     * @param nation The nation.
     * @return True if squadrons are available for the given patrol type. False otherwise.
     */
    public BooleanProperty getAvailablePatrolExists(final PatrolType type, final Nation nation) {
        return patrolViewModels.get(type).getAvailableExists().get(nation);
    }

    /**
     * Add a squadron to the given patrol type.
     *
     * @param type The patrol type.
     * @param squadron The squadron added to the patrol of the given type.
     */
    public void addToPatrol(final PatrolType type, final SquadronViewModel squadron) {
        // Update the group first, since the view watches for the patrol view model update
        // to display the patrol stats. This way the stats are correct.
        Optional
                .ofNullable(airbaseGroup)
                .ifPresent(group -> group.addToPatrol(type, squadron));

        patrolViewModels
                .get(type)
                .addToPatrol(squadron);
    }

    /**
     * Remove a squadron from the given patrol type.
     *
     * @param type The patrol type.
     * @param squadron The squadron removed from the patrol of the given type.
     */
    public void removeFromPatrol(final PatrolType type, final SquadronViewModel squadron) {
        // Update the group first, since the view watches for the patrol view model update
        // to display the patrol stats. This way the stats are correct.
        Optional
                .ofNullable(airbaseGroup)
                .ifPresent(group -> group.removeFromPatrol(type, squadron));

        patrolViewModels
                .get(type)
                .removeFromPatrol(squadron);
    }

    /**
     * Get the airbase patrol group.
     *
     * For airfields this is simply the patrol group of the airfield. This consists of
     * only the squadrons in this airbase's patrol view models.
     *
     * For task forces this is the aggregate of all airbases within the task force; i.e.,
     * all ships with squadrons.
     *
     * @param patrolType The patrol type.
     * @return The corresponding patrol group for the given patrol type.
     */
    public PatrolGroup getPatrolGroup(final PatrolType patrolType) {
        return Optional
                .ofNullable(airbaseGroup)
                .map(group -> group.getPatrolGroup(patrolType))
                .orElseGet(() -> getThisAirbasesPatrolGroup(patrolType));
    }

    /**
     * Get the String representation of this airbase view model.
     *
     * @return The String representation of this airbase view model.
     */
    @Override
    public String toString() {
        return Optional.ofNullable(airbase.getValue()).map(Airbase::getTitle).orElse("");
    }

    /**
     * Build the child view models.
     *  - mission view models (one for each mission). The mission view models represent the airbase's current missions.
     *  - patrol view models (one for each patrol type). The patrol view models represent the airbase's current patrols.
     *  - nation view models (one for each nation). The nation view models represent a per nation view of the airbase.
     *
     * @param base The airbase model.
     */
    private void buildChildViewModels(final Airbase base) {
        squadronsViewModel.setModel(base);                         // Build the squadron view models for this airbase. This must be done first.

        missionViewModels = base
                .getNations()
                .stream()
                .collect(Collectors.toMap(nation -> nation, this::buildMissionViewModel));

        PatrolType.stream().forEach(this::buildPatrolViewModel);   // Build a patrol view model for each patrol-type.
        base.getNations().forEach(this::buildNationViewModel);     // Build a nation view model for each nation.
    }

    /**
     * Connect the child view models to each other.
     *  - The mission view models receive a nation view model.
     *  - The patrol view models receive a nation view model.
     *  - The nation view models receive both patrol view models and mission view models.
     */
    private void relateChildViewModels() {
        missionViewModels.forEach(this::addNationViewToMissionView);
        patrolViewModels.values().forEach(this::addNationViewToPatrolView);
        nationViewModels.values().forEach(nationVM -> nationVM.setPatrolViewModels(patrolViewModels));
        nationViewModels.forEach((nation, nationVM) -> nationVM.setMissionViewModels(missionViewModels));
    }

    /**
     * Bind the squadrons present property.
     */
    private void bindSquadronsPresent() {
        squadronsPresent.bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(airbase.getValue())
                .map(Airbase::areSquadronsPresent)
                .orElse(false), airbase));
    }

    /**
     * Bind the total missions list. It depends on all of the nation's missions.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void bindTotalMissions() {
        final ListProperty[] listProperties = missionViewModels.values().toArray(ListProperty[]::new);

        Callable<ObservableList<AirMissionViewModel>> bindingFunction = () -> {
            List<AirMissionViewModel> allMissionsVMs = new ArrayList<>();

            for (List<AirMissionViewModel> missionVMs : listProperties) {
                allMissionsVMs.addAll(missionVMs);
            }

            return FXCollections.observableArrayList(allMissionsVMs);
        };

        totalMissions.bind(Bindings.createObjectBinding(bindingFunction, listProperties));
    }

    /**
     * Build the mission views for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The list of build air mission view models.
     */
    private ListProperty<AirMissionViewModel> buildMissionViewModel(final Nation nation) {
        List<AirMissionViewModel> missionsVMs = airbase
                .getValue()
                .getMissions(nation)
                .stream()
                .map(mission -> missionViewModelProvider
                        .get()
                        .setSquadrons(squadronsViewModel)
                        .setModel(mission))
                .collect(Collectors.toList());

        return new SimpleListProperty<>(FXCollections.observableList(missionsVMs));
    }

    /**
     * Build the patrol view for the given type.
     *
     * @param type The patrol type.
     */
    private void buildPatrolViewModel(final PatrolType type) {
        PatrolViewModel patrolViewModel =  patrolViewModelProvider
                .get()
                .setSquadrons(squadronsViewModel)
                .setModel(airbase.getValue().getPatrol(type));

        patrolViewModels.put(type, patrolViewModel);
    }

    /**
     * Build the given nation's view of the airbase.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void buildNationViewModel(final Nation nation) {
        NationAirbaseViewModel nationAirbaseViewModel = nationAirbaseViewModelProvider
                .get()
                .setModel(nation, this);

        nationViewModels.put(nation, nationAirbaseViewModel);
    }

    /**
     * Add the airbase nation's view model to the mission view model.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param missionVMs The given nation's air mission view models.
     */
    private void addNationViewToMissionView(final Nation nation, final List<AirMissionViewModel> missionVMs) {
        missionVMs.forEach(missionVM -> missionVM.setNationViewModel(nationViewModels.get(nation)));
    }

    /**
     * Add the airbase nation view models to the patrol view model.
     *
     * @param patrolViewModel A given patrol view model.
     */
    private void addNationViewToPatrolView(final PatrolViewModel patrolViewModel) {
        patrolViewModel.setNationViewModels(nationViewModels);
    }

    /**
     * Get this airbase's patrol group. This is used by airfields which are a single air group of themselves.
     *
     * @param patrolType The patrol type.
     * @return A this airbase's patrol group that corresponds to the given patrol type.
     */
    private PatrolGroup getThisAirbasesPatrolGroup(final PatrolType patrolType) {
        PatrolGroupData data = new PatrolGroupData();

        List<Squadron> totalOnPatrol = patrolViewModels
                .get(patrolType)
                .getAssignedAllNations()
                .getValue()
                .stream()
                .map(SquadronViewModel::get)
                .collect(Collectors.toList());

        // Since we are kinda faking out the patrol group, we also
        // need to fake a patrols groups parent and corresponding
        // airbase group. Classes that use the patrol group typically
        // also need access to its airbase group.
        PatrolGroups patrolGroups = patrolGroupsProvider.get();
        patrolGroups.setAirbaseGroup((Airfield) airbase.getValue());

        data.setType(patrolType);
        data.setSquadrons(totalOnPatrol);
        data.setGroups(patrolGroups);

        return patrolGroupDAO.load(data);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(final @NotNull AirbaseViewModel o) {
        return airbase.getValue().compareTo(o.getAirbase().getValue());
    }
}
