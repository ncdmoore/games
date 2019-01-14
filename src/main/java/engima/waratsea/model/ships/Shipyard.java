package engima.waratsea.model.ships;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.data.ShipData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The shipyard that creates ships given ship class names.
 */
@Singleton
@Slf4j
public class Shipyard {

    private static final String SHIP_DIRECTORY_NAME = "/ships";

    private Map<ShipType, ShipFactory> factoryMap = new HashMap<>();
    private ShipFactory defaultFactory;

    //Each side has a map of ship class names to ship's data.
    private Map<Side, Map<String, ShipData>> shipDataMap = new HashMap<>();

    private GameTitle gameTitle;
    private ShipRegistry registry;

    /**
     * Constructor called by guice.
     * @param gameTitle The game title/name.
     * @param registry The ship registry. Maps ship names to ship classes.
     * @param aircraftCarrierFactory A factory for creating aircraft carriers.
     * @param surfaceShipFactory A factory for creating surface ships: battleships, cruisers, etc.
     */
    @Inject
    public Shipyard(final GameTitle gameTitle,
                    final ShipRegistry registry,
                    final AircraftCarrierFactory aircraftCarrierFactory,
                    final SurfaceShipFactory surfaceShipFactory) {
        this.gameTitle = gameTitle;
        this.registry = registry;

        factoryMap.put(ShipType.AIRCRAFT_CARRIER, aircraftCarrierFactory);
        factoryMap.put(ShipType.BATTLECRUISER, surfaceShipFactory);
        factoryMap.put(ShipType.BATTLESHIP, surfaceShipFactory);
        factoryMap.put(ShipType.CRUISER, surfaceShipFactory);
        factoryMap.put(ShipType.DESTROYER, surfaceShipFactory);
        factoryMap.put(ShipType.DESTROYER_ESCORT, surfaceShipFactory);
        factoryMap.put(ShipType.MINELAYER, surfaceShipFactory);
        factoryMap.put(ShipType.MINESWEEPER, surfaceShipFactory);
        factoryMap.put(ShipType.OILER, surfaceShipFactory);
        factoryMap.put(ShipType.SEAPLANE_CARRIER, aircraftCarrierFactory);
        factoryMap.put(ShipType.SLOOP, surfaceShipFactory);
        factoryMap.put(ShipType.TRANSPORT, surfaceShipFactory);

        defaultFactory = surfaceShipFactory;

        shipDataMap.put(Side.ALLIES, new HashMap<>());
        shipDataMap.put(Side.AXIS, new HashMap<>());
    }

    /**
     * Build a ship.
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    public Ship build(final ShipId shipId) throws ShipyardException {
        log.info("Build ship: '{}' for side {}", shipId.getName(), shipId.getSide());
        String shipClassName = registry.getClass(shipId);
        ShipData shipData = getShipData(shipClassName, shipId.getSide());
        ShipType shipType = shipData.getType();
        shipData.setShipId(shipId);
        return getFactory(shipType).create(shipData);
    }

    /**
     * Get a ship given the ship's class.
     * @param shipClassName The ship's class.
     * @param side The side. ALLIES or AXIS.
     * @return The ship's data.
     * @throws ShipyardException Indicates that the ship's data could not be found.
     */
    private ShipData getShipData(final String shipClassName, final Side side) throws ShipyardException {
        ShipData data;

        Map<String, ShipData> dataMap = shipDataMap.get(side);

        if (dataMap.containsKey(shipClassName)) {
            data = dataMap.get(shipClassName);
        } else {
            data = loadShipData(shipClassName, side);
            dataMap.put(shipClassName, data);
        }

        return data;
    }

    /**
     * Read the ship class data from the JSON file.
     * @param shipClassName The ship class to read.
     * @param side The side of the ship. ALLIES or AXIS.
     * @return The ship's class data.
     * @throws ShipyardException An error occurred while attempting to read the ship's class data.
     */
    private ShipData loadShipData(final String shipClassName, final Side side) throws ShipyardException {
        String path = gameTitle.getValue() + SHIP_DIRECTORY_NAME + "/" + side.toString().toLowerCase() + "/" + shipClassName + ".json";
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(u -> readShipClass(u, side))
                .orElseThrow(() -> new ShipyardException("Unable to load ship class for '" + shipClassName + "' for " + side));
    }

    /**
     * Read the ship class data from the JSON file.
     * @param url The url of the JSON file.
     * @param side The side: ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private ShipData readShipClass(final URL url, final Side side) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                ShipData shipData = gson.fromJson(br, ShipData.class);

                log.info("load ship class {} for side {}", url.getPath(), side);

                return shipData;
            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
                log.error("Unable to load ship class {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load ship class {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Get the ship's factory based on the ship type.
     * @param type The type of ship.
     * @return The corresponding factory to the ship's type.
     */
    private ShipFactory getFactory(final ShipType type) {
        return factoryMap.containsKey(type) ? factoryMap.get(type) : useDefaultFactory(type);
    }

    /**
     * Use the default ship factory.
     * @param type The type of ship that uses the default factory for ship construction.
     * @return The default ship factory.
     */
    private ShipFactory useDefaultFactory(final ShipType type) {
        log.warn("Using default factory for ship type {}", type);
        return defaultFactory;
    }
}
