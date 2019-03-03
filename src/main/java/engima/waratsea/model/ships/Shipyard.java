package engima.waratsea.model.ships;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.data.ShipData;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The shipyard that creates ships given ship class names.
 */
@Singleton
@Slf4j
public class Shipyard {
    // Ship type to ship factory map.
    private Map<ShipType, Function<ShipData, Ship>> factoryMap = new HashMap<>();

    //Each side has a map of ship class names to ship's data. This acts as a cash for data read in from JSON files.
    private Map<Side, Map<String, ShipData>> shipDataMap = new HashMap<>();

    private Config config;
    private ShipRegistry registry;
    private ShipFactory shipFactory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param registry The ship registry. Maps ship names to ship classes.
     * @param shipFactory A factory for creating ships.
     */
    @Inject
    public Shipyard(final Config config,
                    final ShipRegistry registry,
                    final ShipFactory shipFactory) {

        this.config = config;
        this.registry = registry;
        this.shipFactory = shipFactory;

        factoryMap.put(ShipType.AIRCRAFT_CARRIER, shipFactory::createAircraftCarrier);
        factoryMap.put(ShipType.SEAPLANE_CARRIER, shipFactory::createAircraftCarrier);

        shipDataMap.put(Side.ALLIES, new HashMap<>());
        shipDataMap.put(Side.AXIS, new HashMap<>());
    }

    /**
     * Build a ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    public Ship load(final ShipId shipId) throws ShipyardException {
        return config.isNew() ? buildNew(shipId) : buildExisting(shipId);
    }

    /**
     * Save a ship.
     *
     * @param ship The ship to save.
     */
    public void save(final Ship ship) {
        log.info("Save ship: '{}' for side {}", ship.getName(), ship.getShipId().getSide());
        String fileName = config.getSavedFileName(ship.getShipId().getSide(), Ship.class, ship.getName() + ".json");
        PersistentUtility.save(fileName, ship.getData());
    }

    /**
     * Build a new ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private Ship buildNew(final ShipId shipId) throws ShipyardException {
        log.info("Build new ship: '{}' for side {}", shipId.getName(), shipId.getSide());
        String shipClassName = registry.getClass(shipId);
        ShipData shipData = getShipData(shipClassName, shipId.getSide());
        ShipType shipType = shipData.getType();
        shipData.setShipId(shipId);
        return getFactory(shipType).apply(shipData);
    }

    /**
     * Build an existing ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The build ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private Ship buildExisting(final ShipId shipId) throws ShipyardException {
        log.info("Build existing ship: '{}' for side {}", shipId.getName(), shipId.getSide());
        ShipData shipData = loadExistingShipData(shipId);
        ShipType shipType = shipData.getType();
        shipData.setShipId(shipId);
        return getFactory(shipType).apply(shipData);
    }

    /**
     * Get a ship given the ship's class.
     *
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
            data = loadNewShipData(shipClassName, side);
            dataMap.put(shipClassName, data);
        }

        return data;
    }

    /**
     * Read the ship class data from the JSON file.
     *
     * @param shipClassName The ship class to read.
     * @param side The side of the ship. ALLIES or AXIS.
     * @return The ship's class data.
     * @throws ShipyardException An error occurred while attempting to read the ship's class data.
     */
    private ShipData loadNewShipData(final String shipClassName, final Side side) throws ShipyardException {
        log.info("Load new ship class: '{}' for side {}", shipClassName, side);
        return config
                .getGameURL(side, Ship.class, shipClassName + ".json")
                .map(url -> readShipClass(url, side))
                .orElseThrow(() -> new ShipyardException("Unable to load ship class for '" + shipClassName + "' for " + side));
    }

    /**
     * Read the ship data from a saved JSON file.
     *
     * @param shipId uniquely identifies a ship.
     * @return The ship data.
     * @throws ShipyardException An error occurred while attempting to read the ship's data.
     */
    private ShipData loadExistingShipData(final ShipId shipId) throws ShipyardException {
        Side side = shipId.getSide();
        log.info("Load existing ship name: '{}' for side {}", shipId.getName(), side);
        return config
                .getSavedURL(side, Ship.class, shipId.getName() + ".json")
                .map(url -> readShipClass(url, side))
                .orElseThrow(() -> new ShipyardException("Unable to load ship class for '" + shipId.getName() + "' for " + side));
    }

    /**
     * Read the ship class data from the JSON file.
     *
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
     *
     * @param type The type of ship.
     * @return The corresponding factory to the ship's type.
     */
    private Function<ShipData, Ship> getFactory(final ShipType type) {
        return factoryMap.getOrDefault(type, shipFactory::createSurfaceShip);
    }

}
