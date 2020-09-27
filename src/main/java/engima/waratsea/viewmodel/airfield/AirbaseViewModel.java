package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the view model for a given airbase.
 * All nations that are allowed to use the airbase are included in this view model.
 */
@Slf4j
public class AirbaseViewModel {
    // All missions for all nations originating from this airbase.
    @Getter private final ObjectProperty<ObservableList<AirMissionViewModel>> totalMissions = new SimpleObjectProperty<>(FXCollections.emptyObservableList());

    @Getter private Map<Nation, List<AirMissionViewModel>> missionViewModels = new HashMap<>();                         // The missions for each nation.
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

        Stream.of(PatrolType.values()).forEach(this::buildPatrolViewModel);
        airbase.getNations().forEach(this::buildNationViewModel);

        missionViewModels.forEach(this::addNationViewToMissionView);
        patrolViewModels.values().forEach(patrolVM -> patrolVM.setNationViewModels(nationViewModels));
        nationViewModels.values().forEach(nationVM -> nationVM.setPatrolViewModels(patrolViewModels));
        nationViewModels.forEach((nation, nationVM) -> nationVM.setMissionViewModels(missionViewModels.get(nation)));

        updateTotalMissions();

        return this;
    }

    /**
     * Update the total missions list.
     */
    public void updateTotalMissions() {
        log.debug("Update total missions for airbase: '{}'", airbase.getTitle());

        List<AirMissionViewModel> total = missionViewModels
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        log.debug("Total missions: '{}'", total.size());

        total.forEach(mission -> mission.getAssigned().forEach((role, squadron) -> {
            String squadronNames = squadron
                    .get()
                    .stream()
                    .map(Squadron::getTitle)
                    .collect(Collectors.joining(","));

            log.debug("Mission id: '{}' role: '{}' squadrons: '{}'", new Object[]{mission.getId(), role, squadronNames});
        }));

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
