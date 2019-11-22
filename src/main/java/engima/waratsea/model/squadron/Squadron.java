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
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Rules;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an aircraft squadron of a particular class of aircraft.
 */
@Slf4j
public class Squadron implements Asset, PersistentData<SquadronData> {
    private Rules rules;

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
    private String reference; //This is always a map reference and never a name.

    @Getter
    private Airbase airfield;

    private GameMap gameMap;

    private static final Map<Side, Map<String, Integer>> DESIGNATION_MAP = new HashMap<>();

    @Getter
    @Setter
    private SquadronState squadronState;

    static {
        DESIGNATION_MAP.put(Side.ALLIES, new HashMap<>());
        DESIGNATION_MAP.put(Side.AXIS, new HashMap<>());
    }

    /**
     * Initialize the designation maps for both sides.
     *
     * @param side The side ALLIES or AXIS.
     */
    public static void init(final Side side) {
        DESIGNATION_MAP.get(side).clear();
    }

    /**
     * Constructor called by guice.
     *
     * @param side The side ALLIES or AXIS.
     * @param nation The nation.
     * @param data The data of the squadron read in from a JSON file.
     * @param aviationPlant The aviation plant that creates aircraft for squadrons.
     * @param gameMap The game map.
     * @param rules The game rules.
     */
    @Inject
    public Squadron(@Assisted final Side side,
                    @Assisted final Nation nation,
                    @Assisted final SquadronData data,
                              final AviationPlant aviationPlant,
                              final GameMap gameMap,
                              final Rules rules) {
        this.rules = rules;
        this.side = side;
        this.nation = nation;
        this.gameMap = gameMap;
        this.model = data.getModel();
        this.strength = data.getStrength();
        this.name = data.getName();
        this.squadronState = data.getSquadronState();

        try {
            AircraftId aircraftId = new AircraftId(model, side);
            aircraft = aviationPlant.load(aircraftId);

            String designation = aircraft.getDesignation();

            int index = DESIGNATION_MAP.get(side).getOrDefault(designation, 0);
            DESIGNATION_MAP.get(side).put(designation, ++index);

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
        data.setNation(nation);
        data.setModel(model);
        data.setStrength(strength);
        data.setName(name);
        Optional.ofNullable(airfield)
                .ifPresent(field -> data.setAirfield(field.getName()));
        data.setSquadronState(squadronState);
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }

    /**
     * Get the squadron's title.
     *
     * @return The squadron's title.
     */
    @Override
    public String getTitle() {
        return getName();
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
     * Get the landing type of this squadron.
     *
     * @return The squadron's landing type.
     */
    public LandingType getLandingType() {
        return aircraft.getLanding();
    }

    /**
     * Get the squadron air-to-air attack factor.
     *
     * @return The air-to-air attack factor.
     */
    public int getAirFactor() {
        return aircraft.getAir().getFactor(strength);
    }

    /**
     * Get the squadron air-to-air attack modifier.
     *
     * @return The air-to-air attack modifier.
     */
    public int getAirModifier() {
        return aircraft.getAir().getModifier();
    }

    /**
     * Determine if the squadron's air-to-air attack is defensive only.
     *
     * @return True if the squadron's air-to-air attack is defensive only. False otherwise.
     */
    public boolean isAirDefensive() {
        return aircraft.getAir().isDefensive();
    }

    /**
     * Get the squadron's air-to-air hit probability.
     *
     * @return The squadron's air-to-air hit probability.
     */
    public int getAirHitProbability() {
        return aircraft.getAirHitProbability(strength);
    }

    /**
     * Get the squadron's land hit probability.
     *
     * @return The squadron's land hit probability.
     */
    public int getLandHitProbability() {
        return aircraft.getLandHitProbability(strength);
    }

    /**
     * Get the squadron's naval hit probability.
     *
     * @return The squadron's naval hit probability.
     */
    public int getNavalHitProbability() {
        return aircraft.getNavalHitProbability(strength);
    }

    /**
     * Get the squadron naval attack factor.
     *
     * @return The naval attack factor.
     */
    public int getNavalFactor() {
        return aircraft.getNaval().getFactor(strength);
    }

    /**
     * Get the squadron naval attack modifier.
     *
     * @return The naval attack modifier.
     */
    public int getNavalModifier() {
        return aircraft.getNaval().getModifier();
    }

    /**
     * Get the squadron land attack factor.
     *
     * @return The land attack factor.
     */
    public int getLandFactor() {
        return aircraft.getLand().getFactor(strength);
    }

    /**
     * Get the squadron land attack modifier.
     *
     * @return The land attack modifier.
     */
    public int getLandModifier() {
        return aircraft.getLand().getModifier();
    }

    /**
     * Determine if this squadron may perform ASW.
     *
     * @param patrolType Type of patrol.
     * @return True if this squadron may perform ASW. False otherwise.
     */
    public boolean canDoPatrol(final PatrolType patrolType) {
        return rules.patrolFilter(patrolType, this);
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
     * Determine if the squadron is ready for go on patrol or fly a mission.
     *
     * @return True if the squadron is ready. False otherwise.
     */
    public boolean isReady() {
        return squadronState == SquadronState.READY;
    }

    /**
     * Set the squadron's airbase.
     *
     * @param airbase The squadron's airbase.
     */
    public void setAirfield(final Airbase airbase) {
        airfield = airbase;

        String mapReference = Optional.ofNullable(airbase)
                .map(Airbase::getReference)
                .orElse(null);

        setReference(mapReference);
    }

    /**
     * Set the squadron's reference..
     *
     * @param newLocation The squadron's new reference.
     */
    public void setReference(final String newLocation) {
        reference = Optional.ofNullable(newLocation)
                .map(gameMap::convertNameToReference)
                .orElse(null);
    }

    /**
     * Determine if the squadron is at an enemy base.
     *
     * @return True if the squadron is currently located at an enemy base. False otherwise.
     */
    public boolean atEnemyBase() {
        return gameMap.isLocationBase(side.opposite(), reference);
    }

    /**
     * Determine if the squadron is at a friendly base.
     *
     * @return True if the squadron is currently located at a friendly base. False otherwise.
     */
    public boolean atFriendlyBase() {
        return gameMap.isLocationBase(side, reference);
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
     * Indicates if the squadron is a seaplane squadron.
     *
     * @param landingType The type of landing type.
     * @return True if the squadron is composed of seaplanes. False otherwise.
     */
    public boolean isLandingTypeCompatible(final LandingType landingType) {

        if (landingType == getLandingType()) {
            return true;
        }

        return getLandingType() == LandingType.CARRIER
                 && landingType == LandingType.LAND;
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
     * Get the squadron's maximum radius. Some aircraft may equip with drop tanks and have two radii.
     * This method gets the maximum of the two radii.
     *
     * @return The squadron's maximum radius.
     */
    public int getMaxRadius() {
        return aircraft
                .getRadius()
                .stream()
                .max(Comparator.comparing(radius -> radius))
                .orElse(0);
    }

    /**
     * Returns true if this squadron belongs to the given nation.
     *
     * @param targetNation A nation: BRITISH, ITALIAN, etc.
     * @return True if this squadron belongs to the given nation. False otherwise.
     */
    public boolean ofNation(final Nation targetNation) {
        return nation == targetNation;
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
