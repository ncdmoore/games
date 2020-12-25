package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the view model for a given airbase.
 * All nations that are allowed to use the airbase are included in this view model.
 */
@Slf4j
public class AirbaseViewModel {
    // All missions for all nations originating from this airbase.
    @Getter private final ListProperty<AirMissionViewModel> totalMissions = new SimpleListProperty<>(FXCollections.emptyObservableList());

    private Map<Nation, List<AirMissionViewModel>> missionViewModels = new HashMap<>();                                 // The missions for each nation.
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
        nationViewModels.forEach((nation, nationVM) -> nationVM.setMissionViewModels(missionViewModels.get(nation)));

        updateTotalMissions();

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
        updateTotalMissions();
    }

    /**
     * Remove a mission from this airbase from the given nation.
     *
     * @param nation The nation that conducts the removed mission.
     * @param missionViewModel The mission that is removed.
     */
    public void removeMission(final Nation nation, final AirMissionViewModel missionViewModel) {
        missionViewModels.get(nation).remove(missionViewModel);
        updateTotalMissions();
    }

    /**
     * Save the missions to the model.
     */
    public void saveMissions() {
        totalMissions.forEach(AirMissionViewModel::saveMission);
    }

    /**
     * Clear the missions from the model.
     */
    public void clearMissions() {
        airbase.clearMissions();
    }

    /**
     * Update the total missions list.
     */
    private void updateTotalMissions() {
        List<AirMissionViewModel> total = missionViewModels
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        totalMissions.setValue(FXCollections.observableArrayList(total));
    }

    /**
     * Build the mission views for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The list of build air mission view models.
     */
    private List<AirMissionViewModel> buildMissionViewModel(final Nation nation) {
        return airbase
                .getMissions(nation)
                .stream()
                .map(mission -> missionViewModelProvider.get().setModel(mission))
                .collect(Collectors.toList());
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
