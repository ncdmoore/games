package engima.waratsea.model.base.port;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.BaseId;
import engima.waratsea.model.base.port.data.PortData;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
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
public class PortDAO {
    private Resource config;
    private PortFactory factory;

    private Map<String, Port> cache = new HashMap<>();
    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param factory The port factory.
     */
    @Inject
    public PortDAO(final Resource config,
                   final PortFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    /**
     * Initialize the airfield cache.
     */
    public void init() {
        cache.clear();
    }

    /**
     * Build the ports.
     *
     * @param portId Uniquely identifies the port to load.
     * @return A port.
     */
    public Port load(final BaseId portId) {
        if (cache.containsKey(portId.getName())) {
            return cache.get(portId.getName());
        } else {
            Port port = buildPort(loadPortData(portId));
            cache.put(portId.getName(), port);
            return port;
        }
    }

    /**
     * Save the ports. The allies and axis port data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param ports The port data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<Port> ports) {
        log.debug("Saving ports, scenario: '{}',side {}", scenario.getTitle(), side);
        ports.forEach(port -> {
            String fileName = config.getSavedFileName(side, Port.class, port.getName() + ".json");
            PersistentUtility.save(fileName, port);
        });
    }

    /**
     * Read the airfield  data from the JSON file.
     *
     * @param portId Uniquely identifies a port.
     * @return The airfield's cdata.
     */
    private PortData loadPortData(final BaseId portId)  {
        String portName = portId.getName();
        Side side = portId.getSide();

        return getURL(side, portName)
                .map(url -> readPort(url, portId))
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
     * @param url The url of the JSON file.
     * @param portId Uniquely identifies a port.
     * @return The data read from the JSON file.
     */
    private PortData readPort(final URL url, final BaseId portId) {
        String portName = portId.getName();
        Side side = portId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            Gson gson = new Gson();
            PortData portData = gson.fromJson(br, PortData.class);

            portData.setSide(side);

            log.debug("load port {} for side {}", portName, side);

            return portData;
        } catch (Exception ex) {                                                                                    // Catch any Gson errors.
            log.error("Unable to load port {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }

    }

    /**
     * Build the port from the data read in from the JSON file.
     *
     * @param portData The port data read in from the JSON file.
     * @return A port.
     */
    private Port buildPort(final PortData portData) {
        return factory.create(portData);
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
