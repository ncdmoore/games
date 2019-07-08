package engima.waratsea.model.flotilla;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.flotilla.data.FlotillaData;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Loads and saves persistent flotialla data.
 */
@Singleton
@Slf4j
public class FlotillaDAO {
    private Config config;
    private FlotillaFactory flotillaFactory;

    /**
     * The constructor. Called by guice.
     *
     * @param config The game's config.
     * @param flotillaFactory Factory for creating flotilla objects.
     */
    @Inject
    public FlotillaDAO(final Config config,
                        final FlotillaFactory flotillaFactory) {
        this.config = config;
        this.flotillaFactory = flotillaFactory;
    }

    /**
     * Load the flotillas for the given scenario and side.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of task forces.
     */
    public List<Flotilla> load(final Scenario scenario, final Side side) {
        log.info("Load flotilla, scenario: '{}', side: '{}'", scenario.getTitle(), side);
        return getURL(side)
                .map(u -> readFlotilla(u, side))
                .orElseGet(() -> logWarn(scenario, side));
    }

    /**
     * Get the flotilla URL.
     *
     * @param side The side ALLIES or AXIS.
     * @return The flotilla URL.
     */
    private Optional<URL> getURL(final Side side) {
        return config.isNew() ? config.getScenarioURL(side, Flotilla.class) // Get the new game task forces.
                : config.getSavedURL(side, Flotilla.class);                 // Get a saved game task forces.
    }

    /**
     * Save the flotilla. The allies and axis flotilla data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side AlLIES of AXIS.
     * @param flotillas The flotilla data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<Flotilla> flotillas) {
        log.info("Saving flotillas, scenario: '{}',side {}", scenario.getTitle(), side);
        String fileName = config.getSavedFileName(side, Flotilla.class);
        PersistentUtility.save(fileName, flotillas);
    }

    /**
     * Read the flotilla data from scenario flotilla json files for the given side.
     *
     * @param url specifies the flotilla json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @return returns a list of flotilla objects.
     */
    private List<Flotilla> readFlotilla(final URL url, final Side side) {
        Path path = Paths.get(url.getPath());

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type collectionType = new TypeToken<List<FlotillaData>>() { }.getType();

            Gson gson = new Gson();
            List<FlotillaData> flotillas = gson.fromJson(br, collectionType);

            log.debug("load flotilla for side: {}, number of flotillas: {}", side, flotillas.size());

            return seedFlotilla(side, flotillas);
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load flotilla: {}", url.getPath(), ex);
            return null;
        }
    }

    /**
     * Seed the flotilla with the data from the JSON file.
     *
     * @param side The side of the flotilla. ALLIES or AXIS.
     * @param data Flotilla data from the JSON file.
     * @return An initialized or seeded Flotilla.
     */
    private List<Flotilla> seedFlotilla(final Side side, final List<FlotillaData> data) {
        return data.stream()
                .map(flotillaData -> flotillaFactory.create(side, flotillaData))
                .collect(Collectors.toList());
    }

    /**
     * Log a warning message that no flotilla's for the given side could be loaded.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return An empty list.
     */
    private List<Flotilla> logWarn(final Scenario scenario, final Side side) {
        log.warn("Unable to load flotilla for scenario: '{}', side: {}", scenario.getTitle(), side);
        return Collections.emptyList();
    }
}
