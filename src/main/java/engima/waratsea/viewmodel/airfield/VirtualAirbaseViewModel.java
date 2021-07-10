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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.stream.Collectors;

@Slf4j
public class VirtualAirbaseViewModel implements Comparable<AirbaseViewModel>, AirbaseViewModel {
    @Getter private final ObjectProperty<Airbase> airbase = new SimpleObjectProperty<>();                               // The underlying airbase model.

    @Getter private final Map<PatrolType, PatrolViewModel> patrolViewModels = new HashMap<>();                          // The patrols for all nations.
    @Getter private final SquadronsViewModel squadronsViewModel;
    @Getter private final Map<Nation, NationAirbaseViewModel> nationViewModels = new HashMap<>();                       // A given nation's view of this airbase.
    @Getter private final boolean real = false;

    private final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider;
    private final Provider<VirtualPatrolViewModel> patrolViewModelProvider;
    private AirbaseGroupViewModel airbaseGroup;

    private final Provider<PatrolGroups> patrolGroupsProvider;
    private final PatrolGroupDAO patrolGroupDAO;

    private final Map<Integer, List<SquadronViewModel>> missionMap = new HashMap<>();

    @Inject
    public VirtualAirbaseViewModel(final SquadronsViewModel squadronsViewModel,
                                   final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider,
                                   final Provider<VirtualPatrolViewModel> patrolViewModelProvider,
                                   final Provider<PatrolGroups> patrolGroupsProvider,
                                   final PatrolGroupDAO patrolGroupDAO) {
        this.squadronsViewModel = squadronsViewModel;
        this.nationAirbaseViewModelProvider = nationAirbaseViewModelProvider;
        this.patrolViewModelProvider = patrolViewModelProvider;
        this.patrolGroupsProvider = patrolGroupsProvider;
        this.patrolGroupDAO = patrolGroupDAO;
    }

    /**
     * Set the model.
     *
     * @param base The airbase.
     * @return This airbase view model.
     */
    public VirtualAirbaseViewModel setModel(final Airbase base) {
        airbase.setValue(base);

        buildChildViewModels(base);
        relateChildViewModels();

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
    public VirtualAirbaseViewModel setGroup(final AirbaseGroupViewModel group) {
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
     * Get the patrol view models for this airbase.
     *
     * @return The patrol view models for this airbase.
     */
    public Collection<PatrolViewModel> getPatrols() {
        return patrolViewModels.values();
    }

    /**
     * Virtual airbases do not have missions.
     *
     * @return An empty list property.
     */
    @Override
    public ListProperty<AirMissionViewModel> getTotalMissions() {
        return new SimpleListProperty<>();
    }

    @Override
    public void addMission(final Nation nation, final AirMissionViewModel missionViewModel) {
    }

    @Override
    public void removeMission(final Nation nation, final AirMissionViewModel missionViewModel) {
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
    @Override
    public void addToPatrol(final PatrolType type, final SquadronViewModel squadron) {
        log.info("Add to patrol: '{}' squadron: '{}'", type, squadron.getTitle());

        squadronsViewModel.add(squadron);

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
    @Override
    public void removeFromPatrol(final PatrolType type, final SquadronViewModel squadron) {
        // Update the group first, since the view watches for the patrol view model update
        // to display the patrol stats. This way the stats are correct.
        Optional
                .ofNullable(airbaseGroup)
                .ifPresent(group -> group.removeFromPatrol(type, squadron));

        patrolViewModels
                .get(type)
                .removeFromPatrol(squadron);

        squadronsViewModel.remove(squadron);
    }

    /**
     * Add a squadron to the given patrol type.
     *
     * @param type The patrol type.
     * @param squadrons The squadron added to the patrol of the given type.
     * @param missionId The mission id of the mission that adds to the virtual patrol.
     */
    public void addSquadrons(final PatrolType type, final List<SquadronViewModel> squadrons, final int missionId) {
        squadrons.forEach(squadron -> {
            addToPatrol(type, squadron);
            getMissionSquadrons(missionId).add(squadron);
        });
    }

    /**
     * Clear the squadrons from a given mission id.
     *
     * @param type The patrol type.
     * @param missionId The mission that add the squadron to the virtual patrol.
     */
    public void clearSquadrons(final PatrolType type, final int missionId) {
        List<SquadronViewModel> squadrons = Optional
                .ofNullable(missionMap.remove(missionId))
                .orElse(Collections.emptyList());

        squadrons.forEach(squadron -> removeFromPatrol(type, squadron));
    }

    /**
     * Virtual airbases have no missions.
     *
     * @return An empty map.
     */
    @Override
    public Map<Nation, ListProperty<AirMissionViewModel>> getMissionViewModels() {
        return Collections.emptyMap();
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
        patrolViewModels.values().forEach(this::addNationViewToPatrolView);
        nationViewModels.values().forEach(nationVM -> nationVM.setPatrolViewModels(patrolViewModels));
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

        // Add the patrol's squadron to the mission map.
        buildMissionMap(patrolViewModel);
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

    private List<SquadronViewModel> getMissionSquadrons(final int missionId) {
        missionMap.computeIfAbsent(missionId, k -> new ArrayList<>());
        return missionMap.get(missionId);
    }

    private void buildMissionMap(final PatrolViewModel patrolViewModel) {
        patrolViewModel
                .getAssignedAllNations()
                .forEach(squadron -> getMissionSquadrons(squadron.getMissionId()).add(squadron));
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
