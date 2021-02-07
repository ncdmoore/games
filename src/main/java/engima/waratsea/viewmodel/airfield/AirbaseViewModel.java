package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
public class AirbaseViewModel {
    @Getter private final ObjectProperty<Airbase> airbase = new SimpleObjectProperty<>();                               // The underlying airbase model.

    // All missions for all nations originating from this airbase. This is bound to the aggregate sum of
    // all the mission view models of each nation.
    @Getter private final ListProperty<AirMissionViewModel> totalMissions = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<Nation, ListProperty<AirMissionViewModel>> missionViewModels = new HashMap<>();                 // The missions for each nation.
    @Getter private final Map<PatrolType, PatrolViewModel> patrolViewModels = new HashMap<>();                          // The patrols for all nations.
    @Getter private final SquadronsViewModel squadronsViewModel;
    @Getter private final Map<Nation, NationAirbaseViewModel> nationViewModels = new HashMap<>();                       // A given nation's view of this airbase.

    private final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider;
    private final Provider<AirMissionViewModel> missionViewModelProvider;
    private final Provider<PatrolViewModel> patrolViewModelProvider;

    /**
     * Constructor called by guice.
     *
     * @param nationAirbaseViewModelProvider Provides nation airbase view models.
     * @param missionViewModelProvider Provides mission view models.
     * @param patrolViewModelProvider Provides patrol view models.
     * @param squadronsViewModel The squadrons view model. It houses all the airbase squadron view models.
     */
    @Inject
    public AirbaseViewModel(final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider,
                            final Provider<AirMissionViewModel> missionViewModelProvider,
                            final Provider<PatrolViewModel> patrolViewModelProvider,
                            final SquadronsViewModel squadronsViewModel) {
        this.nationAirbaseViewModelProvider = nationAirbaseViewModelProvider;
        this.missionViewModelProvider = missionViewModelProvider;
        this.patrolViewModelProvider = patrolViewModelProvider;
        this.squadronsViewModel = squadronsViewModel;
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
        patrolViewModels.get(type).addToPatrol(squadron);
    }

    /**
     * Remove a squadron from the given patrol type.
     *
     * @param type The patrol type.
     * @param squadron The squadron removed from the patrol of the given type.
     */
    public void removeFromPatrol(final PatrolType type, final SquadronViewModel squadron) {
        patrolViewModels.get(type).removeFromPatrol(squadron);
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
        patrolViewModels.values().forEach(patrolVM -> patrolVM.setNationViewModels(nationViewModels));
        nationViewModels.values().forEach(nationVM -> nationVM.setPatrolViewModels(patrolViewModels));
        nationViewModels.forEach((nation, nationVM) -> nationVM.setMissionViewModels(missionViewModels));
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
}
