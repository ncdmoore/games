package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Represents the view model for a given airbase.
 * All nations that are allowed to use the airbase are included in this view model.
 */
@Slf4j
public class AirbaseViewModel {
    // All missions for all nations originating from this airbase. This is bound to the aggregate sum of
    // all the mission view models of each nation.
    @Getter private final ListProperty<AirMissionViewModel> totalMissions = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<Nation, ListProperty<AirMissionViewModel>> missionViewModels = new HashMap<>();                 // The missions for each nation.
    @Getter private final Map<PatrolType, PatrolViewModel> patrolViewModels = new HashMap<>();                          // The patrols for all nations.
    @Getter private final Map<Nation, NationAirbaseViewModel> nationViewModels = new HashMap<>();                       // A given nation's view of this airbase.

    private final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider;
    private final Provider<AirMissionViewModel> missionViewModelProvider;
    private final Provider<PatrolViewModel> patrolViewModelProvider;

    @Getter private Airbase airbase;

    /**
     * Constructor called by guice.
     *
     * @param nationAirbaseViewModelProvider Provides nation airbase view models.
     * @param missionViewModelProvider Provides mission view models.
     * @param patrolViewModelProvider Provides patrol view models.
     */
    @Inject
    public AirbaseViewModel(final Provider<NationAirbaseViewModel> nationAirbaseViewModelProvider,
                            final Provider<AirMissionViewModel> missionViewModelProvider,
                            final Provider<PatrolViewModel> patrolViewModelProvider) {
        this.nationAirbaseViewModelProvider = nationAirbaseViewModelProvider;
        this.missionViewModelProvider = missionViewModelProvider;
        this.patrolViewModelProvider = patrolViewModelProvider;

    }

    /**
     * Set the model.
     *
     * @param base The airbase.
     * @return This airbase view model.
     */
    public AirbaseViewModel setModel(final Airbase base) {
        log.debug("Set model for airbase: '{}'", base.getTitle());

        airbase = base;

        missionViewModels = airbase
                .getNations()
                .stream()
                .collect(Collectors.toMap(nation -> nation, this::buildMissionViewModel));

        PatrolType.stream().forEach(this::buildPatrolViewModel);   // Build a patrol view model for each patrol-type.
        airbase.getNations().forEach(this::buildNationViewModel);  // Build a nation view model for each nation.

        missionViewModels.forEach(this::addNationViewToMissionView);
        patrolViewModels.values().forEach(patrolVM -> patrolVM.setNationViewModels(nationViewModels));
        nationViewModels.values().forEach(nationVM -> nationVM.setPatrolViewModels(patrolViewModels));
        nationViewModels.forEach((nation, nationVM) -> nationVM.setMissionViewModels(missionViewModels));

        bindTotalMissions();  // We have to wait to bind until the nations are known.

        return this;
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
     * Save the missions to the model.
     */
    public void saveMissions() {
        airbase.clearMissions();
        totalMissions.forEach(AirMissionViewModel::saveMission);
    }

    /**
     * Get the patrol view models for this airbase.
     *
     * @return The patrol view modles for this airbase.
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
    public SimpleListProperty<Squadron> getAssignedPatrolSquadrons(final PatrolType type, final Nation nation) {
        return patrolViewModels.get(type).getAssigned().get(nation);
    }

    /**
     * Get the squadrons available for the given patrol type.
     *
     * @param type The patrol type.
     * @param nation The nation.
     * @return The squadrons available for the given patrol type.
     */
    public SimpleListProperty<Squadron> getAvailablePatrolSquadrons(final PatrolType type, final Nation nation) {
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
    public void addToPatrol(final PatrolType type, final Squadron squadron) {
        patrolViewModels.get(type).addToPatrol(squadron);
    }

    /**
     * Remove a squadron from the given patrol type.
     *
     * @param type The patrol type.
     * @param squadron The squadron removed from the patrol of the given type.
     */
    public void removeFromPatrol(final PatrolType type, final Squadron squadron) {
        patrolViewModels.get(type).removeFromPatrol(squadron);
    }

    /**
     * Save the patrols to the model.
     */
    public void savePatrols() {
        airbase.clearPatrols();
        patrolViewModels.values().forEach(PatrolViewModel::savePatrol);
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
                .getMissions(nation)
                .stream()
                .map(mission -> missionViewModelProvider.get().setModel(mission))
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
                .setModel(airbase.getPatrol(type));

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
