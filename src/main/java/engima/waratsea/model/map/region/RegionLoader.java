package engima.waratsea.model.map.region;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
    private static final String MAP_DIRECTORY_NAME = "/maps";

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
     * Load the task force for the given scenario and side.
     * @param scenario The scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of task forces.
     * @throws MapException if the scenario task force cannot be loaded.
     */
    public List<Region> loadRegions(final Scenario scenario, final Side side) throws MapException {
        String path = gameTitle.getValue() + "/" + MAP_DIRECTORY_NAME + "/" + scenario.getMap() + FILE_NAME_MAP.get(side);
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(u -> readRegions(scenario.getName(), u, side))
                .orElseThrow(() -> new MapException("Unable to load map for '" + scenario.getTitle() + "' for " + side));
    }

    /**
     * Read the region data from map json files for the given side.
     * @param scenarioName Name of the scenario.
     * @param url specifies the map json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @return returns a list of region objects.
     */
    private List<Region> readRegions(final String scenarioName, final URL url, final Side side) {
        Path path = Paths.get(url.getPath());

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type collectionType = new TypeToken<List<RegionData>>() { }.getType();

            Gson gson = new Gson();
            List<RegionData> regions = gson.fromJson(br, collectionType);

            log.info("Scenario: '{}' load map regions for side: {}, number regions: {}", new Object[] {scenarioName, side, regions.size()});

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
