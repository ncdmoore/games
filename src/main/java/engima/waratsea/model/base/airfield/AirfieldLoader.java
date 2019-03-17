package engima.waratsea.model.base.airfield;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class loads airfield data from JSON files.
 */
@Slf4j
@Singleton
public class AirfieldLoader {
    private Config config;
    private AirfieldFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param factory The airfield factory.
     */
    @Inject
    public AirfieldLoader(final Config config,
                          final AirfieldFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    /**
     * Build the airfields.
     *
     * @param side The airfield side ALLIES or AXIS.
     * @param airfields The list of airfields to load.
     * @return A list of airfield objects.
     */
    public List<Airfield> load(final Side side, final List<String> airfields) {
        return airfields
                .stream()
                .map(airfield -> loadAirfieldData(airfield, side))
                .filter(Objects::nonNull)
                .map(data -> factory.create(side, data))
                .collect(Collectors.toList());
    }

    /**
     * Save the airfields. The allies and axis airfield data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param airfields The port data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<Airfield> airfields) {
        log.info("Saving airfields, scenario: '{}',side {}", scenario.getTitle(), side);
        airfields.forEach(airfield -> {
            String fileName = config.getSavedFileName(side, Airfield.class, airfield.getName() + ".json");
            PersistentUtility.save(fileName, airfield.getData());
        });
    }

    /**
     * Read the airfield  data from the JSON file.
     *
     * @param airfieldName The airfield to read.
     * @param side The side of the airfield. ALLIES or AXIS.
     * @return The airfield's cdata.
     */
    private AirfieldData loadAirfieldData(final String airfieldName, final Side side)  {
        return getURL(side, airfieldName)
                .map(url -> readAirfield(airfieldName, url, side))
                .orElseGet(() -> logError(airfieldName));
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
     * @param airfieldName The name of the airfield.
     * @param url The url of the JSON file.
     * @param side The side: ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private AirfieldData readAirfield(final String airfieldName, final URL url, final Side side) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                AirfieldData airfieldData = gson.fromJson(br, AirfieldData.class);

                log.info("load airfield {} for side {}", airfieldName, side);

                return airfieldData;
            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
                log.error("Unable to load airfield {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load airfield {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Log an error for airfields that cannot be loaded.
     *
     * @param airfieldName The airfield that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private AirfieldData logError(final String airfieldName) {
        log.error("Unable to load airfield '{}'", airfieldName);
        return null;
    }
}
