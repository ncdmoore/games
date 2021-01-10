package engima.waratsea.model.base.airfield.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.squadron.data.SquadronsData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.utility.ListUtil;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is just a convenience  class that aggregates all of the squadron data structures for an airfield.
 *
 * It does store the squadrons at an airbase. It is the source of truth regarding squadrons stationed
 * at an airbase.
 */
public class Squadrons {
    @Getter private final List<Squadron> squadrons = new ArrayList<>();
    private final Map<String, Squadron> squadronNameMap = new HashMap<>();
    private final Map<AircraftType, List<Squadron>> squadronMap = new LinkedHashMap<>();

    private final SquadronFactory factory;
    private Airbase airbase;

    @Inject
    public Squadrons(final SquadronFactory squadronFactory) {
        this.factory = squadronFactory;

        Stream
                .of(AircraftType.values())
                .sorted()
                .forEach(type -> squadronMap.put(type, new ArrayList<>()));
    }

    /**
     * Get the squadrons persistent data.
     *
     * @return The squadrons persistent data.
     */
    public SquadronsData getData() {
        SquadronsData data = new SquadronsData();

        List<SquadronData> squadronData = squadrons
                .stream()
                .map(Squadron::getData)
                .collect(Collectors.toList());

        data.setSquadrons(squadronData);

        return data;
    }

    /**
     * Build the airfield's squadrons. This is only valid for saved games where the airfield
     * squadrons are already known.
     *
     * @param base The parent airbase..
     * @param data A List of the squadron data.
     */
    public void build(final Airbase base, final SquadronsData data) {
        airbase = base;
        Side side = base.getSide();
        Optional.ofNullable(data)
                .map(SquadronsData::getSquadrons)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronData -> factory.create(side, squadronData.getNation(), squadronData))
                .forEach(this::add);
    }

    /**
     * Get the list of squadrons for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron list for the given nation.
     */
    public List<Squadron> getSquadrons(final Nation nation) {
        return filterNation(nation, squadrons);
    }

    /**
     * Get the squadron given its name.
     *
     * @param squadronName The squadron name.
     * @return The squadron that corresponds to the given squadron name.
     */
    public Squadron getSquadron(final String squadronName) {
        return squadronNameMap.get(squadronName);
    }

    /**
     * Get the list of squadrons for the given nation and given state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return A list of squadron for the given nation and given state.
     */
    public List<Squadron> getSquadrons(final Nation nation, final SquadronState state) {
        return filterNationAndState(nation, state, squadrons);
    }

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation nation) {
        return squadronMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> filterNation(nation, entry.getValue()),
                        (oldList, newList) -> oldList,
                        LinkedHashMap::new));
    }

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation nation, final SquadronState state) {
        return squadronMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> filterNationAndState(nation, state, entry.getValue()),
                        (oldList, newList) -> oldList,
                        LinkedHashMap::new));
    }

    /**
     * Determine the current number of steps deployed at this airfield.
     *
     * @return The current number of steps deployed at this airfield.
     */
    public int deployedSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    /**
     * Get the current number of steps.
     *
     * @return The current number of steps deployed at this airfield.
     */
    public BigDecimal getCurrentSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get the number of steps stationed at this airfield for the
     * given base type of aircraft.
     *
     * @param type An aircraft base type.
     * @return The number of steps of aircraft of the given type based at this airfield.
     */
    public BigDecimal getStepsForType(final AircraftBaseType type) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.getBaseType() == type)
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get a list of the aircraft models present at this airbase. The list is unique.
     *
     * Build a map of models to list of aircraft for that model. Then return the first element of each list.
     * This gives us a unique list of aircraft per model. Each model of aircraft appears in the list once.
     * Note, each sublist is guaranteed to contain at least one element.
     *
     * @param nation The nation.
     * @return A unique list of aircraft that represent the aircraft models present at this airbase.
     */
    public List<Aircraft> getAircraftModelsPresent(final Nation nation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .map(Squadron::getAircraft)
                .collect(Collectors.toMap(Aircraft::getModel, ListUtil::createList, ListUtils::union))
                .values()
                .stream()
                .map(aircraft -> aircraft.get(0))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Indicates if this airbase has any squadrons.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    public boolean areSquadronsPresent() {
        return !squadrons.isEmpty();
    }

    /**
     * Remove a squadron from the airbase.
     *
     * @param squadron The squadron to remove.
     */
    public void remove(final Squadron squadron) {
        squadrons.remove(squadron);

        squadronMap
                .get(squadron.getType())
                .remove(squadron);

        squadronNameMap.remove(squadron.getName());

        squadron.setHome(null);
    }

    /**
     * Station a squadron at this airfield.
     *
     * @param squadron The squadron that is stationed.
     */
    public void add(final Squadron squadron) {
        squadrons.add(squadron);

        squadronMap
                .get(squadron.getType())
                .add(squadron);

        squadronNameMap.put(squadron.getName(), squadron);

        squadron.setHome(airbase);
    }

    /**
     * Remove all of the given nation's squadrons.
     *
     * @param nation The nation BRITISH, ITALIAN, etc...
     */
    public void clear(final Nation nation) {
        List<Squadron> toBeRemoved = getSquadrons(nation);
        squadrons.removeAll(toBeRemoved);

        toBeRemoved
                .stream()
                .peek(squadron -> squadronMap.get(squadron.getType()).remove(squadron))
                .map(Squadron::getName)
                .forEach(squadronNameMap::remove);
    }


    /**
     * Filter the given squadrons by nation.
     *
     * @param nation A nation.
     * @param squadronList A list of squadrons.
     * @return A list of squadrons that are owned by the given nation.
     */
    private List<Squadron> filterNation(final Nation nation, final List<Squadron> squadronList) {
        return squadronList
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Filter the given squadrons by nation and state.
     *
     * @param nation The nation.
     * @param state A squadron state.
     * @param squadronsOfType A list of squadrons of a particular type.
     * @return A list of squadrons that are at the given state for the given nation.
     */
    private List<Squadron> filterNationAndState(final Nation nation, final SquadronState state, final List<Squadron> squadronsOfType) {
        return squadronsOfType
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .filter(squadron -> squadron.isAtState(state))
                .collect(Collectors.toList());
    }
}
