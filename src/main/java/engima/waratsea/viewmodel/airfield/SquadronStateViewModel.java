package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents the airbase view of the squadrons for a given nation that have a given state.
 * It is currently used in the airfield dialog to show all the squadrons that have a particular state.
 */
public class SquadronStateViewModel {
    private Airbase airbase;                                          // The airbase where the squadrons at this given state are stationed.
    private Nation nation;                                            // The nation of the squadrons at this given state.
    private SquadronState state;                                      // The given squadron state.

    // Map of squadron view type to list of squadrons of that view type
    @Getter private final Map<SquadronViewType, ListProperty<SquadronViewModel>> squadronMap = new HashMap<>();

    @Getter private final Map<SquadronViewType, IntegerProperty> countMap = new HashMap<>();

    // Convenience list that contains all of the squadrons at the given base for the given nation that are at the given state.
    @Getter private final ListProperty<SquadronViewModel> squadrons = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private final IntegerProperty count = new SimpleIntegerProperty();

    // Indicates if there are any squadrons at this given state.
    @Getter private final BooleanProperty noSquadronsPresent = new SimpleBooleanProperty(true);

    private NationAirbaseViewModel nationAirbaseViewModel;

    @Inject
    public SquadronStateViewModel() {
        Stream
                .of(SquadronViewType.values())
                .forEach(type -> {
                    squadronMap.put(type, new SimpleListProperty<>());
                    countMap.put(type, new SimpleIntegerProperty());
                });


        bindCounts();
        bindNoSquadronsPresent();
    }

    /**
     * Initialize the squadrons of the given airbase and nation for the given state.
     *
     * @param newNationAirbaseViewModel The airbase where the squadrons are stationed.
     * @param newNation The nation of all of the squadrons.
     * @param newState The state of all of the squadrons.
     */
    public void setModel(final NationAirbaseViewModel newNationAirbaseViewModel, final Nation newNation, final SquadronState newState) {
        nationAirbaseViewModel = newNationAirbaseViewModel;

        airbase = newNationAirbaseViewModel.getAirbaseViewModel().getAirbaseModel();
        nation = newNation;
        state = newState;

        initSquadronMap();
        initSquadrons();
    }

    /**
     * Add a squadron to this view model. The squadron should have the correct airbase, nation and state.
     *
     * @param squadron The squadron added to this view model. This squadron should have the correct airbase
     *                 nation and state.
     */
    public void add(final SquadronViewModel squadron) {
        if (!squadrons.get().contains(squadron)) {   // If the squadron is already in the ready list don't add it again.
            SquadronViewType type = SquadronViewType.get(squadron.getType());

            squadronMap.get(type).get().add(squadron);

            // Have to set the value of the squadrons property to trigger the custom object binding used by observers.
            // Modifying the list by calling add or remove does not work. This seems like a Javafx bug.
            List<SquadronViewModel> squadronsInDesiredState = squadrons.get();
            squadronsInDesiredState.add(squadron);

            squadrons.set(FXCollections.observableArrayList(squadronsInDesiredState));
        }
    }

    /**
     * Remove the given squadron from the ready list of the squadron's type.
     *
     * @param squadron The squadron removed.
     */
    public void remove(final SquadronViewModel squadron) {
        SquadronViewType type = SquadronViewType.get(squadron.getType());

        squadronMap.get(type).getValue().remove(squadron);

        // Have to set the value of the squadrons property to trigger the custom object binding used by observers.
        // Modifying the list by calling add or remove does not work. This seems like a Javafx bug.
        List<SquadronViewModel> squadronsInDesiredState = squadrons.getValue();
        squadronsInDesiredState.remove(squadron);

        squadrons.set(FXCollections.observableArrayList(squadronsInDesiredState));
    }

    /**
     * Get the current number of squadrons in this view model for the given type of squadron.
     *
     * @param type The type of squadron as displayed in the view.
     * @return The number of squadrons included in this view model of the given type.
     */
    public int getNumber(final SquadronViewType type) {
        return Optional.ofNullable(squadronMap.get(type).getValue())
                .map(List::size)
                .orElse(0);
    }

    /**
     * Determine if the given squadron is included in this view model. This indicates that the given
     * squadron has the same state as squadrons in this view model. All squadrons in this view model
     * have the same state.
     *
     * @param squadron The squadron that is tested to determine if it has the same state as the squadrons
     *                 contained within this view model.
     * @return True if the squadron has the same state. False otherwise.
     */
    public boolean isPresent(final SquadronViewModel squadron) {
        return Optional.ofNullable(squadrons.getValue())
                .map(squadronsInDesiredState -> squadronsInDesiredState.contains(squadron))
                .orElse(false);
    }

    /**
     * Initialize the squadron map for a given nation and state from the airbase.
     */
    private void initSquadronMap() {
        Map<SquadronViewType, List<SquadronViewModel>> squadronsFromAirbase = getSquadronViewModels()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> SquadronViewType.get(e.getKey()),
                        Map.Entry::getValue,
                        ListUtils::union,
                        LinkedHashMap::new));

        squadronsFromAirbase.forEach(this::setSquadron);
    }

    /**
     * Initialize the squadron list.
     */
    private void initSquadrons() {
        List<SquadronViewModel> total = squadronMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        squadrons.set(FXCollections.observableArrayList(total));
    }

    /**
     * Set the squadrons in the squadron map for the given type.
     *
     * @param type The type of squadron.
     * @param squadronsOfType The squadrons of the given type.
     */
    private void setSquadron(final SquadronViewType type, final List<SquadronViewModel> squadronsOfType) {
        squadronMap.get(type).set(FXCollections.observableArrayList(squadronsOfType));
    }

    /**
     * Bind the squadron counts.
     */
    private void bindCounts() {
        count.bind(squadrons.sizeProperty());
        countMap.forEach((type, squadronsOfType) -> squadronsOfType.bind(squadronMap.get(type).sizeProperty()));
    }

    /**
     * Bind the no squadrons ready property. This property indicates if any squadrons are ready
     * for this nation at this airbase.
     */
    private void bindNoSquadronsPresent() {
        noSquadronsPresent.bind(squadrons.emptyProperty());
    }

    private  Map<AircraftType, List<SquadronViewModel>> getSquadronViewModels() {
        return nationAirbaseViewModel.getSquadronViewModels(airbase.getSquadronMap(nation, state));
    }
}
