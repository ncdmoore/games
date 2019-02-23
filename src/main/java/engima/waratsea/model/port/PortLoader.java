package engima.waratsea.model.port;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.port.data.PortData;
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
    private GameTitle gameTitle;
    private GameMap gameMap;
    private PortFactory factory;

    /**
     * Constructor called by guice.
     * @param gameTitle The game title.
     * @param gameMap The game map.
     * @param factory The port factory.
     */
    @Inject
    public PortLoader(final GameTitle gameTitle,
                      final GameMap gameMap,
                      final PortFactory factory) {
        this.gameTitle = gameTitle;
        this.gameMap = gameMap;
        this.factory = factory;
    }

    /**
     * Build the ports.
     * @param side The airfield side ALLIES or AXIS.
     * @return A list of airfield objects.
     */
    public List<Port> build(final Side side) {
        return gameMap.getPorts(side)
                .stream()
                .map(port -> loadPortData(port, side))
                .filter(Objects::nonNull)
                .map(data -> factory.create(side, data))
                .collect(Collectors.toList());
    }

    /**
     * Read the airfield  data from the JSON file.
     * @param portName The airfield to read.
     * @param side The side of the airfield. ALLIES or AXIS.
     * @return The airfield's cdata.
     */
    private PortData loadPortData(final String portName, final Side side)  {
        String path = gameTitle.getValue() + Config.PORT_DIRECTORY_NAME + "/" + side.toString().toLowerCase() + "/" + portName + ".json";
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(u -> readPort(portName, u, side))
                .orElseGet(() -> logError(portName));
    }

    /**
     * Read the airfield data from the JSON file.
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
            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
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
     * @param portName The port that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private PortData logError(final String portName) {
        log.error("Unable to load port '{}'", portName);
        return null;
    }
}
