package engima.waratsea.model.motorTorpedoBoat;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.motorTorpedoBoat.data.MotorTorpedoBoatData;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipRegistry;
import engima.waratsea.model.ship.ShipyardException;
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

/**
 * Responsible for loading and saving persistent submarine data.
 */
@Singleton
@Slf4j
public class MotorTorpedoBoatDAO {

    //Each side has a map of ship class names to ship's data. This acts as a cash for data read in from JSON files.
    private Map<Side, Map<String, MotorTorpedoBoatData>> boatDataMap = new HashMap<>();

    private Config config;
    private ShipRegistry registry;
    private MotorTorpedoBoatFactory boatFactory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param registry The ship registry. Maps ship/sub names to ship/sub classes.
     * @param boatFactory A factory for creating motor torpedo boats.
     */
    @Inject
    public MotorTorpedoBoatDAO(final Config config,
                               final ShipRegistry registry,
                               final MotorTorpedoBoatFactory boatFactory) {

        this.config = config;
        this.registry = registry;
        this.boatFactory = boatFactory;

        boatDataMap.put(Side.ALLIES, new HashMap<>());
        boatDataMap.put(Side.AXIS, new HashMap<>());
    }

    /**
     * Build a ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    public MotorTorpedoBoat load(final ShipId shipId) throws ShipyardException {
        return config.isNew() ? buildNew(shipId) : buildExisting(shipId);
    }

    /**
     * Save a submarine.
     *
     * @param boat The submarine to save.
     */
    public void save(final MotorTorpedoBoat boat) {
        log.debug("Save submarine: '{}' for side {}", boat.getShipId().getName(), boat.getShipId().getSide());
        String fileName = config.getSavedFileName(boat.getShipId().getSide(), Ship.class, boat.getShipId().getName() + ".json");
        PersistentUtility.save(fileName, boat);
    }
    /**
     * Build a new ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private MotorTorpedoBoat buildNew(final ShipId shipId) throws ShipyardException {
        log.debug("Build new sub: '{}' for side {}", shipId.getName(), shipId.getSide());
        String boatClassName = registry.getClass(shipId);
        MotorTorpedoBoatData boatData = getBoatData(boatClassName, shipId);
        boatData.setShipId(shipId);
        return boatFactory.create(boatData);
    }

    /**
     * Build an existing ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private MotorTorpedoBoat buildExisting(final ShipId shipId) throws ShipyardException {
        log.debug("Build existing sub: '{}' for side {}", shipId.getName(), shipId.getSide());
        MotorTorpedoBoatData boatData = loadExistingBoatData(shipId);
        boatData.setShipId(shipId);
        return boatFactory.create(boatData);
    }

    /**
     * Get a ship given the ship's class.
     *
     * @param shipClassName The ship's class.
     * @param shipId The shipId.
     * @return The ship's data.
     * @throws ShipyardException Indicates that the ship's data could not be found.
     */
    private MotorTorpedoBoatData getBoatData(final String shipClassName, final ShipId shipId) throws ShipyardException {
        MotorTorpedoBoatData data;
        Side side = shipId.getSide();

        Map<String, MotorTorpedoBoatData> dataMap = boatDataMap.get(side);

        if (dataMap.containsKey(shipClassName)) {
            data = dataMap.get(shipClassName);
        } else {
            data = loadNewBoatData(shipClassName, shipId);
            dataMap.put(shipClassName, data);
        }

        return data;
    }

    /**
     * Read the ship class data from the JSON file.
     *
     * @param shipClassName The ship class to read.
     * @param shipId The shipId.
     * @return The boat data.
     * @throws ShipyardException An error occurred while attempting to read the ship's class data.
     */
    private MotorTorpedoBoatData loadNewBoatData(final String shipClassName, final ShipId shipId) throws ShipyardException {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        log.debug("Load new boat class: '{}' for boat: '{}', side {}", new Object[]{shipClassName, shipName, side});
        return config
                .getGameURL(side, Ship.class, shipClassName + ".json")
                .map(url -> readShipClass(url, shipId))
                .orElseThrow(() -> new ShipyardException("Unable to load boat class '" + shipClassName + "' for boat '" + shipName + "' and side " + side));
    }

    /**
     * Read the sub data from a saved JSON file.
     *
     * @param shipId uniquely identifies a ship.
     * @return The sub data.
     * @throws ShipyardException An error occurred while attempting to read the ship's data.
     */
    private MotorTorpedoBoatData loadExistingBoatData(final ShipId shipId) throws ShipyardException {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        log.debug("Load existing boat name: '{}' for side {}", shipName, side);
        return config
                .getSavedURL(side, Ship.class, shipId.getName() + ".json")
                .map(url -> readShipClass(url, shipId))
                .orElseThrow(() -> new ShipyardException("Unable to load boat '" + shipName + "' for " + side));
    }

    /**
     * Read the sub class data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param shipId The shipId.
     * @return The data read from the JSON file.
     */
    private MotorTorpedoBoatData readShipClass(final URL url, final ShipId shipId) {
        String shipName = shipId.getName();
        Side side = shipId.getSide();

        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                log.debug("load sub class '{}' for boat '{}' and side '{}'", new Object[]{url.getPath(), shipName, side});

                Gson gson = new Gson();
                return gson.fromJson(br, MotorTorpedoBoatData.class);

            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
                log.error("Unable to load sub class '{}' for boat '{}' and side: '{}'. {}", new Object[]{url.getPath(), shipName, side, ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load sub class '{}' for boat '{}' and side: '{}'. {}", new Object[]{url.getPath(), shipName, side, ex});
            return null;
        }
    }
}
