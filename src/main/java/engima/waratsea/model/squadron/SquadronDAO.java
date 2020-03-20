package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.allotment.AllotmentDAO;
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
public class SquadronDAO {

    private AllotmentDAO allotmentDAO;
    private SquadronFactory factory;

    private Map<Nation, Allotment> allotmentMap = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param allotmentDAO Loads the squadron allotment data.
     * @param factory Builds squadrons.
     */
    @Inject
    public SquadronDAO(final AllotmentDAO allotmentDAO,
                       final SquadronFactory factory) {
        this.allotmentDAO = allotmentDAO;
        this.factory = factory;
    }

    /**
     * Load the given side and nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons for the given side and nation.
     */
    public List<Squadron> load(final Scenario scenario, final Side side, final Nation nation) {
        return loadNew(scenario, side, nation);
    }

    /**
     *  Build a given side and nation's squadron.
     *
     * @param data The squadron data.
     * @return The squadron.
     */
    public Squadron build(final SquadronData data) {
        return factory.create(data.getSide(), data.getNation(), data);
    }

    /**
     * Load the given side's squadrons. This is for a new game and the squadrons are created
     * from the squadron allotment.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons for the given side and nation.
     */
    private List<Squadron> loadNew(final Scenario scenario, final Side side, final Nation nation) {
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
        log.debug("Load squadrons for scenario '{}', side: {}, nation: {}", new Object[]{scenario.getTitle(), side, nation});
        try {
            Allotment allotment = allotmentDAO.load(scenario, side, nation);
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
                .map(data -> factory.create(side, nation, data))
                .collect(Collectors.toList());
    }
}
