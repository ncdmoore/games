package engima.waratsea.model.map.region;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class loads the map region data from the region json files.
 *
 * Each scenario specifies a map that consists of several json files.
 * The maps/allies/{date}.json file contains the allied bases (airfields and ports) available for this scenario.
 * The maps/axis/{date}.json file contains the axis bases (airfields and ports) available for this scenario.
 */
@Slf4j
@Singleton
public class RegionLoader {
    private Config confg;
    private RegionFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config
     * @param factory Factory for creating region objects.
     */
    @Inject
    public RegionLoader(final Config config,
                        final RegionFactory factory) {
        this.confg = config;
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
        List<Region> regions = confg
                .getScenarioURL(side, Region.class)
                .map(url -> readRegions(scenario, url, side))
                .orElse(null);

        log.info("Scenario: '{}' load specific regions, success: {}", new Object[]{scenario.getTitle(), regions != null});
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
        log.info("Load regions for scenario: {}, side: {}", scenario.getTitle(), side);
        return confg
                .getGameURL(side, Region.class, scenario.getMap() + ".json")
                .map(url -> readRegions(scenario, url, side))
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
