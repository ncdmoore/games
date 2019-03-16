package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.allotment.AllotmentLoader;
import engima.waratsea.model.squadron.data.SquadronData;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class creates aircraft squadrons.
 */
@Singleton
@Slf4j
public class SquadronLoader {

    private AllotmentLoader allotmentLoader;
    private SquadronFactory factory;


    private Map<Nation, Allotment> allotmentMap = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param allotmentLoader Loads the squadron allotment data.
     * @param factory Builds squadrons.
     */
    @Inject
    public SquadronLoader(final AllotmentLoader allotmentLoader,
                          final SquadronFactory factory) {
        this.allotmentLoader = allotmentLoader;
        this.factory = factory;
    }

    /**
     * Load the given side's squadrons.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons for the given side and nation.
     */
    public List<Squadron> load(final Scenario scenario, final Side side, final Nation nation) {
        loadNationAllotment(scenario, side, nation);
        return buildSquadrons(side, nation);
    }

    /**
     * Load a given nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param side The side.
     * @param nation The nation.
     */
    private void loadNationAllotment(final Scenario scenario, final Side side, final Nation nation) {
        log.info("Load squadrons for scenario '{}', side: {}, nation: {}", new Object[]{scenario.getTitle(), side, nation});
        try {
            Allotment allotment = allotmentLoader.load(scenario, side, nation);
            allotmentMap.put(nation, allotment);
        } catch (SquadronException ex) {
            log.error("Unable to load squadron for scenario: '" + scenario + "' side: '" + side + "' nation: '" + nation + "'");
        }
    }

    /**
     * Build the squadrons for the given side and nation.
     *
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons.
     */
    private List<Squadron> buildSquadrons(final Side side, final Nation nation) {
        Squadron.init(side);
        return allotmentMap
                .get(nation)
                .get()
                .map(SquadronData::new)
                .map(data -> factory.create(side, data))
                .collect(Collectors.toList());
    }
}
