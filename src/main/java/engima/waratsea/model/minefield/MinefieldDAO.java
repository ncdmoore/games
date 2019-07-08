package engima.waratsea.model.minefield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.data.MinefieldData;
import engima.waratsea.model.minefield.zone.MinefieldZone;
import engima.waratsea.model.minefield.zone.MinefieldZoneFactory;
import engima.waratsea.model.minefield.zone.MinefieldZoneId;
import engima.waratsea.model.minefield.zone.data.MinefieldZoneData;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.PersistentUtility;
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
 * The data abstraction object for Minefields.
 *
 * The scenario specifies which minefield zones are used for the scenario.
 * For each zone listed in the scenario, a minefield is created.
 * The corresponding zone is then loaded and attached or added to a minefield.
 */
@Slf4j
@Singleton
public class MinefieldDAO {
    private Config config;
    private MinefieldZoneFactory zoneFactory;
    private MinefieldFactory minefieldFactory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param zoneFactory The minefield zone zoneFactory.
     * @param minefieldFactory The minefield zoneFactory.
     */
    @Inject
    public MinefieldDAO(final Config config,
                        final MinefieldZoneFactory zoneFactory,
                        final MinefieldFactory minefieldFactory) {
        this.config = config;
        this.zoneFactory = zoneFactory;
        this.minefieldFactory = minefieldFactory;
    }

    /**
     * Load all of the given sides's minefields.
     *
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of minefields.
     */
    public List<Minefield> load(final Side side) {
        log.info("Load minefields for side: {}", side);
        return loadMinefieldData(side)
                .stream()
                .map(minefieldFactory::create)
                .map(this::addZone)
                .collect(Collectors.toList());
    }

    /**
     * Save the minefields. The allies and axis minefield data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side AlLIES of AXIS.
     * @param minefields The minefield data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<Minefield> minefields) {
        log.info("Saving minefields, scenario: '{}',side {}", scenario.getTitle(), side);
        String fileName = config.getSavedFileName(side, Minefield.class);
        PersistentUtility.save(fileName, minefields);
    }

    /**
     * Add a zone to the minefield.
     *
     * @param minefield The minefield that corresponds to the zone.
     * @return The minefield zone.
     */
    private Minefield addZone(final Minefield minefield) {
        String name = minefield.getZoneName();
        Side side = minefield.getSide();

        log.info("Add minefield zone: '{}' for side: {}", name, side);

        MinefieldZoneId zoneId = new MinefieldZoneId(name, side);
        MinefieldZoneData data = loadMinefieldZoneData(zoneId);

        if (data != null) {
            MinefieldZone zone = buildZone(data);
            minefield.setZone(zone);
        } else {
            log.error("Unable to get minefield zone: '{}' for side: {}", name, side);
        }

        return minefield;
    }

    /**
     * Load all of the given side's minefields.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of minefield data.
     */
    private List<MinefieldData> loadMinefieldData(final Side side) {
        return getMinefieldUrl(side)
                .map(url -> readMinefield(url, side))
                .orElseGet(() -> logWarn(side));
    }

    /**
     * Read the minefield data from the JSON file.
     *
     * @param minefieldId Uniquely identifies a minefield.
     * @return The airfield's cdata.
     */
    private MinefieldZoneData loadMinefieldZoneData(final MinefieldZoneId minefieldId)  {
        return getZoneUrl(minefieldId)
                .map(url -> readZone(url, minefieldId))
                .orElseGet(() -> logError(minefieldId));
    }

    /**
     * Get the minefield URL.
     *
     * @param side The side ALLIES or AXIS.
     * @return The minefield URL.
     */
    private Optional<URL> getMinefieldUrl(final Side side) {
        return config.isNew() ? config.getScenarioURL(side, Minefield.class)    // Get a new game minefields.
                : config.getSavedURL(side, Minefield.class);                    // Get a saved game minefields.

    }

    /**
     * Get the minefield zone URL.
     *
     * @param minefieldId Uniquely identifies a minefield.
     * @return The minefield zone URL.
     */
    private Optional<URL> getZoneUrl(final MinefieldZoneId minefieldId) {
        Side side = minefieldId.getSide();
        String minefieldName = minefieldId.getName();
        return config.getGameURL(side, MinefieldZone.class, minefieldName + ".json");   // Get a minefield zone.
    }

    /**
     * Read the minefield data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param side The side ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private List<MinefieldData> readMinefield(final URL url, final Side side) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Type collectionType = new TypeToken<List<MinefieldData>>() { }.getType();

                Gson gson = new Gson();
                List<MinefieldData> minefieldData = gson.fromJson(br, collectionType);

                log.debug("load minefields for side {}", side);

                minefieldData.forEach(data -> data.setSide(side));

                return minefieldData;
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
     * Read the minefield zone data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param minefieldId Uniquely identifies a minefield.
     * @return The data read from the JSON file.
     */
    private MinefieldZoneData readZone(final URL url, final MinefieldZoneId minefieldId) {
        String portName = minefieldId.getName();
        Side side = minefieldId.getSide();
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                MinefieldZoneData minefieldData = gson.fromJson(br, MinefieldZoneData.class);

                log.debug("load minefield zone {} for side {}", portName, side);

                return minefieldData;
            } catch (Exception ex) {                                                                                    // Catch any Gson errors.
                log.error("Unable to load minefield zone {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load minefield zone {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Build the minefield from the data read in from the JSON file.
     *
     * @param minefieldZoneData The minefield data read in from the JSON file.
     * @return A minefield.
     */
    private MinefieldZone buildZone(final MinefieldZoneData minefieldZoneData) {
        return zoneFactory.create(minefieldZoneData);
    }

    /**
     * Log an error for minefields that cannot be loaded.
     *
     * @param side The side ALLIES or AXIS.
     * @return Empty list.
     */
    private List<MinefieldData> logWarn(final Side side) {
        log.warn("Unable to load minefields for side: {}", side);
        return Collections.emptyList();
    }

    /**
     * Log an error for minefield zones that cannot be loaded.
     *
     * @param minefieldZoneId The minefield that cannot be loaded.
     * @return null. The calling routine will fitler this out.
     */
    private MinefieldZoneData logError(final MinefieldZoneId minefieldZoneId) {
        log.error("Unable to load minefield zone '{}' for side: {}", minefieldZoneId.getName(), minefieldZoneId.getSide());
        return null;
    }
}


