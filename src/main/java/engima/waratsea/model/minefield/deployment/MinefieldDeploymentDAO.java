package engima.waratsea.model.minefield.deployment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.deployment.data.DeploymentData;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the minefield deployment data abstraction object.
 */
@Slf4j
public class MinefieldDeploymentDAO {
    private Config config;

    private MinefieldDeploymentFactory minefieldDeploymentFactory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param minefieldDeploymentFactory The minefield deployment factory.
     */
    @Inject
    public MinefieldDeploymentDAO(final Config config,
                                  final MinefieldDeploymentFactory minefieldDeploymentFactory) {
        this.config = config;
        this.minefieldDeploymentFactory = minefieldDeploymentFactory;
    }

    /**
     * Load all of the given sides's minefields.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of minefields.
     */
    public List<MinefieldDeployment> load(final Side side) {
        log.info("Load minefield deployment for side: {}", side);
        return loadDeploymentData(side)
                .stream()
                .map(minefieldDeploymentFactory::create)
                .collect(Collectors.toList());
    }

    /**
     * Load all of the given side's minefields.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of minefield data.
     */
    private List<DeploymentData> loadDeploymentData(final Side side) {
        return getMinefieldUrl(side)
                .map(url -> readMinefield(url, side))
                .orElseGet(() -> logWarn(side));
    }

    /**
     * Get the minefield URL.
     *
     * @param side The side ALLIES or AXIS.
     * @return The minefield URL.
     */
    private Optional<URL> getMinefieldUrl(final Side side) {
        return config.getScenarioURL(side, MinefieldDeployment.class);
    }

    /**
     * Read the minefield data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param side The side ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private List<DeploymentData> readMinefield(final URL url, final Side side) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Type collectionType = new TypeToken<List<DeploymentData>>() { }.getType();

                Gson gson = new Gson();
                List<DeploymentData> deploymentData = gson.fromJson(br, collectionType);

                log.debug("load minefields for side {}", side);

                return deploymentData;
            } catch (Exception ex) {                                                                                    // Catch any Gson errors.
                log.error("Unable to load minefields '{}' for side: {}. {}", new Object[]{url.getPath(), side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load minefields '{}' for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Log an error for minefield deployments that cannot be loaded.
     *
     * @param side The side ALLIES or AXIS.
     * @return Empty list.
     */
    private List<DeploymentData> logWarn(final Side side) {
        log.warn("Unable to load minefield deployment for side: {}", side);
        return Collections.emptyList();
    }
}
