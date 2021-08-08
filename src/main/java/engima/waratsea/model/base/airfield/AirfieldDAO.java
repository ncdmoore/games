package engima.waratsea.model.base.airfield;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.BaseId;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.scenario.ScenarioEvent;
import engima.waratsea.model.game.event.scenario.ScenarioEventTypes;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class loads airfield data from JSON files.
 */
@Slf4j
@Singleton
public class AirfieldDAO {
    private final Resource config;
    private final AirfieldFactory factory;

    private final Map<String, Airfield> cache = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param factory The airfield factory.
     */
    @Inject
    public AirfieldDAO(final Resource config,
                       final AirfieldFactory factory) {
        this.config = config;
        this.factory = factory;

        ScenarioEvent.register(this, this::init, true);
    }

    /**
     * Initialize the airfield cache.
     *
     * @param event The scenario event.
     */
    private void init(final ScenarioEvent event) {
        if (event.getType() == ScenarioEventTypes.BOOT) {
            log.debug("Clear airfield DAO cache.");
            cache.clear();
        }
    }

    /**
     * Build the airfields.
     *
     * @param airfieldId The airfield Id.
     * @return An Airfield
     */
    public Airfield load(final BaseId airfieldId) {

        if (cache.containsKey(airfieldId.getName())) {
            return cache.get(airfieldId.getName());
        } else {
            Airfield airfield = buildAirfield(loadAirfieldData(airfieldId));
            cache.put(airfieldId.getName(), airfield);
            return airfield;
        }
    }

    /**
     * Save the airfields. The allies and axis airfield data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param airfields The port data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<Airfield> airfields) {
        log.debug("Saving airfields, scenario: '{}',side {}", scenario.getTitle(), side);
        airfields.forEach(airfield -> {
            String fileName = config.getSavedFileName(side, Airfield.class, airfield.getName() + ".json");
            PersistentUtility.save(fileName, airfield);
            log.debug("Saving Airfield: '{}' with '{}' squadrons", airfield.getTitle(), airfield.getSquadrons().size());
        });
    }

    /**
     * Read the airfield  data from the JSON file.
     *
     * @param airfieldId The airfield to read.
     * @return The airfield's cdata.
     */
    private AirfieldData loadAirfieldData(final BaseId airfieldId)  {
        String airfieldName = airfieldId.getName();
        Side side = airfieldId.getSide();

        return getURL(side, airfieldName)
                .map(url -> readAirfield(url, airfieldId))
                .orElseGet(() -> logError(airfieldId));
    }

    /**
     * Get the airfield URL.
     *
     * @param side The side ALLIES or AXIS.
     * @param airfieldName The name of the airfield for which the URL is obtained.
     * @return The port URL.
     */
    private Optional<URL> getURL(final Side side, final String airfieldName) {
        return config.isNew() ? config.getGameURL(side, Airfield.class, airfieldName + ".json")    // Get a new game port.
                : config.getSavedURL(side, Airfield.class, airfieldName + ".json");                // Get a saved game port.
    }

    /**
     * Read the airfield data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param airfieldId The airfield to read.
     * @return The data read from the JSON file.
     */
    private AirfieldData readAirfield(final URL url, final BaseId airfieldId) {
        String airfieldName = airfieldId.getName();
        Side side = airfieldId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            Gson gson = new Gson();
            AirfieldData airfieldData = gson.fromJson(br, AirfieldData.class);

            airfieldData.setSide(side);

            log.debug("load airfield {} for side {}", airfieldName, side);

            return airfieldData;
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load airfield {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Build the airfield from the data read in from the JSON file.
     *
     * @param airfieldData The airfield data read in from the JSON file.
     * @return An airfield.
     */
    private Airfield buildAirfield(final AirfieldData airfieldData) {
        return factory.create(airfieldData);
    }

    /**
     * Log an error for airfields that cannot be loaded.
     *
     * @param airfieldId The airfield that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private AirfieldData logError(final BaseId airfieldId) {
        log.error("Unable to load airfield '{}'", airfieldId.getName());
        return null;
    }

}
