package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.aircraft.AircraftId;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.AviationPlant;
import engima.waratsea.model.aircraft.AviationPlantException;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.data.SquadronData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an aircraft squadron of a particular class of aircraft.
 */
@Slf4j
public class Squadron implements Asset, PersistentData<SquadronData> {
    @Getter
    private Side side;

    @Getter
    private Nation nation;

    @Getter
    private final String model;

    @Getter
    @Setter
    private Aircraft aircraft;

    @Getter
    private String name;

    @Getter
    @Setter
    private SquadronStrength strength;

    @Getter
    private String location;

    @Getter
    private Airbase airfield;

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
     * @param nation The nation.
     * @param data The data of the squadron read in from a JSON file.
     * @param aviationPlant The aviation plant that creates aircraft for squadrons.
     * @param gameMap The game map.
     */
    @Inject
    public Squadron(@Assisted final Side side,
                    @Assisted final Nation nation,
                    @Assisted final SquadronData data,
                              final AviationPlant aviationPlant,
                              final GameMap gameMap) {
        this.side = side;
        this.nation = nation;
        this.gameMap = gameMap;
        this.model = data.getModel();
        this.strength = data.getStrength();
        this.name = data.getName();
        Optional.ofNullable(data.getAirfield())
                .ifPresent(field -> airfield = gameMap.getAirfield(side, field));

        try {
            AircraftId aircraftId = new AircraftId(model, side);
            aircraft = aviationPlant.load(aircraftId);

            String designation = aircraft.getDesignation();

            int index = designationMap.get(side).getOrDefault(designation, 0);
            designationMap.get(side).put(designation, ++index);

            // Squadrons that have been saved will already have a name.
            // Only newly created squadrons at game start will not have a name.
            if (StringUtils.isBlank(name)) {
                name = designation + index + "-" + model;
            }

            log.debug("Squadron: '{}' with strength: '{}' built for side: {}", new Object[]{name, strength, side});

        } catch (AviationPlantException ex) {
            log.error("Unable to build aircraft model: '{}' for side: {}", model, side);
        }
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public SquadronData getData() {
        SquadronData data = new SquadronData();
        data.setModel(model);
        data.setStrength(strength);
        data.setName(name);
        Optional.ofNullable(airfield)
                .ifPresent(field -> data.setAirfield(field.getName()));
        return data;
    }

    /**
     * Get the type of squadron.
     *
     * @return The type of squadron: FIGHTER, BOMBER, etc.
     */
    public AircraftType getType() {
        return aircraft.getType();
    }

    /**
     * Get the aircraft base type.
     *
     * @return The aircraft base type.
     */
    public AircraftBaseType getBaseType() {
        return getType().getBaseType();
    }

    /**
     * Get the number of steps that the squadron contains. Full strength squadrons contain two steps. Half strength
     * squadrons contain one step. Battleship and cruiser float planes are equal to 1/3 of a step.
     *
     * @return Number of steps in the squadron.
     */
    public BigDecimal getSteps() {
        return strength.getSteps();
    }

    /**
     * Set the squadron's airbase.
     *
     * @param airbase The squadron's airbase.
     */
    public void setAirfield(final Airbase airbase) {
        airfield = airbase;

        String reference = Optional.ofNullable(airbase)
                .map(Airbase::getReference)
                .orElse(null);

        setLocation(reference);
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

    /**
     * Indicates if the squadron is deployed.
     *
     * @return True if the squadron is deployed. False otherwise.
     */
    public boolean isDeployed() {
        return airfield != null;
    }

    /**
     * Indicates if the squadron is available (not deployed).
     *
     * @return True if the squadron is available (not deployed). False otherwise.
     */
    public boolean isAvailable() {
        return airfield == null;
    }

    /**
     * Get the squadron's combat radii. If the squadron can equip with
     * drop tanks then two radii are returned: one with drop tanks and
     * one without.
     *
     * @return The squadron's combat radii.
     */
    public List<Integer> getRadius() {
        return aircraft.getRadius();
    }

    /**
     * The String representation of a squadron.
     *
     * @return The String representation of a squadron.
     */
    @Override
    public String toString() {
        return name + " (" + getType().toString() + ")";
    }

    /**
     * Get the active state of the asset.
     *
     * @return True if the asset is active. False if the asset is not active.
     */
    @Override
    public boolean isActive() {
        return true;
    }
}
