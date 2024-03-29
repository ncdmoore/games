package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SquadronsViewModel {
    private final Provider<SquadronViewModel> provider;

    // Squadron names are unique throughout the game. Thus a given squadron name uniquely identifies a squadron view model.
    // Contains all nation's squadron view models for a given airbase.
    // key: Squadron Name, value: squadron view model.
    private Map<String, SquadronViewModel> squadronNameMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param provider Provides squadron view models.
     */
    @Inject
    public SquadronsViewModel(final Provider<SquadronViewModel> provider) {
        this.provider = provider;
    }

    /**
     * Set the backing model of the airbase's squadrons.
     *
     * @param airbase The airbase.
     */
    public void setModel(final Airbase airbase) {
        log.info("Set squadrons for airbase: '{}'", airbase.getTitle());
        log.info("Contains squadrons: '{}'", airbase.getSquadrons().stream().map(Squadron::getTitle).collect(Collectors.joining(",")));

        squadronNameMap = airbase
                .getSquadrons()
                .stream()
                .map(squadron -> provider.get().setModel(squadron))
                .collect(Collectors.toMap(SquadronViewModel::getNameAsString,       // key:   squadron name.
                                          svm -> svm));                             // value: squadron view model.
    }

    /**
     * Get a squadron view model given the backing squadron.
     *
     * @param squadron The backing squadron.
     * @return The corresponding squadron view model of the given squadron.
     */
    public SquadronViewModel get(final Squadron squadron) {
        return squadronNameMap.get(squadron.getName());
    }

    /**
     * Get a list of the corresponding squadron view models given a list of squadrons.
     *
     * @param squadrons The backing squadrons.
     * @return The corresponding squadron view models of the given squadrons.
     */
    public List<SquadronViewModel> get(final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .map(s -> squadronNameMap.get(s.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Get a map of corresponding aircraft types to lists of squadron view models or that type.
     *
     * @param squadrons The backing squadrons.
     * @return The corresponding squadron view models of the given squadrons.
     */
    public Map<AircraftType, List<SquadronViewModel>> get(final Map<AircraftType, List<Squadron>> squadrons) {
        return squadrons
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,        //  key:   aircraft  type.
                                          e -> get(e.getValue()))); //  value: list of squadron view models.
    }

    /**
     * Add a squadron view model to this collection of squadron view models.
     *
     * @param squadron The squadron view model that is added.
     */
    public void add(final SquadronViewModel squadron) {
        squadronNameMap.put(squadron.getNameAsString(), squadron);
    }

    /**
     * Remove a squadron view model from this collection of squadron view models.
     *
     * @param squadron The squadron view model that is removed.
     */
    public void remove(final SquadronViewModel squadron) {
        squadronNameMap.remove(squadron.getNameAsString());
    }

    /**
     * Save the squadrons.
     */
    public void save() {
        squadronNameMap
                .values()
                .forEach(SquadronViewModel::save);
    }
}
