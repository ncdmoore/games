package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.base.airfield.patrol.data.PatrolsData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import javafx.util.Pair;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an airfield's or aircraft carrier's patrols.
 */
public class Patrols {
    private final Map<PatrolType, Patrol> patrolMap = new HashMap<>();

    private final PatrolDAO patrolDAO;
    private Airbase airbase;

    /**
     * Constructor called by guice.
     *
     * @param patrolDAO Patrol data access object.
     */
    @Inject
    public Patrols(final PatrolDAO patrolDAO) {
        this.patrolDAO = patrolDAO;
    }

    /**
     * Build the patrols.
     *
     * @param base The airbase of these patrols.
     * @param data The patrols data.
     */
    public void build(final Airbase base, final PatrolsData data) {
        this.airbase = base;
        PatrolType
                .stream()
                .forEach(patrolType -> buildPatrol(patrolType, data));
    }

    /**
     * Get the patrols persistent data.
     *
     * @return The patrols persistent data.
     */
    public PatrolsData getData() {
        PatrolsData data = new PatrolsData();

        Map<PatrolType, PatrolData> map =
                patrolMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        this::getData));

        data.setPatrols(map);

        return data;
    }

    /**
     * Clear all squadrons on patrol.
     */
    public void clear() {
        patrolMap.forEach((patrolType, patrol) -> patrol.clearSquadrons());
    }

    /**
     * Clear all the given nation's squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    public void clear(final Nation nation) {
        patrolMap.forEach(((patrolType, patrol) -> patrol.clearSquadrons(nation)));
    }

    /**
     * Update the given patrol with the given list of squadrons.
     *
     * @param patrolType The patrol type.
     * @param squadrons The squadrons on the patrol.
     */
    public void update(final PatrolType patrolType, final List<Squadron> squadrons) {
        Patrol patrol = patrolMap.get(patrolType);
        squadrons.forEach(patrol::addSquadron);
    }

    /**
     * Get a given patrol specified by the given type.
     *
     * @param patrolType The patrol type.
     * @return The patrol corresponding to the given type.
     */
    public Patrol getPatrol(final PatrolType patrolType) {
        return patrolMap.get(patrolType);
    }

    /**
     * Get a map of patrol maximum radius to list of patrols.
     *
     * @return A map containing the patrol maximum radius as key and the patrol as the value.
     * Note, that if all the patrol types have the same maximum radius value then this map
     * will contain a single entry where the single maximum radius maps to all three types
     * of patrols.
     */
    public Map<Integer, List<Patrol>> getPatrolRadiiMap() {
        return Stream.of(PatrolType.values())
                .map(this::getPatrolRadii)
                .collect(Collectors.toMap(Pair::getKey,
                        this::createList,
                        ListUtils::union));
    }

    /**
     * Build a patrol.
     *
     * @param patrolType The patrol type to build.
     * @param patrolsData The patrol's data.
     */
    private void buildPatrol(final PatrolType patrolType, final PatrolsData patrolsData) {

        PatrolData patrolData = Optional
                .ofNullable(patrolsData)
                .map(PatrolsData::getPatrols)
                .map(data -> data.get(patrolType))
                .orElseGet(PatrolData::new);

        patrolData.setAirbase(airbase);
        patrolData.setType(patrolType);
        patrolMap.put(patrolType, patrolDAO.load(patrolData));
    }

    /**
     * Get a given patrol entry's data. This is just a utility method to aid in
     * getting a given patrols's persistent data.
     *
     * @param entry a map entry with patrol type as key and the patrol as the value.
     * @return The patrol's persistent data.
     */
    private PatrolData getData(final Map.Entry<PatrolType, Patrol> entry) {
        return entry.getValue().getData();
    }

    /**
     * Get a Pair of max radius to patrol.
     *
     * @param patrolType The type of patrol.
     * @return A Pair of radius, patrol.
     */
    private Pair<Integer, Patrol> getPatrolRadii(final PatrolType patrolType) {
        int radius = patrolMap
                .get(patrolType)
                .getTrueMaxRadius();

        return new Pair<>(radius, patrolMap.get(patrolType));
    }

    /**
     * Create a list of Patrols from a pair of radius, patrol.
     *
     * @param pair  A radius, patrol pair.
     * @return A list of Patrols.
     */
    private  List<Patrol> createList(final Pair<Integer, Patrol> pair) {
        Patrol patrol = pair.getValue();
        List<Patrol> list = new ArrayList<>();
        list.add(patrol);
        return list;
    }
}
