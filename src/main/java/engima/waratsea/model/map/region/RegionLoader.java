package engima.waratsea.model.map.region;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.map.region.data.RegionData;
import engima.waratsea.model.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class loads the map region data from the region json files.
 *
 * Each scenario specifies a map that consists of several json files.
 * The alliesBases.json file contains the allied bases (airfields and ports) available for this scenario.
 * The axisBases.json file contains the axis bases (airfields and ports) available for this scenario.
 */
@Slf4j
@Singleton
public class RegionLoader {
    private static final String ALLIES_BASES_FILE_NAME = "/alliesBases.json";
    private static final String AXIS_BASES_FILE_NAME = "/axisBases.json";

    private static final Map<Side, String> FILE_NAME_MAP = new HashMap<>();
    static {
        FILE_NAME_MAP.put(Side.ALLIES, ALLIES_BASES_FILE_NAME);
        FILE_NAME_MAP.put(Side.AXIS, AXIS_BASES_FILE_NAME);
    }

    private GameTitle gameTitle;
    private RegionFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param gameTitle The title of the game. Used to locate the region json files.
     * @param factory Factory for creating region objects.
     */
    @Inject
    public RegionLoader(final GameTitle gameTitle,
                        final RegionFactory factory) {
        this.gameTitle = gameTitle;
        this.factory = factory;
    }

    /**
     * Load the regions for the given scenario and side.
     *
     * @param scenario The scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of regions.
     * @throws MapException if the scenario regions cannot be loaded.
     */
    public List<Region> loadRegions(final Scenario scenario, final Side side) throws MapException {
        List<Region> regions = loadScenarioSpecific(scenario, side);      // Attempt to load scenario specific regions.
        return regions != null ? regions : loadDefault(scenario, side);   // If no specific secnario regions exist, load the default.
    }

    /**
     * Load the scenario specific region files if they exists. It is normal if they do not. Not all scenarios
     * override the regions.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of regions.
     */
    private List<Region> loadScenarioSpecific(final Scenario scenario, final Side side)  {
        String path = gameTitle.getValue() + Config.SCENARIO_DIRECTORY_NAME + "/" + scenario.getName() + "/" + FILE_NAME_MAP.get(side);
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        List<Region> regions = url.map(u -> readRegions(scenario, u, side)).orElse(null);
        log.info("Scenario: '{}' load specific regions: '{}', success: {}", new Object[]{scenario.getTitle(), path, regions != null});
        return regions;
    }

    /**
     * Load the region files for the given scenario and side.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of regions.
     * @throws MapException if the regions cannot be loaded.
     */
    private List<Region> loadDefault(final Scenario scenario, final Side side) throws MapException {
        String path = gameTitle.getValue() + Config.MAP_DIRECTORY_NAME + "/" + scenario.getMap() + FILE_NAME_MAP.get(side);
        log.info("Scenario: '{}' load default regions: '{}'", scenario.getTitle(), path);
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(u -> readRegions(scenario, u, side))
                .orElseThrow(() -> new MapException("Unable to load map for '" + scenario.getTitle() + "' for " + side));
    }

    /**
     * Read the region data from map json files for the given side.
     *
     * @param scenario The selected scenario.
     * @param url specifies the map json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @return returns a list of region objects.
     */
    private List<Region> readRegions(final Scenario scenario, final URL url, final Side side) {
        Path path = Paths.get(url.getPath());

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type collectionType = new TypeToken<List<RegionData>>() { }.getType();

            Gson gson = new Gson();
            List<RegionData> regions = gson.fromJson(br, collectionType);

            log.info("Scenario: '{}' load map regions for side: {}, number regions: {}", new Object[] {scenario.getTitle(), side, regions.size()});

            return regions
                    .stream()
                    .map(regionData -> factory.create(side, regionData))
                    .collect(Collectors.toList());
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load map regions: {}", url.getPath(), ex);
            return null;
        }
    }
}
