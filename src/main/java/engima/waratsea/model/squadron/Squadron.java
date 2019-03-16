package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftId;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.AviationPlant;
import engima.waratsea.model.aircraft.AviationPlantException;
import engima.waratsea.model.game.Side;
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
    private final String model;

    @Getter
    @Setter
    private Aircraft aircraft;

    @Getter
    private String name;

    @Getter
    @Setter
    private int strength;

    private static Map<Side, Map<AircraftType, Integer>> designationMap = new HashMap<>();

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
     */
    @Inject
    public Squadron(@Assisted final Side side,
                    @Assisted final SquadronData data,
                              final AviationPlant aviationPlant) {
        this.model = data.getModel();
        this.strength = data.getStrength();

        try {
            AircraftId aircraftId = new AircraftId(model, side);
            aircraft = aviationPlant.load(aircraftId);

            AircraftType aircraftType = aircraft.getType();
            String designation = aircraftType.getDesignation();

            int index = designationMap.get(side).getOrDefault(aircraftType, 0);
            designationMap.get(side).put(aircraftType, ++index);

            name = designation + index + "-" + model;

            log.info("Squadron: '{}' built for side: {}", name, side);

        } catch (AviationPlantException ex) {
            log.error("Unable to build aircraft model: '{}' for side: {}", model, side);
        }
    }
}
