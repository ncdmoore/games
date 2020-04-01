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

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class creates aircraft squadrons.
 */
@Singleton
@Slf4j
public class SquadronDAO {
    private final AllotmentDAO allotmentDAO;
    private final SquadronFactory factory;

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
     * @throws SquadronException if the squadron cannot be loaded.
     */
    public List<Squadron> load(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
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
     * @throws SquadronException if the allotment cannot be loaded.
     */
    private List<Squadron> loadNew(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
        Allotment allotment = loadNationAllotment(scenario, side, nation);
        return buildSquadrons(side, nation, allotment);
    }

    /**
     * Load a given nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param side The side.
     * @param nation The nation.
     * @return The nation's squadron allotment.
     * @throws SquadronException if the allotment cannot be loaded.
     */
    private Allotment loadNationAllotment(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
        log.debug("Load squadrons for scenario '{}', side: {}, nation: {}", new Object[]{scenario.getTitle(), side, nation});
        return allotmentDAO.load(scenario, side, nation);
    }

    /**
     * Build the squadrons for the given side and nation.
     *
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @param allotment The nation's squadron allotment.
     * @return A list of squadrons.
     */
    private List<Squadron> buildSquadrons(final Side side, final Nation nation, final Allotment allotment) {
        Squadron.init(side);
        return allotment
                .get()
                .map(data -> factory.create(side, nation, data))
                .collect(Collectors.toList());
    }
}
