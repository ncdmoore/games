package engima.waratsea.model.aircraft;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import lombok.SneakyThrows;
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
 * Responsible for loading aircraft data from JSON files.
 * Aircraft data is read only.
 */
@Slf4j
@Singleton
public class AviationPlant {
    // Aircraft type to aircraft factory map.
    private final Map<AircraftType, Function<AircraftData, Aircraft>> factoryMap;

    //Each side has a map of aircraft model names to aircraft model data. This acts as a cash for data read in from JSON files.
    private final Map<Side, Map<String, AircraftData>> aircraftDataMap = Map.of(
            Side.ALLIES, new HashMap<>(),
            Side.AXIS, new HashMap<>()
    );

    private final Resource config;
    private final AircraftFactory factory;

    /**
     * The constructor called by guice.
     *
     * @param config The game's config.
     * @param factory The aircraft factory.
     */
    @Inject
    public AviationPlant(final Resource config, final AircraftFactory factory) {
        this.config = config;
        this.factory = factory;

        factoryMap = Map.of(
                AircraftType.FIGHTER, factory::createFighter,
                AircraftType.RECONNAISSANCE, factory::createRecon,
                AircraftType.BOMBER, factory::createBomber);
    }

    /**
     * Build an aircraft.
     *
     * @param aircraftId uniquely identifies an aircraft model.
     * @return The built aircraft model.
     * @throws AviationPlantException Indicates that the aircraft model could not be built.
     */
    public Aircraft load(final AircraftId aircraftId) throws AviationPlantException {
        log.debug("Build aircraft model: '{}' for side {}", aircraftId.getModel(), aircraftId.getSide());
        AircraftData aircraftData = getData(aircraftId.getModel(), aircraftId.getSide());
        aircraftData.setAircraftId(aircraftId);
        AircraftType aircraftType = aircraftData.getType();
        return getFactory(aircraftType).apply(aircraftData);
    }

    /**
     * Get an aircraft given the aircraft's model.
     *
     * @param model The aircraft's model.
     * @param side The side. ALLIES or AXIS.
     * @return The aircraft's data.
     */
    private AircraftData getData(final String model, final Side side) {
        return aircraftDataMap
                .get(side)
                .computeIfAbsent(model, m -> loadData(m, side));
    }

    /**
     * Read the aircraft model data from the JSON file.
     *
     * @param model The aircraft model to read.
     * @param side The side of the ship. ALLIES or AXIS.
     * @return The aircraft's class data.
     */
    @SneakyThrows
    private AircraftData loadData(final String model, final Side side) {
        log.debug("Load aircraft model: '{}' for side {}", model, side);
        return config
                .getGameURL(side, Aircraft.class, model + ".json")
                .map(url -> readAircraftModel(url, side))
                .orElseThrow(() -> new AviationPlantException("Unable to load aircraft model for '" + model + "' for " + side));
    }

    /**
     * Read the aircraft model data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param side The side: ALLIES or AXIS.
     * @return The data read from the JSON file.
     */
    private AircraftData readAircraftModel(final URL url, final Side side) {
        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            log.debug("load aircraft model {} for side {}", url.getPath(), side);

            Gson gson = new Gson();
            return gson.fromJson(br, AircraftData.class);

        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load aircraft model '{}' for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Get the aircraft's factory based on the aircraft type.
     *
     * @param type The type of aircraft.
     * @return The corresponding factory to the aircraft's type.
     */
    private Function<AircraftData, Aircraft> getFactory(final AircraftType type) {
        return factoryMap.getOrDefault(type, factory::createBomber);
    }
}
