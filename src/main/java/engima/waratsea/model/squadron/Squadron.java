package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftId;
import engima.waratsea.model.aircraft.AviationPlant;
import engima.waratsea.model.aircraft.AviationPlantException;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.data.SquadronData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aircraft squadron of a particular class of aircraft.
 */
@Slf4j
public class Squadron {
    @Getter
    private Side side;

    @Getter
    private final String model;

    @Getter
    @Setter
    private Aircraft aircraft;

    @Getter
    private String name;

    @Getter
    @Setter
    private int strength;

    @Getter
    private String location;

    @Getter
    @Setter
    private Airfield airfield;


    private GameMap gameMap;

    private static Map<Side, Map<String, Integer>> designationMap = new HashMap<>();

    static {
        designationMap.put(Side.ALLIES, new HashMap<>());
        designationMap.put(Side.AXIS, new HashMap<>());
    }

    /**
     * Initialize the designation maps for both sides.
     *
     * @param side The side ALLIES or AXIS.
     */
    public static void init(final Side side) {
        designationMap.get(side).clear();
    }

    /**
     * Constructor called by guice.
     *
     * @param side The side ALLIES or AXIS.
     * @param data The data of the squadron read in from a JSON file.
     * @param aviationPlant The aviation plant that creates aircraft for squadrons.
     * @param gameMap The game map.
     */
    @Inject
    public Squadron(@Assisted final Side side,
                    @Assisted final SquadronData data,
                              final AviationPlant aviationPlant,
                              final GameMap gameMap) {
        this.side = side;
        this.gameMap = gameMap;
        this.model = data.getModel();
        this.strength = data.getStrength();

        try {
            AircraftId aircraftId = new AircraftId(model, side);
            aircraft = aviationPlant.load(aircraftId);

            String designation = aircraft.getDesignation();

            int index = designationMap.get(side).getOrDefault(designation, 0);
            designationMap.get(side).put(designation, ++index);

            name = designation + index + "-" + model;

            log.info("Squadron: '{}' with strength: '{}' built for side: {}", new Object[]{name, strength, side});

        } catch (AviationPlantException ex) {
            log.error("Unable to build aircraft model: '{}' for side: {}", model, side);
        }
    }

    /**
     * Set the squadron's location..
     *
     * @param newLocation The squadron's new location.
     */
    public void setLocation(final String newLocation) {
        location = gameMap.convertNameToReference(newLocation);
    }

    /**
     * Determine if the squadron is at an enemey base.
     *
     * @return True if the squadron is currently located at an enemy base. False otherwise.
     */
    public boolean atEnemyBase() {
        return gameMap.isLocationBase(side.opposite(), location);
    }

    /**
     * Determine if the squadron is at a friendly base.
     *
     * @return True if the squadron is currently located at a friendly base. False otherwise.
     */
    public boolean atFriendlyBase() {
        return gameMap.isLocationBase(side, location);
    }

}
