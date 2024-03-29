package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.Aircraft;
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
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an aircraft squadron of a particular class of aircraft.
 */
@Slf4j
public class Squadron implements Comparable<Squadron>, Asset, PersistentData<SquadronData> {
    private static final Map<Side, Map<String, Integer>> DESIGNATION_MAP = Map.of(
            Side.ALLIES, new HashMap<>(),
            Side.AXIS, new HashMap<>()
    );

    private final GameMap gameMap;
    private final GameRules rules;
    @Getter private final Side side;
    @Getter private final Nation nation;
    @Getter private final String model;
    @Getter private Aircraft aircraft;
    @Getter private String name;
    @Getter @Setter private SquadronStrength strength;             //The squadron's actual physical strength.
    @Getter private transient SquadronStrength effectiveStrength;  //The effective squadron strength as determined by battlefield conditions such as flak.
    @Getter private String reference;                              //This is always a map reference and never a name.
    @Getter private Airbase home;
    @Getter private SquadronState state;
    @Getter @Setter private SquadronConfig config;
    @Getter @Setter private int missionId;

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
                    final GameRules rules) {
        this.gameMap = gameMap;
        this.rules = rules;
        this.side = side;
        this.nation = nation;
        this.model = data.getModel();
        this.strength = data.getStrength();
        this.effectiveStrength = data.getStrength();
        this.name = data.getName();
        this.state = data.getSquadronState();
        this.config = Optional.ofNullable(data.getConfig()).orElse(SquadronConfig.NONE);
        this.missionId = data.getMissionId();

        try {
            AircraftId aircraftId = new AircraftId(model, side);
            aircraft = aviationPlant.load(aircraftId);

            String designation = aircraft.getDesignation();

            int index = DESIGNATION_MAP.get(side).getOrDefault(designation, 0);
            DESIGNATION_MAP.get(side).put(designation, ++index);

            // Squadrons that have been saved will already have a name.
            // Only newly created squadrons at game start will not have a name.
            if (StringUtils.isBlank(name)) {
                name = designation + index + "-" + aircraft.getName();
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
        Optional.ofNullable(home)
                .ifPresent(h -> data.setAirfield(h.getName()));
        data.setSquadronState(state);
        data.setConfig(config);
        data.setMissionId(missionId);
        return data;
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
     * Get the landing type of this squadron.
     *
     * @return The squadron's landing type.
     */
    public LandingType getLandingType() {
        return aircraft.getLanding();
    }

    /**
     * Get a squadron attack factor.
     *
     * @param attackType The type of attack factor.
     * @return The attack factor.
     */
    public SquadronAttack getAttack(final AttackType attackType) {
        return new SquadronAttack(
                aircraft
                .getAttack(attackType)
                .get(config))
                .setStrength(strength);
    }

    /**
     * Get a squadron attack factor.
     *
     * @param attackType The type of attack.
     * @param squadronConfig A squadron configuration.
     * @return The attack factor.
     */
    public SquadronAttack getAttack(final AttackType attackType, final SquadronConfig squadronConfig) {
        return new SquadronAttack(
                aircraft
                .getAttack(attackType)
                .get(squadronConfig))
                .setStrength(strength);
    }

    /**
     * Get the squadron's hit probability.
     *
     * @param attackType The attack type.
     * @param squadronConfig A squadron configuration.
     * @return The squadron's hit probability.
     */
    public double getHitProbability(final AttackType attackType, final SquadronConfig squadronConfig) {
        return aircraft.getHitProbability(attackType, strength).get(squadronConfig);
    }

    /**
     * Get the squadron's air hit probability.
     *
     *
     * @param attackType The attack type.
     * @param target The target.
     * @param modifier Any game modifications to the hit probability: weather, target type, etc...
     * @return The squadron's air hit probability.
     */
    public double getHitIndividualProbability(final AttackType attackType, final Target target, final int modifier) {
        return aircraft.getHitIndividualProbability(attackType, target, modifier).get(config);
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
     * Get the number of aircraft that the squadron contains. Full strength squadrons contain 12 aircraft. Half strength
     * squadrons contain 6 aircraft. Sixth strength squadrons (Battleship and cruiser float planes) contain two aircraft.
     *
     * @return Number of aircraft in the squadron.
     */
    public int getAircraftNumber() {
        return strength.getAircraft();
    }

    /**
     * Get the number of steps that the squadron contains. Full strength squadrons contain two steps. Half strength
     * squadrons contain one step. Battleship and cruiser float planes are equal to 0 steps.
     *
     * @return Number of steps in the squadron.
     */
    public int getSteps() {
        return strength.getSteps();
    }

    /**
     * Determine if the squadron is ready for go on patrol or fly a mission.
     *
     * @param desiredState The state to check if the squadron has.
     * @return True if the squadron is in the desired state. False otherwise.
     */
    public boolean isAtState(final SquadronState desiredState) {
        return state == desiredState || desiredState == SquadronState.ALL;
    }

    /**
     * Set the squadron's airbase.
     *
     * @param squadronHome The squadron's airbase.
     */
    public void setHome(final Airbase squadronHome) {
        this.home = squadronHome;

        String mapReference = Optional.ofNullable(squadronHome)
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
     * Set the squadron's state.
     *
     * @param action A squadron action that affects the squadron's state.
     */
    public void setState(final SquadronAction action) {
         state = state.transition(action);
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
        return home != null;
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
        return home == null;
    }

    /**
     * Get teh squadron's range with its current configuration.
     *
     * @return The squadron's range with its current configuration.
     */
    public int getRange() {
        return aircraft.getRange().get(config);
    }

    /**
     * Get the squadron's range given a configuration.
     *
     * @param squadronConfig A squadron configuration.
     * @return The squadron range given a squadron configuration.
     */
    public int getRange(final SquadronConfig squadronConfig) {
        return aircraft.getRange().get(squadronConfig);
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
        String targetReference = gameMap.convertNameToReference(target.getReference());
        String airbaseReference = home.getReference();

        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setAirfieldType(home.getAirbaseType())
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
     * Equip this squadron for the given patrol.
     *
     * @param patrol The patrol that contains the squadron.
     */
    public void equip(final Patrol patrol) {
        config = patrol.getBestSquadronConfig();
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
     * The squadron takes off.
     *
     * When a squadron takes off its effective strength is set to its physical strength.
     * Effective strength is changed by battlefield conditions such as flak.
     */
    public void takeOff() {
        effectiveStrength = strength;  // Ensure that on take off the effective strength has the correct value.
        state = state.transition(SquadronAction.TAKE_OFF);
    }

    /**
     * The squadron attacks an enemy airfield.
     *
     * @param enemyAirfield The enemy airfield that is attacked.
     */
    public void attack(final AirfieldView enemyAirfield) {

    }

    /**
     * The squadron lands.
     */
    public void land() {
        state = state.transition(SquadronAction.LAND);
        missionId = 0;
    }

    /**
     * The squadron's effective strength is reduced.
     */
    public void reduceEffectiveStrength() {
        effectiveStrength = effectiveStrength.reduce();
    }

    /**
     * The squadron's strength is reduced.
     */
    public void reduceStrength() {
        strength = strength.reduce();
        if (strength == SquadronStrength.ZERO) {
            state = state.transition(SquadronAction.SHOT_DOWN);
        }
    }

    /**
     * Indicates if the squadron is effective.
     *
     * @return True if the squadron is effective. False otherwise.
     */
    public boolean isEffective() {
        return effectiveStrength != SquadronStrength.ZERO;
    }

    /**
     * Indicates if the squadron is effective.
     *
     * @return True if the squadron is not effective. False otherwise.
     */
    public boolean isNotEffective() {
        return effectiveStrength == SquadronStrength.ZERO;
    }

    /**
     * Indicates if the squadron is destroyed.
     *
     * @return True if the squadron is destroyed. False otherwise.
     */
    public boolean isDestroyed() {
        return strength == SquadronStrength.ZERO;
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
        String targetReference = gameMap.convertNameToReference(target.getReference());
        String airbaseReference = home.getReference();

        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setAirfieldType(home.getAirbaseType())
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

    /**
     * Determine if this squadron is equal to a given object.
     *
     * @param o The given object.
     * @return True if this squadron is equal to the given object.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Squadron squadron = (Squadron) o;

        return side == squadron.side && nation == squadron.nation && model.equals(squadron.model) && name.equals(squadron.name);
    }

    /**
     * The hash code for squadrons.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(side, nation, model, name);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull final Squadron o) {

        if (getTitle() == null || o.getTitle() == null) {
            return 0;
        }

        return getTitle().compareTo(o.getTitle());
    }
}
