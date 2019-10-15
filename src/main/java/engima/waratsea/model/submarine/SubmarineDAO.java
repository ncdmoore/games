package engima.waratsea.model.submarine;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipRegistry;
import engima.waratsea.model.ship.ShipyardException;
import engima.waratsea.model.submarine.data.SubmarineData;
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

/**
 * Responsible for loading and saving persistent submarine data.
 */
@Singleton
@Slf4j
public class SubmarineDAO {

    //Each side has a map of ship class names to ship's data. This acts as a cash for data read in from JSON files.
    private Map<Side, Map<String, SubmarineData>> subDataMap = new HashMap<>();

    private Resource config;
    private ShipRegistry registry;
    private SubmarineFactory subFactory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config.
     * @param registry The ship registry. Maps ship/sub names to ship/sub classes.
     * @param subFactory A factory for creating submarines.
     */
    @Inject
    public SubmarineDAO(final Resource config,
                    final ShipRegistry registry,
                    final SubmarineFactory subFactory) {

        this.config = config;
        this.registry = registry;
        this.subFactory = subFactory;


        subDataMap.put(Side.ALLIES, new HashMap<>());
        subDataMap.put(Side.AXIS, new HashMap<>());
    }

    /**
     * Build a ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    public Submarine load(final ShipId shipId) throws ShipyardException {
        return config.isNew() ? buildNew(shipId) : buildExisting(shipId);
    }

    /**
     * Save a submarine.
     *
     * @param submarine The submarine to save.
     */
    public void save(final Submarine submarine) {
        log.debug("Save submarine: '{}' for side {}", submarine.getShipId().getName(), submarine.getShipId().getSide());
        String fileName = config.getSavedFileName(submarine.getShipId().getSide(), Ship.class, submarine.getShipId().getName() + ".json");
        PersistentUtility.save(fileName, submarine);
    }
    /**
     * Build a new ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private Submarine buildNew(final ShipId shipId) throws ShipyardException {
        log.debug("Build new sub: '{}' for side {}", shipId.getName(), shipId.getSide());
        String subClassName = registry.getClass(shipId);
        SubmarineData subData = getSubData(subClassName, shipId);
        subData.setShipId(shipId);
        return subFactory.create(subData);
    }

    /**
     * Build an existing ship.
     *
     * @param shipId uniquely identifies a ship.
     * @return The built ship.
     * @throws ShipyardException Indicates that the ship could not be built.
     */
    private Submarine buildExisting(final ShipId shipId) throws ShipyardException {
        log.debug("Build existing sub: '{}' for side {}", shipId.getName(), shipId.getSide());
        SubmarineData subData = loadExistingSubData(shipId);
        subData.setShipId(shipId);
        return subFactory.create(subData);
    }

    /**
     * Get a ship given the ship's class.
     *
     * @param shipClassName The ship's class.
     * @param shipId The shipId.
     * @return The ship's data.
     * @throws ShipyardException Indicates that the ship's data could not be found.
     */
    private SubmarineData getSubData(final String shipClassName, final ShipId shipId) throws ShipyardException {
        SubmarineData data;
        Side side = shipId.getSide();

        Map<String, SubmarineData> dataMap = subDataMap.get(side);

        if (dataMap.containsKey(shipClassName)) {
            data = dataMap.get(shipClassName);
        } else {
            data = loadNewSubData(shipClassName, shipId);
            dataMap.put(shipClassName, data);
        }

        return data;
    }

    /**
     * Read the ship class data from the JSON file.
     *
     * @param shipClassName The ship class to read.
     * @param shipId The shipId.
     * @return The sub data.
     * @throws ShipyardException An error occurred while attempting to read the ship's class data.
     */
    private SubmarineData loadNewSubData(final String shipClassName, final ShipId shipId) throws ShipyardException {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        log.debug("Load new sub class: '{}' for sub: '{}', side {}", new Object[]{shipClassName, shipName, side});
        return config
                .getGameURL(side, Ship.class, shipClassName + ".json")
                .map(url -> readShipClass(url, shipId))
                .orElseThrow(() -> new ShipyardException("Unable to load sub class '" + shipClassName + "' for sub '" + shipName + "' and side " + side));
    }

    /**
     * Read the sub data from a saved JSON file.
     *
     * @param shipId uniquely identifies a ship.
     * @return The sub data.
     * @throws ShipyardException An error occurred while attempting to read the ship's data.
     */
    private SubmarineData loadExistingSubData(final ShipId shipId) throws ShipyardException {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        log.debug("Load existing sub name: '{}' for side {}", shipName, side);
        return config
                .getSavedURL(side, Ship.class, shipId.getName() + ".json")
                .map(url -> readShipClass(url, shipId))
                .orElseThrow(() -> new ShipyardException("Unable to load sub '" + shipName + "' for " + side));
    }

    /**
     * Read the sub class data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param shipId The shipId.
     * @return The data read from the JSON file.
     */
    private SubmarineData readShipClass(final URL url, final ShipId shipId) {
        String shipName = shipId.getName();
        Side side = shipId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            log.debug("load sub class '{}' for sub '{}' and side '{}'", new Object[]{url.getPath(), shipName, side});

            Gson gson = new Gson();
            return gson.fromJson(br, SubmarineData.class);

        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load sub class '{}' for sub '{}' and side: '{}'. {}", new Object[]{url.getPath(), shipName, side, ex});
            return null;
        }
    }
}
