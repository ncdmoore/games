package engima.waratsea.model.squadron.allotment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.allotment.data.AllotmentData;
import engima.waratsea.model.squadron.allotment.data.AllotmentTableData;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        final List<Integer> optionalDice = getOptionalDice(data, dice);

        log.debug("Nation allotment: '{}'", nation);

        log.debug("Bombers:");
        log.debug("Bomber optional dice: {}", optionalDice.get(0));
        bombers = Optional.ofNullable(data.getBombers())
                .map(bomberData -> addOptional(bomberData, optionalDice.get(0)))
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());

        log.debug("Fighters:");
        log.debug("Fighter optional dice: {}", optionalDice.get(1));
        fighters = Optional.ofNullable(data.getFighters())
                .map(fighterData -> addOptional(fighterData, optionalDice.get(1)))
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());

        log.debug("Recon:");
        log.debug("Recon optional dice: {}", optionalDice.get(2));
        recon = Optional.ofNullable(data.getRecon())
                .map(reconData -> addOptional(reconData, optionalDice.get(2)))
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());
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
    private List<Integer> getOptionalDice(final AllotmentData data,
                                          final Dice dice) {
        int needed = getNumDiceNeeded(data);   // The needed value matches the types of aircraft in the allotment.

        int maxOptionalDice = data.getMaxOptionalDice();

        List<Integer> optionalDice = new ArrayList<>();

        if (needed <= 1) {
            // If only one type of aircraft is needed, then the max optional number of dice
            // is simply used for this one type of aircraft.
            optionalDice.add(maxOptionalDice);
        } else if (needed <= 2) {
            // Two types of aircraft are needed. Get a random number between 1 and the max optional dice
            // for  one of the types. The other type is what remains.
            int optDice1 = getOptionalDice(dice, maxOptionalDice);
            int optDice2 = maxOptionalDice - optDice1;

            optionalDice.add(optDice1);
            optionalDice.add(optDice2);
        } else {
            // Three types of aircraft are needed. Get two random numbers. The second random number is
            // constrained by the max optional dice number minus what was selected for the first random
            // number. The third type is what remains.
            int optDice1 = getOptionalDice(dice, maxOptionalDice);
            int optDice2 = getOptionalDice(dice, maxOptionalDice - optDice1);
            int optDice3 = maxOptionalDice - (optDice1 + optDice2);

            optionalDice.add(optDice1);
            optionalDice.add(optDice2);
            optionalDice.add(optDice3);
        }

        return optionalDice;
    }

    /**
     * Determine how many optional dice are needed.
     *
     * @param data The allotment data.
     * @return The number of optional dice needed.
     */
    private int getNumDiceNeeded(final AllotmentData data) {
        int needed = 0;
        if (data.getBombers() != null) { // The allotment contains bombers, so add an optional dice number for bombers.
            needed++;
        }

        if (data.getFighters() != null) { // The allotment contains fighters, so add an optional dice number for fighters.
            needed++;
        }

        if (data.getRecon() != null) { // the allotment contains reconnaissance, so add an optional dice number for recon.
            needed++;
        }

        // The needed value will range between 0 and 3.
        return needed;
    }
}
