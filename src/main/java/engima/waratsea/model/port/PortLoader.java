package engima.waratsea.model.port;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.port.data.PortData;
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
public class PortLoader {
    private Config config;
    private GameMap gameMap;
    private PortFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param gameMap The game map.
     * @param factory The port factory.
     */
    @Inject
    public PortLoader(final Config config,
                      final GameMap gameMap,
                      final PortFactory factory) {
        this.config = config;
        this.gameMap = gameMap;
        this.factory = factory;
    }

    /**
     * Build the ports.
     *
     * @param side The airfield side ALLIES or AXIS.
     * @return A list of airfield objects.
     */
    public List<Port> load(final Side side) {
        return gameMap.getPorts(side)
                .stream()
                .map(port -> loadPortData(port, side))
                .filter(Objects::nonNull)
                .map(data -> factory.create(side, data))
                .collect(Collectors.toList());
    }

    /**
     * Save the ports. The allies and axis port data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param ports The port data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<Port> ports) {
        log.info("Saving ports, scenario: '{}',side {}", scenario.getTitle(), side);
        ports.forEach(port -> {
            String fileName = config.getSavedFileName(side, Port.class, port.getName() + ".json");
            PersistentUtility.save(fileName, port.getData());
        });
    }

    /**
     * Read the airfield  data from the JSON file.
     *
     * @param portName The airfield to read.
     * @param side The side of the airfield. ALLIES or AXIS.
     * @return The airfield's cdata.
     */
    private PortData loadPortData(final String portName, final Side side)  {
        return getURL(side, portName)
                .map(url -> readPort(portName, url, side))
                .orElseGet(() -> logError(portName));
    }

    /**
     * Get the port URL.
     *
     * @param side The side ALLIES or AXIS.
     * @param portName The name of the port for which the URL is obtained.
     * @return The port URL.
     */
    private Optional<URL> getURL(final Side side, final String portName) {
        return config.isNew() ? config.getGameURL(side, Port.class, portName + ".json")    // Get a new game port.
                : config.getSavedURL(side, Port.class, portName + ".json");                // Get a saved game port.
    }

    /**
     * Read the airfield data from the JSON file.
     *
     * @param portName The name of the airfield.
     * @param url The url of the JSON file.
     * @param side The side: ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private PortData readPort(final String portName, final URL url, final Side side) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                PortData portData = gson.fromJson(br, PortData.class);

                log.info("load port {} for side {}", portName, side);

                return portData;
            } catch (Exception ex) {                                                                                    // Catch any Gson errors.
                log.error("Unable to load port {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load port {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Log an error for ports that cannot be loaded.
     *
     * @param portName The port that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private PortData logError(final String portName) {
        log.error("Unable to load port '{}'", portName);
        return null;
    }
}
