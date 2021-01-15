package engima.waratsea.model.squadron.allotment;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.squadron.SquadronProps;
import engima.waratsea.model.squadron.allotment.data.AllotmentData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class loads the squadron allotment data from JSON files.
 */
@Slf4j
@Singleton
public class AllotmentDAO {
    private final Resource config;
    private final SquadronProps props;
    private final AllotmentFactory allotmentFactory;

    /**
     * The constructor. Called by guice.
     *
     * @param config The game's config.
     * @param props The squadron properties.
     * @param allotmentFactory an allotment factory.
     */
    @Inject
    public AllotmentDAO(final Resource config,
                        final SquadronProps props,
                        final AllotmentFactory allotmentFactory) {
        this.config = config;
        this.props = props;
        this.allotmentFactory = allotmentFactory;
    }

    /**
     * Load the allotment for the given scenario, side and nation.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return The squadron allotment of a nation.
     * @throws SquadronException if the squadron allotment cannot be loaded.
     */
    public Allotment load(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
        Allotment allotment = loadScenarioSpecific(scenario, side, nation);
        return allotment != null ? allotment : loadDefault(scenario, side, nation);
    }

    /**
     * Load the scenario specific region files if they exists. It is normal if they do not. Not all scenarios
     * override the regions.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return The squadron allotment of a nation.
     */
    private Allotment loadScenarioSpecific(final Scenario scenario, final Side side, final Nation nation)  {
        String year = getAllotmentYear(scenario);

        log.info("load specific squadron allotments, scenario: '{}', side: '{}', nation: '{}', year: '{}'", new Object[]{scenario.getTitle(), side.toLower(), nation.toString(), year});
        Allotment allotment = config
                .getScenarioURL(side, Allotment.class, year + "/" + side.toLower() + "/" + nation.toString() + ".json")
                .map(url -> readAllotment(url, side))
                .orElse(null);

        log.info("load specific squadron allotments scenario: '{}', side: '{}', nation: '{}', year: '{}', success: {}",
                new Object[]{scenario.getTitle(), side, nation, year, allotment != null});
        return allotment;
    }

    /**
     * Load the default allotment for the game for the given scenario, side and nation.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return The squadron allotment of a nation.
     * @throws SquadronException if the squadron allotment cannot be loaded.
     */
    private Allotment loadDefault(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
        String year = getAllotmentYear(scenario);

        log.info("Load squadron allotments, scenario: '{}', side: '{}', nation: '{}', year: '{}'", new Object[]{scenario.getTitle(), side.toLower(), nation.toString(), year});
        return config.getGameURL(side, Allotment.class, year + "/" + side.toLower() + "/" + nation.toString() + ".json")
                .map(url -> readAllotment(url, side))
                .orElseThrow(() -> new SquadronException("Unable to load allotment for " + scenario.getTitle() + " for " + side.toLower() + " nation: " + nation + " year: " + year));
    }

    /**
     * Read the allotment data from json files for the given side.
     *
     * @param url specifies the task force json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @return returns a squadron allotment.
     */
    private Allotment readAllotment(final URL url, final Side side) {
        try {
            Path path = Paths.get(url.toURI().getPath()); // Use the URI to support file names with spaces.

            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                AllotmentData allotmentData = gson.fromJson(br, AllotmentData.class);

                log.info("load allotment for side: {}, nation: {}", side, allotmentData.getNation());

                return allotmentFactory.create(side, allotmentData);
            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
                log.error("Unable to load allotment: {}", url.getPath(), ex);
                return null;
            }
        } catch (URISyntaxException ex) {
            log.error("Bad URI Unable to load allotment: {}", url.getPath(), ex);
            return null;
        }
    }

    /**
     * Get the allotment year.
     *
     * @param scenario The selected scenario.
     * @return The allotment year for the given selected scenario.
     */
    private String getAllotmentYear(final Scenario scenario) {
        return props.getString(scenario.getMonthYear("-"));
    }
}
