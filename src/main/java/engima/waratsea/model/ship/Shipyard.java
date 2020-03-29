package engima.waratsea.model.ship;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.taskForce.TaskForce;
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

    private Resource config;
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
    public Shipyard(final Resource config,
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
     * @param taskForce The ship's task force.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    public Ship load(final ShipId shipId, final TaskForce taskForce) throws ShipyardException {
        return config.isNew() ? buildNew(shipId, taskForce) : buildExisting(shipId, taskForce);
    }

    /**
     * Save a ship.
     *
     * @param ship The ship to save.
     */
    public void save(final Ship ship) {
        log.debug("Save ship: '{}' for side {}", ship.getName(), ship.getShipId().getSide());
        String fileName = config.getSavedFileName(ship.getShipId().getSide(), Ship.class, ship.getName() + ".json");
        PersistentUtility.save(fileName, ship);
    }

    /**
     * Build a new ship.
     *
     * @param shipId uniquely identifies a ship.
     * @param taskForce The ship's task force.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private Ship buildNew(final ShipId shipId, final TaskForce taskForce) throws ShipyardException {
        log.debug("Build new ship: '{}' for side {}", shipId.getName(), shipId.getSide());
        String shipClassName = registry.getClass(shipId);
        ShipData shipData = getShipData(shipClassName, shipId);
        ShipType shipType = shipData.getType();
        shipData.setShipId(shipId);
        shipData.setTaskForce(taskForce);
        return getFactory(shipType).apply(shipData);
    }

    /**
     * Build an existing ship.
     *
     * @param shipId uniquely identifies a ship.
     * @param taskForce The ship's task force.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private Ship buildExisting(final ShipId shipId, final TaskForce taskForce) throws ShipyardException {
        log.debug("Build existing ship: '{}' for side {}", shipId.getName(), shipId.getSide());
        ShipData shipData = loadExistingShipData(shipId);
        ShipType shipType = shipData.getType();
        shipData.setShipId(shipId);
        shipData.setTaskForce(taskForce);
        return getFactory(shipType).apply(shipData);
    }

    /**
     * Get a ship given the ship's class.
     *
     * @param shipClassName The ship's class.
     * @param shipId The shipId.
     * @return The ship's data.
     * @throws ShipyardException Indicates that the ship's data could not be found.
     */
    private ShipData getShipData(final String shipClassName, final ShipId shipId) throws ShipyardException {
        ShipData data;
        Side side = shipId.getSide();

        Map<String, ShipData> dataMap = shipDataMap.get(side);

        if (dataMap.containsKey(shipClassName)) {
            data = dataMap.get(shipClassName);
        } else {
            data = loadNewShipData(shipClassName, shipId);
            dataMap.put(shipClassName, data);
        }

        return data;
    }

    /**
     * Read the ship class data from the JSON file.
     *
     * @param shipClassName The ship class to read.
     * @param shipId The shipId.
     * @return The ship's class data.
     * @throws ShipyardException An error occurred while attempting to read the ship's class data.
     */
    private ShipData loadNewShipData(final String shipClassName, final ShipId shipId) throws ShipyardException {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        log.debug("Load new ship class: '{}' for ship: '{}', side {}", new Object[]{shipClassName, shipName, side});
        return config
                .getGameURL(side, Ship.class, shipClassName + ".json")
                .map(url -> readShipClass(url, shipId))
                .orElseThrow(() -> new ShipyardException("Unable to load ship class '" + shipClassName + "' for ship '" + shipName + "' and side " + side));
    }

    /**
     * Read the ship data from a saved JSON file.
     *
     * @param shipId uniquely identifies a ship.
     * @return The ship data.
     * @throws ShipyardException An error occurred while attempting to read the ship's data.
     */
    private ShipData loadExistingShipData(final ShipId shipId) throws ShipyardException {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        log.debug("Load existing ship name: '{}' for side {}", shipName, side);
        return config
                .getSavedURL(side, Ship.class, shipId.getName() + ".json")
                .map(url -> readShipClass(url, shipId))
                .orElseThrow(() -> new ShipyardException("Unable to load ship '" + shipName + "' for " + side));
    }

    /**
     * Read the ship class data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param shipId The shipId.
     * @return The data read from the JSON file.
     */
    private ShipData readShipClass(final URL url, final ShipId shipId) {
        String shipName = shipId.getName();
        Side side = shipId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            log.debug("load ship class '{}' for ship '{}' and side '{}'", new Object[]{url.getPath(), shipName, side});

            Gson gson = new Gson();
            return gson.fromJson(br, ShipData.class);

        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load ship class '{}' for ship '{}' and side: '{}'. {}", new Object[]{url.getPath(), shipName, side, ex});
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
