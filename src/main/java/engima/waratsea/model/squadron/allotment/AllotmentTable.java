package engima.waratsea.model.squadron.allotment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.squadron.allotment.data.AllotmentTableData;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a given type of aircraft class (bombers, fighters or recon) allotment table.
 */
@Slf4j
public class AllotmentTable {

    @Getter
    @Setter
    private List<AllotmentGroup> groups;

    @Getter
    private final List<String> squadrons = new ArrayList<>(); // The total number of squadrons for this type of aircraft: bombers, fighters or recon.

    /**
     * The constructor called by guice.
     *
     * @param data The allotment table data read in from a JSON file.
     * @param dice A utility for rolling dice.
     */
    @Inject
    public AllotmentTable(@Assisted final AllotmentTableData data,
                                    final Dice dice) {

        groups = data.getGroups()
                .stream()
                .map(AllotmentGroup::new)
                .collect(Collectors.toList());


        int numberOfSteps = dice.sumDiceRoll(data.getDice()) + data.getFactor();

        if (numberOfSteps % 2 != 0) {
            numberOfSteps++;
        }

        int numberOfSquadrons = numberOfSteps / 2;

        log.info("Number of squadrons: {}", numberOfSquadrons);

        // loop through the groups picking squadrons from each group.
        int neededSquadrons = numberOfSquadrons;
        while (neededSquadrons > 0) {
            for (AllotmentGroup group : groups) {
                List<String> selected = group.select(neededSquadrons);
                neededSquadrons -= selected.size();
                squadrons.addAll(selected);
            }
        }

        squadrons.forEach(squadron -> log.info("Squadron: {}", squadron));
    }
}