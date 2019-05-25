package engima.waratsea.model.flotilla.deployment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import engima.waratsea.model.flotilla.deployment.data.DeploymentData;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.ScenarioException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the flotilla deployment data abstraction object.
 */
@Slf4j
public class FlotillaDeploymentDAO {
    private Config config;

    private FlotillaDeploymentFactory flotillaDeploymentFactory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param flotillaDeploymentFactory The flotilla deployment factory.
     */
    @Inject
    public FlotillaDeploymentDAO(final Config config,
                                 final FlotillaDeploymentFactory flotillaDeploymentFactory) {
        this.config = config;
        this.flotillaDeploymentFactory = flotillaDeploymentFactory;
    }

    /**
     * Load all of the given sides's flotilla deployment.
     *
     * @param side The side ALLIES or AXIS.
     * @throws ScenarioException if the deployment cannot be loaded.
     * @return A list of flotilla deployment objects.
     */
    public List<FlotillaDeployment> load(final Side side) throws ScenarioException {
        log.info("Load flotilla deployment for side: {}", side);
        return loadDeploymentData(side)
                .stream()
                .map(flotillaDeploymentFactory::create)
                .collect(Collectors.toList());
    }

    /**
     * Load all of the given side's flotilla deployment data.
     *
     * @param side The side ALLIES or AXIS.
     * @throws ScenarioException if the deployment cannot be loaded.
     * @return A list of flotilla deployment data.
     */
    private List<DeploymentData> loadDeploymentData(final Side side) throws ScenarioException {
        return getFlotillaURL(side)
                .map(url -> readFlotilla(url, side))
                .orElseThrow(() -> new ScenarioException("Unable to load flotilla deployment for side:" + side));
    }

    /**
     * Get the flotilla data URL.
     *
     * @param side The side ALLIES or AXIS.
     * @return The flotilla data URL.
     */
    private Optional<URL> getFlotillaURL(final Side side) {
        return config.getScenarioURL(side, FlotillaDeployment.class);
    }

    /**
     * Read the flotilla data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param side The side ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private List<DeploymentData> readFlotilla(final URL url, final Side side) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Type collectionType = new TypeToken<List<DeploymentData>>() { }.getType();

                Gson gson = new Gson();
                List<DeploymentData> deploymentData = gson.fromJson(br, collectionType);

                log.info("load flotilla deployment for side {}, deployment size: {}", side, deploymentData.size());

                return deploymentData;
            } catch (Exception ex) {                                                                                    // Catch any Gson errors.
                log.error("Unable to load flotilla deployment '{}' for side: {}. {}", new Object[]{url.getPath(), side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load flotilla deployment '{}' for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }
}
