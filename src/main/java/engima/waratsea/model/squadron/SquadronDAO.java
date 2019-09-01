package engima.waratsea.model.squadron;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.allotment.AllotmentDAO;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private Config config;
    private AllotmentDAO allotmentDAO;
    private SquadronFactory factory;


    private Map<Nation, Allotment> allotmentMap = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param config The game config.
     * @param allotmentDAO Loads the squadron allotment data.
     * @param factory Builds squadrons.
     */
    @Inject
    public SquadronDAO(final Config config,
                       final AllotmentDAO allotmentDAO,
                       final SquadronFactory factory) {
        this.config = config;
        this.allotmentDAO = allotmentDAO;
        this.factory = factory;
    }

    /**
     * Load the given side's squadrons.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons for the given side and nation.
     * @throws ScenarioException indicating that the squadrons could not be loaded.
     */
    public List<Squadron> load(final Scenario scenario, final Side side, final Nation nation) throws ScenarioException {
        return config.isNew() ? loadNew(scenario, side, nation) : loadExisting(scenario, side, nation);
    }

    /**
     * Load the given side's squadrons. This is for a new game and the squadrons are created
     * from the squadron allotment.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons for the given side and nation.
     */
    private List<Squadron> loadNew(final Scenario scenario, final Side side, final Nation nation) {
        loadNationAllotment(scenario, side, nation);
        return buildSquadrons(side, nation);
    }

    /**
     * Load the given side's squadrons. This is for an existing game and the squadrons are read from JSON
     * data. No allotment data is needed.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @return A list of squadrons for the given side and nation.
     * @throws ScenarioException indicating that the squadrons could not be loaded.
     */
    private List<Squadron> loadExisting(final Scenario scenario, final Side side, final Nation nation) throws ScenarioException {
        log.info("Load squadrons for scenario: '{}', side: {}, nation: {}", new Object[]{scenario.getTitle(), side, nation});
        return config.getSavedURL(side, Squadron.class, nation + ".json")
                .map(url -> readSquadrons(url, side, nation))
                .orElseThrow(() -> new ScenarioException("Unable to load squadrons for " + scenario.getTitle() + " for " + side + " and " + nation));
    }

    /**
     * Save the squadrons. The allies and axis squadron data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side AlLIES of AXIS.
     * @param nation The nation.
     * @param squadrons The squadron data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final Nation nation, final List<Squadron> squadrons) {
        log.info("Saving squadrons, scenario: '{}', side: {}, nation: {}", new Object[]{scenario.getTitle(), side, nation});
        log.info("Saving {} squadrons", squadrons.size());
        String fileName = config.getSavedFileName(side, Squadron.class, nation + ".json");
        PersistentUtility.save(fileName, squadrons);
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

    /**
     * Read the task force data from scenario task force json files for the given side.
     *
     * @param url specifies the task force json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @param nation The nation.
     * @return returns a list of task force objects.
     */
    private List<Squadron> readSquadrons(final URL url, final Side side, final Nation nation) {
        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            Type collectionType = new TypeToken<List<SquadronData>>() { }.getType();

            Gson gson = new Gson();
            List<SquadronData> squadrons = gson.fromJson(br, collectionType);

            log.debug("load squadrons for side: {}, nation: {}, number of squadrons: {}", new Object[]{side, nation, squadrons.size()});

            return buildSquadrons(side, nation, squadrons);
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load squadrons: {}", url.getPath(), ex);
            return null;
        }
    }

    /**
     * Build the squadrons from data read in from a JSON file. This is used for saved games.
     *
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @param data The squadron data read in from the JSON file.
     * @return A list of squadrons.
     */
    private List<Squadron> buildSquadrons(final Side side, final Nation nation, final List<SquadronData> data) {
        return data
                .stream()
                .map(squadronData -> factory.create(side, nation, squadronData))
                .collect(Collectors.toList());
    }
}
