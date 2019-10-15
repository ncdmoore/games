package engima.waratsea.model.squadron.deployment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.squadron.SquadronProps;
import engima.waratsea.model.squadron.deployment.data.DeploymentData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class loads the squadron deployment data from JSON files.
 */
@Slf4j
@Singleton
public class SquadronDeploymentDAO {
    private Resource config;
    private SquadronProps props;
    private SquadronDeploymentFactory deploymentFactory;

    /**
     * The constructor. Called by guice.
     *
     * @param config The game's config.
     * @param props The squadron properties.
     * @param deploymentFactory a deployment factory.
     */
    @Inject
    public SquadronDeploymentDAO(final Resource config,
                                 final SquadronProps props,
                                 final SquadronDeploymentFactory deploymentFactory) {
        this.config = config;
        this.props = props;
        this.deploymentFactory = deploymentFactory;
    }

    /**
     * Load the deployment for the given scenario, side and nation.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return The squadron deployment of a nation.
     * @throws SquadronException if the squadron deployment cannot be loaded.
     */
    public List<SquadronDeployment> load(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
        List<SquadronDeployment> deployment = loadScenarioSpecific(scenario, side, nation);
        return deployment.isEmpty() ? loadDefault(scenario, side, nation) : deployment;
    }

    /**
     * Load the scenario specific region files if they exists. It is normal if they do not. Not all scenarios
     * override the regions.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return The squadron deployment of a nation.
     */
    private List<SquadronDeployment> loadScenarioSpecific(final Scenario scenario, final Side side, final Nation nation)  {
        String year = getAllotmentYear(scenario);

        log.info("load specific squadron deployment, scenario: '{}', side: '{}', nation: '{}', year: '{}'", new Object[]{scenario.getTitle(), side.toLower(), nation.toString(), year});
        List<SquadronDeployment> deployment = config
                .getScenarioURL(side, SquadronDeployment.class, year + "/" + side.toLower() + "/" + nation.toString() + ".json")
                .map(url -> readDeployment(url, side, nation))
                .orElseGet(Collections::emptyList);

        log.info("load specific squadron deployment scenario: '{}', side: '{}', nation: '{}', year: '{}', success: {}",
                new Object[]{scenario.getTitle(), side, nation, year, !deployment.isEmpty()});
        return deployment;
    }

    /**
     * Load the default deployment for the game for the given scenario, side and nation.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return The squadron deployment of a nation.
     * @throws SquadronException if the squadron deployment cannot be loaded.
     */
    private List<SquadronDeployment> loadDefault(final Scenario scenario, final Side side, final Nation nation) throws SquadronException {
        String year = getAllotmentYear(scenario);

        log.info("Load squadron deployment, scenario: '{}', side: '{}', nation: '{}', year: '{}'", new Object[]{scenario.getTitle(), side.toLower(), nation.toString(), year});
        return config.getGameURL(side, SquadronDeployment.class, year + "/" + side.toLower() + "/" + nation.toString() + ".json")
                .map(url -> readDeployment(url, side, nation))
                .orElseThrow(() -> new SquadronException("Unable to load deployment for " + scenario.getTitle() + " for " + side.toLower() + " nation: " + nation + " year: " + year));
    }

    /**
     * Read the deployment data from json files for the given side.
     *
     * @param url specifies the task force json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @param nation specifies the nation: BRITISH, ITALIAN, GERMAN, etc...
     * @return returns a squadron deployment.
     */
    private List<SquadronDeployment> readDeployment(final URL url, final Side side, final Nation nation) {
        try {
            Path path = Paths.get(url.toURI().getPath()); // Use the URI to support file names with spaces.

            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Type collectionType = new TypeToken<List<DeploymentData>>() { }.getType();

                Gson gson = new Gson();
                List<DeploymentData> deploymentData = gson.fromJson(br, collectionType);

                log.info("load deployment for side: {}, nation: {}", side, nation);

                return createDeployments(side, deploymentData);
            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
                log.error("Unable to load deployment: {}", url.getPath(), ex);
                return null;
            }
        } catch (URISyntaxException ex) {
            log.error("Bad URI Unable to load deployment: {}", url.getPath(), ex);
            return null;
        }
    }

    /**
     * Seed the task forces with the data from the JSON file.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Task force data from the JSON file.
     * @return An initialized or seeded Task Force.
     */
    private List<SquadronDeployment> createDeployments(final Side side, final List<DeploymentData> data) {
        return data.stream()
                .map(deploymentData -> deploymentFactory.create(side, deploymentData))
                .collect(Collectors.toList());
    }

    /**
     * Get the deployment year.
     *
     * @param scenario The selected scenario.
     * @return The deployment year for the given selected scenario.
     */
    private String getAllotmentYear(final Scenario scenario) {
        return props.getString(scenario.getMonthYear("-"));
    }
}
