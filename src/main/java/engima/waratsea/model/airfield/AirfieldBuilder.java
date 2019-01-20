package engima.waratsea.model.airfield;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.airfield.data.AirfieldData;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
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
public class AirfieldBuilder {
    private static final String AIRFIELD_DIRECTORY_NAME = "/airfields";

    private GameTitle gameTitle;
    private GameMap gameMap;
    private AirfieldFactory factory;

    /**
     * Constructor called by guice.
     * @param gameTitle The game title.
     * @param gameMap The game map.
     * @param factory The airfield factory.
     */
    @Inject
    public AirfieldBuilder(final GameTitle gameTitle,
                           final GameMap gameMap,
                           final AirfieldFactory factory) {
        this.gameTitle = gameTitle;
        this.gameMap = gameMap;
        this.factory = factory;
    }

    /**
     * Build the airfields.
     * @param side The airfield side ALLIES or AXIS.
     * @return A list of airfield objects.
     */
    public List<Airfield> build(final Side side) {
        return gameMap.getAirfields(side)
                .stream()
                .map(airfield -> loadAirfieldData(airfield, side))
                .filter(Objects::nonNull)
                .map(data -> factory.create(side, data))
                .collect(Collectors.toList());
    }

    /**
     * Read the airfield  data from the JSON file.
     * @param airfieldName The airfield to read.
     * @param side The side of the airfield. ALLIES or AXIS.
     * @return The airfield's cdata.
     */
    private AirfieldData loadAirfieldData(final String airfieldName, final Side side)  {
        String path = gameTitle.getValue() + AIRFIELD_DIRECTORY_NAME + "/" + side.toString().toLowerCase() + "/" + airfieldName + ".json";
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(u -> readAirfield(airfieldName, u, side))
                .orElseGet(() -> logError(airfieldName));
    }

    /**
     * Read the airfield data from the JSON file.
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
     * @param airfieldName The airfield that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private AirfieldData logError(final String airfieldName) {
        log.error("Unable to load airfield '{}'", airfieldName);
        return null;
    }
}
