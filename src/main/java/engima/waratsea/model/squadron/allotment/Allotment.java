package engima.waratsea.model.squadron.allotment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.allotment.data.AllotmentData;
import engima.waratsea.model.squadron.allotment.data.AllotmentTableData;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.utility.Dice;
import javafx.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a nations squadron allotment.
 *
 * There are three types of aircraft allotments:
 *   Bombers
 *   Fighters
 *   Reconnaissance
 *
 * Each type may have an optional number of "extra" dice rolled.
 * This is specified by an overall number of optional dice. This
 * number of of optional (extra) dice is spread over the three
 * types of aircraft.
 *
 * An allotment is a collection of SquadronData for the three
 * types of aircraft. Squadrons are then built from this data.
 * Thus, essentially an allotment is just the collection of
 * squadron data for a given nation.
 */
@Slf4j
public class Allotment {
    private final AllotmentTableFactory factory;

    @Getter
    private final Nation nation;

    @Getter
    private final List<SquadronData> bombers;

    @Getter
    private final List<SquadronData> fighters;

    @Getter
    private final List<SquadronData> recon;

    private static final Map<String, Function<AllotmentData, AllotmentTableData>> FUNCTION_MAP = Map.of(
        "bombers", AllotmentData::getBombers,
        "fighters", AllotmentData::getFighters,
        "recon", AllotmentData::getRecon
    );

    /**
     * Constructor called by guice.
     *
     * @param data Allotment data read in from a JSON file.
     * @param factory Allotment table factory.
     * @param dice A utility for rolling dice.
     */
    @Inject
    public Allotment(@Assisted final AllotmentData data,
                               final AllotmentTableFactory factory,
                               final Dice dice) {
        this.factory = factory;

        nation = data.getNation();
        final Map<String, Integer> optionalDice = getOptionalDice(data, dice);

        log.debug("Nation allotment: '{}'", nation);

        bombers = getSquadronData("bombers", optionalDice, data);
        fighters = getSquadronData("fighters", optionalDice, data);
        recon = getSquadronData("recon", optionalDice, data);
    }

    /**
     * Get all the squadron types.
     *
     * @return A stream of all the squadron types.
     */
    public Stream<SquadronData> get() {
        return Stream.of(bombers, fighters, recon).flatMap(Collection::stream);
    }

    /**
     * Get the squadron data.
     *
     * @param type The type of aircraft. Bombers, fighters, recon, etc.
     * @param optionalDice The optional dice map.
     * @param data The allotment data.
     * @return The squadron data.
     */
    private List<SquadronData> getSquadronData(final String type, final Map<String, Integer> optionalDice, final AllotmentData data) {
        log.debug(type + ":");
        log.debug(type + " optional dice: {}", optionalDice.getOrDefault(type, 0));
        return Optional.ofNullable(FUNCTION_MAP.get(type).apply(data))
                .map(bomberData -> addOptional(bomberData, optionalDice.getOrDefault(type, 0)))
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());
    }

    /**
     * Add the maximum number of optional dice to the allotment table data.
     *
     * @param data The allotment table data.
     * @param optionalDice The allotment's optional dice.
     * @return The allotment table data with the added maximum optional dice number.
     */
    private AllotmentTableData addOptional(final AllotmentTableData data, final int optionalDice) {
        data.setOptionalDice(optionalDice);
        return data;
    }

    /**
     * Get the number of optional dice. The number of optional dice is in the
     * range of 1 - numOptionalDice.
     *
     * @param dice A utility for rolling dice.
     * @param numOptionalDice Maximum number of optional dice.
     * @return The number of optional dice.
     */
    private int getOptionalDice(final Dice dice, final int numOptionalDice) {
        return numOptionalDice <= 0 ? 0 : dice.roll(numOptionalDice);
    }

    /**
     * Get the squadrons from the allotment table.
     *
     * @param data The allotment table data.
     * @return A list of squadron models.
     */
    private List<SquadronData> getSquadrons(final AllotmentTableData data) {
        return factory.create(nation, data).getSquadrons();
    }

    /**
     * Get the optional dice for each type of aircraft allotted. Given the maximum
     * number of optional dice (from the allotment json) an optional number of dice for
     * each type of aircraft is calculated.
     *
     * Note, that an optional number of dice for a given aircraft type is only calculated
     * if the allotment contains that type of aircraft. Thus, if all type of aircraft
     * are present, 3 optional dice are calculated. If only two types are present then
     * 2 optional dice are calculated and so on.
     *
     * @param data The allotment data.
     * @param dice A utility for rolling dice.
     * @return A list that contains numbers of optional dice.
     */
    private Map<String, Integer> getOptionalDice(final AllotmentData data,
                                                 final Dice dice) {
        List<String> needed = getNumDiceNeeded(data);   // The needed value matches the types of aircraft in the allotment.

        int maxOptionalDice = data.getMaxOptionalDice();

        Map<String, Integer> optionalDice = new HashMap<>();

        int optDice = 0;
        for (int i = 1; i < needed.size(); i++) {
            int numDice = getOptionalDice(dice, maxOptionalDice - optDice);
            optionalDice.put(needed.get(i - 1), numDice);
            optDice += numDice;
        }

        int numDice = maxOptionalDice - optDice;
        optionalDice.put(needed.get(needed.size() - 1), numDice);

        return optionalDice;
    }

    /**
     * Determine how many optional dice are needed based on the types of squadrons that are included in the
     * allotment.
     *
     * @param data The allotment data.
     * @return A list of squadron types that are present in this allotment.
     */
    private List<String> getNumDiceNeeded(final AllotmentData data) {
        return FUNCTION_MAP
                .keySet()
                .stream()
                .map(type -> new Pair<>(type, FUNCTION_MAP.get(type).apply(data)))
                .filter(this::isSquadronTypePresent)
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Determine if the squadron type is present in the allotment.
     *
     * @param pair This pair has the squadron type as key and the corresponding allotment table data as the value.
     * @return True if this type of squadron is present in the allotment.
     */
    private boolean isSquadronTypePresent(final Pair<String, AllotmentTableData> pair) {
        return pair.getValue() != null && pair.getValue().isPresent();
    }
}
