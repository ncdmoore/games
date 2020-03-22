package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetEnemyPort;
import engima.waratsea.model.target.TargetEnemyTaskForce;

import java.util.HashMap;
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
public class PoorNavalBomber extends Bomber {
    private static final Set<SquadronConfig> CONFIGS = Set.of(SquadronConfig.NONE, SquadronConfig.LEAN_ENGINE, SquadronConfig.SEARCH);

    private static final int SEARCH_ATTACK_REDUCTION = 2; // Squadron configured for search attack factor reduction.
    private static final int LEAN_ENGINE_FACTOR = 2;
    private static final double POOR_NAVAL_MODIFIER = 2.0 / 6.0;

    private static final Map<Class<?>, Double> FACTOR_MAP = new HashMap<>();

    static {
        FACTOR_MAP.put(TargetEnemyPort.class, 1.0);                         // No penalty when attacking ships in ports.
        FACTOR_MAP.put(TargetEnemyTaskForce.class, POOR_NAVAL_MODIFIER);    // Penalty is applied when attacking ships at sea.
    }

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param probability Probability utility.
     */
    @Inject
    public PoorNavalBomber(@Assisted final AircraftData data,
                                     final Probability probability) {
        super(data, probability);

        getProbability().setConfigurations(CONFIGS);
    }

    /**
     * Get the aircraft's air attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getAir() {
        AttackFactor air = super.getAir().get(SquadronConfig.NONE);

        return Map.of(SquadronConfig.NONE, air,
                      SquadronConfig.LEAN_ENGINE, air,
                      SquadronConfig.SEARCH, air);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getAirHitProbability(final SquadronStrength strength) {
        return getProbability().getHitProbability(getAir(), strength);
    }

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     * @param target   The target.
     * @param modifier The circumstance air-to-air attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    @Override
    public Map<SquadronConfig, Double> getAirHitIndividualProbability(final Target target, final int modifier) {
       return getProbability().getIndividualHitProbability(getAir(), modifier);
    }

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getNaval() {
        AttackFactor attack = super.getNaval().get(SquadronConfig.NONE);
        AttackFactor leanMixtureAttack = attack.getReducedRoundUp(LEAN_ENGINE_FACTOR);
        AttackFactor searchAttack = attack.getReducedRoundDown(SEARCH_ATTACK_REDUCTION);

        return Map.of(SquadronConfig.NONE, attack,
                      SquadronConfig.LEAN_ENGINE, leanMixtureAttack,
                      SquadronConfig.SEARCH, searchAttack);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getNavalHitProbability(final SquadronStrength strength) {
        return getProbability().getHitProbability(getNaval(), strength);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance naval attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getNavalHitIndividualProbability(final Target target, final int modifier) {
        double factor = FACTOR_MAP.getOrDefault(target.getClass(), POOR_NAVAL_MODIFIER);
        return getProbability()
                .getIndividualHitProbability(getNaval(), modifier).entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * factor));
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getLand() {
        AttackFactor attack = super.getLand().get(SquadronConfig.NONE);
        AttackFactor leanMixtureAttack = attack.getReducedRoundUp(LEAN_ENGINE_FACTOR);
        AttackFactor searchAttack = attack.getReducedRoundDown(SEARCH_ATTACK_REDUCTION);

        return Map.of(SquadronConfig.NONE, attack,
                      SquadronConfig.LEAN_ENGINE, leanMixtureAttack,
                      SquadronConfig.SEARCH, searchAttack);
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a land attack.
     */
    @Override
    public Map<SquadronConfig, Double> getLandHitProbability(final SquadronStrength strength) {
        return getProbability().getHitProbability(getLand(), strength);
    }

    /**
     * Get the probability the aircraft will hit during a land attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance land attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a land attack.
     */
    @Override
    public Map<SquadronConfig, Double> getLandHitIndividualProbability(final Target target, final int modifier) {
       return getProbability().getIndividualHitProbability(getLand(), modifier);
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
        AttackFactor land = getLand().get(SquadronConfig.NONE);
        AttackFactor naval = getNaval().get(SquadronConfig.NONE);

        int radius = this.getPerformance().getRadius();
        int leanMixtureRadius = radius * LEAN_ENGINE_FACTOR;
        int searchModifier = getPerformance().getSearchModifier(land, naval);
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
        AttackFactor land = getLand().get(SquadronConfig.NONE);
        AttackFactor naval = getNaval().get(SquadronConfig.NONE);

        int distance = this.getPerformance().getFerryDistance();
        int leanMixtureDistance = distance * LEAN_ENGINE_FACTOR;
        int searchModifier = getPerformance().getSearchModifier(land, naval);
        int searchDistance = distance + (searchModifier * 2);

        return Map.of(SquadronConfig.NONE, distance,
                SquadronConfig.LEAN_ENGINE, leanMixtureDistance,
                SquadronConfig.SEARCH, searchDistance);
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
        int endurance = getPerformance().getEndurance();
        int leanMixtureEndurance = endurance * LEAN_ENGINE_FACTOR;

        return Map.of(SquadronConfig.NONE, endurance,
                SquadronConfig.LEAN_ENGINE, leanMixtureEndurance,
                SquadronConfig.SEARCH, endurance);
    }
}
