package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.aircraft.AircraftId;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.aircraft.AviationPlant;
import engima.waratsea.model.aircraft.AviationPlantException;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.rules.Rules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an aircraft squadron of a particular class of aircraft.
 */
@Slf4j
public class Squadron implements Asset, PersistentData<SquadronData> {
    private static final Map<Side, Map<String, Integer>> DESIGNATION_MAP = new HashMap<>();

    private GameMap gameMap;
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

    @Getter
    @Setter
    private SquadronState squadronState;

    @Getter
    @Setter
    private boolean longDistance;

    @Getter
    @Setter
    private SquadronConfig config;

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
        this.gameMap = gameMap;
        this.rules = rules;
        this.side = side;
        this.nation = nation;
        this.model = data.getModel();
        this.strength = data.getStrength();
        this.name = data.getName();
        this.squadronState = data.getSquadronState();
        this.config = Optional.ofNullable(data.getConfig()).orElse(SquadronConfig.NONE);

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
        data.setSide(side);
        data.setNation(nation);
        data.setModel(model);
        data.setStrength(strength);
        data.setName(name);
        Optional.ofNullable(airfield)
                .ifPresent(field -> data.setAirfield(field.getName()));
        data.setSquadronState(squadronState);
        data.setConfig(config);
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
        return aircraft
                .getAir()
                .get(config)
                .getFactor(strength);
    }

    /**
     * Get the squadron air-to-air attack factor.
     *
     * @param squadronConfig A squadron configuration.
     * @return The air-to-air attack factor.
     */
    public int getAirFactor(final SquadronConfig squadronConfig) {
        return aircraft
                .getAir()
                .get(squadronConfig)
                .getFactor(strength);
    }

    /**
     * Get the squadron air-to-air attack modifier.
     *
     * @param squadronConfig A squadron configuration
     * @return The air-to-air attack modifier.
     */
    public int getAirModifier(final SquadronConfig squadronConfig) {
        return aircraft
                .getAir()
                .get(squadronConfig)
                .getModifier();
    }

    /**
     * Determine if the squadron's air-to-air attack is defensive only.
     * The air defensive parameter does not very with squadron configuration.
     *
     * @return True if the squadron's air-to-air attack is defensive only. False otherwise.
     */
    public boolean isAirDefensive() {
        return aircraft
                .getAir()
                .get(SquadronConfig.NONE)  // Does not vary with squadron configuration.
                .isDefensive();
    }

    /**
     * Get the squadron's air-to-air hit probability.
     *
     * @param attackType The attack type.
     * @param squadronConfig A squadron configuration.
     * @return The squadron's air-to-air hit probability.
     */
    public double getHitProbability(final AttackType attackType, final SquadronConfig squadronConfig) {
        return aircraft.getHitProbability(attackType, strength).get(squadronConfig);
    }

    /**
     * Get the squadron's air hit probability.
     *
     * @param target The target.
     * @param modifier Any game modifications to the hit probability: weather, target type, etc...
     * @return The squadron's air hit probability.
     */
    public double getAirHitIndividualProbability(final Target target, final  int modifier) {
        return aircraft.getAirHitIndividualProbability(target, modifier).get(config);
    }


    /**
     * Get the squadron's land hit probability.
     *
     * @param target The target.
     * @param modifier Any game modifications to the hit probability: weather, target type, etc...
     * @return The squadron's land hit probability.
     */
    public double getLandHitIndividualProbability(final Target target, final int modifier) {
        return aircraft.getLandHitIndividualProbability(target, modifier).get(config);
    }

    /**
     * Get the squadron's naval hit probability.
     *
     *
     * @param target The target
     * @param modifier Any game modifications to the hit probability: weather, target type, etc...
     * @return The squadron's naval hit probability.
     */
    public double getNavalHitIndividualProbability(final Target target, final int modifier) {
        return aircraft.getNavalHitIndividualProbability(target, modifier).get(config);
    }

    /**
     * Get the squadron naval attack factor.
     *
     * @return The naval attack factor.
     */
    public int getNavalFactor() {
        return aircraft
                .getNaval()
                .get(config)
                .getFactor(strength);
    }

    /**
     * Get the squadron naval attack factor.
     *
     * @param squadronConfig A squadron configuration.
     * @return The naval attack factor for the given squadron configuration.
     */
    public int getNavalFactor(final SquadronConfig squadronConfig) {
        return aircraft
                .getNaval()
                .get(squadronConfig)
                .getFactor(strength);
    }

    /**
     * Get the squadron naval attack modifier.
     *
     * @param squadronConfig A squadron configuration.
     * @return The naval attack modifier.
     */
    public int getNavalModifier(final SquadronConfig squadronConfig) {
        return aircraft
                .getNaval()
                .get(squadronConfig)
                .getModifier();
    }

    /**
     * Get the squadron land attack factor.
     *
     * @return The land attack factor.
     */
    public int getLandFactor() {
        return aircraft
                .getLand()
                .get(config)
                .getFactor(strength);
    }

    /**
     * Get the squadron land attack factor given a squadron configuration.
     *
     * @param squadronConfig A squadron configuration.
     * @return The land attack factor for the given squadron configuration.
     */
    public int getLandFactor(final SquadronConfig squadronConfig) {
        return aircraft
                .getLand()
                .get(squadronConfig)
                .getFactor(strength);
    }

    /**
     * Get the squadron land attack modifier given a squadron configuration.
     *
     * @param squadronConfig A squadron configuration.
     * @return The land attack modifier for the given squadron configuration.
     */
    public int getLandModifier(final SquadronConfig squadronConfig) {
        return aircraft
                .getLand()
                .get(squadronConfig)
                .getModifier();
    }

    /**
     * Determine if this squadron may perform the given mission.
     *
     * @param missionType Type of mission.
     * @return True if this squadron may perform the given mission. False otherwise.
     */
    public boolean canDoMission(final AirMissionType missionType) {
        return rules.missionFilter(missionType, this);
    }

    /**
     * Determine if this squadron may perform the given mission role.
     *
     * @param missionRole The squadron mission role.
     * @return True if this squadron may perform the given mission role. False otherwise.
     */
    public boolean canDoRole(final MissionRole missionRole) {
        return aircraft.getRoles().contains(missionRole);
    }

    /**
     * Determine if this squadron may perform the given patrol.
     *
     * @param patrolType Type of patrol.
     * @return True if this squadron may perform the given patrol. False otherwise.
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
     * Get the squadron's radius given a configuration.
     *
     * @param squadronConfig A squadron configuration.
     * @return The squadron radius given a squadron configuration.
     */
    public int getRadius(final SquadronConfig squadronConfig) {
        return aircraft.getRadius().get(squadronConfig);
    }

    /**
     * Get the squadron's combat radius with its current configuration.
     *
     * @return The squadron's combat radius with its current configuration.
     */
    public int getRadius() {
        return aircraft.getRadius().get(config);
    }

    /**
     * Get all of the squadron's possible radii.
     *
     * @return The squadron's possible radii.
     */
    public Map<SquadronConfig, Integer> getRadii() {
        return aircraft.getRadius();
    }

    /**
     * Get the squadron's endurance given a squadron configuration.
     *
     * @param squadronConfig A squadron configuration.
     * @return The squadron endurance given a squadron configuration.
     */
    public int getEndurance(final SquadronConfig squadronConfig) {
        return aircraft.getEndurance().get(squadronConfig);
    }

    /**
     * Determine if this squadron is in range of the given target.
     *
     * @param target The squadron's target.
     * @param missionType The squadron's mission type.
     * @param missionRole The squadron's mission role.
     * @return True if this squadron is in range of the given target.
     */
    public boolean inRange(final Target target, final AirMissionType missionType, final MissionRole missionRole) {
        String targetReference = gameMap.convertNameToReference(target.getLocation());
        String airbaseReference = airfield.getReference();

        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setAirfieldType(airfield.getAirfieldType())
                .setMissionRole(missionRole)
                .setMissionType(missionType);

        Set<SquadronConfig> allowedConfigs = rules.getAllowedSquadronConfig(dto);

        // Determine if the target requires a round trip.
        Map<SquadronConfig, Integer> rangeMap = target.requiresRoundTrip()
                ? aircraft.getRadius()
                : aircraft.getFerryDistance();

        return rangeMap
                .entrySet()
                .stream()
                .filter(entry -> allowedConfigs.contains(entry.getKey()))
                .anyMatch(entry -> gameMap.inRange(airbaseReference, targetReference, entry.getValue()));
    }

    /**
     * Equip this squadron for the given mission and role.
     *
     * @param target The squadron's target.
     * @param missionType The squadron's mission type.
     * @param missionRole The squadron's mission role.
     */
    public void equip(final Target target, final AirMissionType missionType, final MissionRole missionRole) {
        config = determineConfig(target, missionType, missionRole);
    }

    /**
     * Un-equip this squadron.
     */
    public void unEquip() {
        config = SquadronConfig.NONE;
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

    /**
     * Determine the squadron's needed config based on its target, mission type and mission role.
     *
     * @param target The squadron's target.
     * @param missionType The squadron's mission type.
     * @param missionRole The squadron's mission role.
     * @return The squadron's needed config in order to reach the target given its mission and role.
     */
    public SquadronConfig determineConfig(final Target target, final AirMissionType missionType, final MissionRole missionRole) {
        String targetReference = gameMap.convertNameToReference(target.getLocation());
        String airbaseReference = airfield.getReference();

        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setAirfieldType(airfield.getAirfieldType())
                .setMissionRole(missionRole)
                .setMissionType(missionType);

        Set<SquadronConfig> allowedConfigs = rules.getAllowedSquadronConfig(dto);

        // Determine if the target requires a round trip.
        Map<SquadronConfig, Integer> rangeMap = target.requiresRoundTrip()
                ? aircraft.getRadius()
                : aircraft.getFerryDistance();

        // Look for the squadron configuration with the highest priority. This should be the best
        // configuration for this squadron. It should be the configuration that offers the best
        // chance of success for the mission.
        return rangeMap
                .entrySet()
                .stream()
                .filter(entry -> allowedConfigs.contains(entry.getKey()))
                .filter(entry -> gameMap.inRange(airbaseReference, targetReference, entry.getValue()))
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .orElse(SquadronConfig.NONE);
    }
}
