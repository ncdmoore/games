package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetEnemyPort;
import engima.waratsea.model.target.TargetEnemyTaskForce;
import engima.waratsea.utility.FunctionalMap;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an Italian level bomber.
 *
 * Squadron configurations supported:
 *
 *  SquadronConfig.NONE
 *  SquadronConfig.LEAN_ENGINE
 *  SquadronConfig.SEARCH
 */
public class PoorNavalBomber implements Aircraft {
    private final Map<AttackType, FunctionalMap<SquadronConfig, AttackFactor>> attackMap = new HashMap<>();

    @Getter
    private final Set<SquadronConfig> configuration = Set.of(SquadronConfig.NONE, SquadronConfig.LEAN_ENGINE, SquadronConfig.SEARCH);

    private static final int SEARCH_ATTACK_REDUCTION = 2; // Squadron configured for search attack factor reduction.
    private static final int LEAN_ENGINE_FACTOR = 2;
    private static final double POOR_NAVAL_MODIFIER = 2.0 / 6.0;

    private static final Map<Class<?>, Double> FACTOR_MAP = new HashMap<>();

    static {
        FACTOR_MAP.put(TargetEnemyPort.class, 1.0);                         // No penalty when attacking ships in ports.
        FACTOR_MAP.put(TargetEnemyTaskForce.class, POOR_NAVAL_MODIFIER);    // Penalty is applied when attacking ships at sea.
    }

    @Getter private final AircraftId aircraftId;
    @Getter private final AircraftType type;
    @Getter private final String designation;
    @Getter private final Nation nationality;
    @Getter private final ServiceType service;
    @Getter private final AltitudeType altitude;
    @Getter private final LandingType landing;
    @Getter private final LandingType takeoff;
    private final AttackFactor naval;
    private final AttackFactor air;
    private final AttackFactor land;
    @Getter private final Frame frame;
    private final Probability probability;
    private final Performance performance;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param probability Probability utility.
     */
    @Inject
    public PoorNavalBomber(@Assisted final AircraftData data,
                                     final Probability probability) {

        this.aircraftId = data.getAircraftId();
        this.type = data.getType();
        this.designation = data.getDesignation();
        this.nationality = data.getNationality();
        this.service = data.getService();
        this.altitude = data.getAltitude();
        this.landing = data.getLanding();
        this.takeoff = data.getTakeoff();
        this.naval = new AttackFactor(data.getNaval());
        this.land = new AttackFactor(data.getLand());
        this.air = new AttackFactor(data.getAir());
        this.performance = new Performance(data.getPerformance());
        this.frame = new Frame(data.getFrame());

        this.probability = probability;

        probability.setConfigurations(configuration);

        attackMap.put(AttackType.AIR, this::getAir);
        attackMap.put(AttackType.LAND, this::getLand);
        attackMap.put(AttackType.NAVAL, this::getNaval);
    }

    /**
     * Get the aircraft's model.
     *
     * @return The aircraft's model.
     */
    @Override
    public String getModel() {
        return aircraftId.getModel();
    }

    /**
     * Get the aircraft's side.
     *
     * @return The aircraft's side.
     */
    @Override
    public Side getSide() {
        return aircraftId.getSide();
    }

    /**
     * Get the mission roles the aircraft is allowed to perform.
     *
     * @return The mission roles the aircraft is allowed to perform.
     */
    @Override
    public List<MissionRole> getRoles() {
        return Collections.singletonList(MissionRole.MAIN);
    }

    /**
     * Get the aircraft's combat radius. This is a map of how the aircraft
     * is configured to the radius of the aircraft under that configuration.
     *
     *  SquadronConfig => combat radius.
     *
     * @return A map of radii based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getRadius() {

        int radius = performance.getRadius();
        int leanMixtureRadius = radius * LEAN_ENGINE_FACTOR;
        int searchModifier = performance.getSearchModifier(land, naval);
        int searchRadius = radius + searchModifier;

        return Map.of(SquadronConfig.NONE, radius,
                SquadronConfig.LEAN_ENGINE, leanMixtureRadius,
                SquadronConfig.SEARCH, searchRadius);
    }

    /**
     * Get the aircraft's ferry distance. This is a map of how the aircraft
     * is configured to the ferry distance of the aircraft under that configuration.
     *
     *  SquadronConfig => ferry distance.
     *
     * @return A map of ferry distances based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getFerryDistance() {

        int distance = performance.getFerryDistance();
        int leanMixtureDistance = distance * LEAN_ENGINE_FACTOR;
        int searchModifier = performance.getSearchModifier(land, naval);
        int searchDistance = distance + (searchModifier * 2);

        return Map.of(SquadronConfig.NONE, distance,
                SquadronConfig.LEAN_ENGINE, leanMixtureDistance,
                SquadronConfig.SEARCH, searchDistance);
    }

    /**
     * Get the aircraft's range.
     *
     * @return The aircraft's range.
     */
    @Override
    public int getRange() {
        return performance.getGameRange();
    }

    /**
     * Get the aircraft's endurance. This is a map of how the aircraft
     * is configured to the endurance of the aircraft under that configuration.
     *
     *  SquadronConfig => endurance.
     *
     * @return A map of the aircraft's endurance based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getEndurance() {
        int endurance = performance.getEndurance();
        int leanMixtureEndurance = endurance * LEAN_ENGINE_FACTOR;

        return Map.of(SquadronConfig.NONE, endurance,
                SquadronConfig.LEAN_ENGINE, leanMixtureEndurance,
                SquadronConfig.SEARCH, endurance);
    }
    /**
     * Get the probability the aircraft will hit during an attack.
     *
     * @param attackType The attack type.
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in an attack.
     */
    @Override
    public Map<SquadronConfig, Double> getHitProbability(final AttackType attackType, final SquadronStrength strength) {
        return probability.getHitProbability(attackMap.get(attackType).execute(), strength);
    }

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     *
     * @param attackType The attack type.
     * @param target   The target.
     * @param modifier The circumstance air-to-air attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    @Override
    public Map<SquadronConfig, Double> getHitIndividualProbability(final AttackType attackType, final Target target, final int modifier) {

        if (attackType == AttackType.NAVAL) {
            return getNavalHitIndividualProbability(target, modifier);
        }

       return probability.getIndividualHitProbability(attackMap.get(attackType).execute(), modifier);
    }

    /**
     * Get the aircraft's given attack factor specified by the attack type.
     *
     * @param attackType The type of attack: AIR, LAND or NAVAL.
     * @return Get the aircraft's given attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getAttack(final AttackType attackType) {
        return attackMap.get(attackType).execute();
    }

    /**
     * Get the aircraft's air attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    private Map<SquadronConfig, AttackFactor> getAir() {
        return Map.of(SquadronConfig.NONE, air,
                SquadronConfig.LEAN_ENGINE, air,
                SquadronConfig.SEARCH, air);
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    private Map<SquadronConfig, AttackFactor> getLand() {
        AttackFactor leanMixtureAttack = land.getReducedRoundUp(LEAN_ENGINE_FACTOR);
        AttackFactor searchAttack = land.getReducedRoundDown(SEARCH_ATTACK_REDUCTION);

        return Map.of(SquadronConfig.NONE, land,
                SquadronConfig.LEAN_ENGINE, leanMixtureAttack,
                SquadronConfig.SEARCH, searchAttack);
    }

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    private Map<SquadronConfig, AttackFactor> getNaval() {
        AttackFactor leanMixtureAttack = naval.getReducedRoundUp(LEAN_ENGINE_FACTOR);
        AttackFactor searchAttack = naval.getReducedRoundDown(SEARCH_ATTACK_REDUCTION);

        return Map.of(SquadronConfig.NONE, naval,
                SquadronConfig.LEAN_ENGINE, leanMixtureAttack,
                SquadronConfig.SEARCH, searchAttack);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance naval attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a naval attack.
     */
    private Map<SquadronConfig, Double> getNavalHitIndividualProbability(final Target target, final int modifier) {
        double factor = FACTOR_MAP.getOrDefault(target.getClass(), POOR_NAVAL_MODIFIER);
        return probability
                .getIndividualHitProbability(getNaval(), modifier).entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * factor));
    }
}
