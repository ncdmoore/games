package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.aircraft.data.AttackFactorData;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetEnemyPort;
import engima.waratsea.model.target.TargetEnemyTaskForce;
import engima.waratsea.utility.Dice;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Italian level bomber.
 *
 * Squadron configurations supported:
 *
 *  SquadronConfig.NONE
 *  SquadronConfig.LEAN_ENGINE
 */
public class PoorNavalBomber extends Bomber {
    private static final int LEAN_ENGINE_FACTOR = 2;
    private static final int BASE_MODIFIER = 1; // 6 always hits on a 6-sided die.
    private static final double POOR_NAVAL_MODIFIER = 2.0 / 6.0;

    private static final Map<Class<?>, Double> FACTOR_MAP = new HashMap<>();

    static {
        FACTOR_MAP.put(TargetEnemyPort.class, 1.0);                       // No penalty when attacking ships in ports.
        FACTOR_MAP.put(TargetEnemyTaskForce.class, POOR_NAVAL_MODIFIER);    // Penalty is applied when attacking ships at sea.
    }

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param dice Dice utility.
     */
    @Inject
    public PoorNavalBomber(@Assisted final AircraftData data,
                                     final Dice dice) {
        super(data, dice);
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
                SquadronConfig.LEAN_ENGINE, air);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getAirHitProbability(final SquadronStrength strength) {
        double prob = getProbability(getAir().get(SquadronConfig.NONE), strength);
        double probLeanMixture = getProbability(getAir().get(SquadronConfig.LEAN_ENGINE), strength);

        return Map.of(SquadronConfig.NONE, prob,
                SquadronConfig.LEAN_ENGINE, probLeanMixture);
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
        double prob = getIndividualProbability(getAir().get(SquadronConfig.NONE).getModifier() + BASE_MODIFIER + modifier);
        double probLeanMixture = getIndividualProbability(getAir().get(SquadronConfig.LEAN_ENGINE).getModifier() + BASE_MODIFIER + modifier);

        return Map.of(SquadronConfig.NONE, prob,
                SquadronConfig.LEAN_ENGINE, probLeanMixture);    }

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getNaval() {
        AttackFactor attack = super.getNaval().get(SquadronConfig.NONE);

        AttackFactorData data = new AttackFactorData();
        data.setModifier(attack.getModifier());
        data.setDefensive(attack.isDefensive());
        data.setFull((attack.getFull() / LEAN_ENGINE_FACTOR) + attack.getFull() % LEAN_ENGINE_FACTOR);
        data.setHalf((attack.getHalf() / LEAN_ENGINE_FACTOR) + attack.getHalf() % LEAN_ENGINE_FACTOR);
        data.setSixth((attack.getSixth() / LEAN_ENGINE_FACTOR) + attack.getSixth() % LEAN_ENGINE_FACTOR);

        AttackFactor leanMixtureAttack = new AttackFactor(data);

        return Map.of(SquadronConfig.NONE, attack,
                      SquadronConfig.LEAN_ENGINE, leanMixtureAttack);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getNavalHitProbability(final SquadronStrength strength) {
        double prob = getProbability(getNaval().get(SquadronConfig.NONE), strength);
        double probLeanMixture = getProbability(getNaval().get(SquadronConfig.LEAN_ENGINE), strength);

        return Map.of(SquadronConfig.NONE, prob,
                      SquadronConfig.LEAN_ENGINE, probLeanMixture);

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

        double prob = getIndividualProbability(getNaval().get(SquadronConfig.NONE).getModifier() + BASE_MODIFIER + modifier) * factor;
        double probLeanMixture = getIndividualProbability(getNaval().get(SquadronConfig.LEAN_ENGINE).getModifier() + BASE_MODIFIER + modifier) * factor;

        return Map.of(SquadronConfig.NONE, prob,
                      SquadronConfig.LEAN_ENGINE, probLeanMixture);
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getLand() {
        AttackFactor attack = super.getLand().get(SquadronConfig.NONE);

        AttackFactorData data = new AttackFactorData();
        data.setModifier(attack.getModifier());
        data.setDefensive(attack.isDefensive());
        data.setFull((attack.getFull() / LEAN_ENGINE_FACTOR) + attack.getFull() % LEAN_ENGINE_FACTOR);
        data.setHalf((attack.getHalf() / LEAN_ENGINE_FACTOR) + attack.getHalf() % LEAN_ENGINE_FACTOR);
        data.setSixth((attack.getSixth() / LEAN_ENGINE_FACTOR) + attack.getSixth() % LEAN_ENGINE_FACTOR);

        AttackFactor leanMixtureAttack = new AttackFactor(data);

        return Map.of(SquadronConfig.NONE, attack,
                      SquadronConfig.LEAN_ENGINE, leanMixtureAttack);
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a land attack.
     */
    @Override
    public Map<SquadronConfig, Double> getLandHitProbability(final SquadronStrength strength) {
        double prob = getProbability(getLand().get(SquadronConfig.NONE), strength);
        double probLeanMixture = getProbability(getLand().get(SquadronConfig.LEAN_ENGINE), strength);

        return Map.of(SquadronConfig.NONE, prob,
                      SquadronConfig.LEAN_ENGINE, probLeanMixture);
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
        double prob = getIndividualProbability(getLand().get(SquadronConfig.NONE).getModifier() + BASE_MODIFIER + modifier);
        double probLeanMixture = getIndividualProbability(getLand().get(SquadronConfig.LEAN_ENGINE).getModifier() + BASE_MODIFIER + modifier);

        return Map.of(SquadronConfig.NONE, prob,
                      SquadronConfig.LEAN_ENGINE, probLeanMixture);
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
        int radius = this.getPerformance().getRadius();
        int leanMixtureRadius = radius * LEAN_ENGINE_FACTOR;
        return Map.of(SquadronConfig.NONE, radius,
                SquadronConfig.LEAN_ENGINE, leanMixtureRadius);
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
        int distance = this.getPerformance().getFerryDistance();
        int leanMixtureDistance = distance * LEAN_ENGINE_FACTOR;
        return Map.of(SquadronConfig.NONE, distance,
                SquadronConfig.LEAN_ENGINE, leanMixtureDistance);
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
                SquadronConfig.LEAN_ENGINE, leanMixtureEndurance);
    }

    /**
     * Get the probability.
     *
     * @param attackFactor  The squadron attack factor.
     * @param strength The squadron strength.
     * @return The probability.
     */
    private double getProbability(final AttackFactor attackFactor, final SquadronStrength strength) {
        int numHit = attackFactor.getModifier() + BASE_MODIFIER;
        int numToRoll = attackFactor.getFactor(strength);
        return getDice().probability(numHit, numToRoll);
    }

    /**
     * Get the individual probability.
     *
     * @param numHit The number of outcomes/rolls that hit.
     * @return The individual probability.
     */
    private double getIndividualProbability(final int numHit) {
        return getDice().individualProbability(numHit);
    }
}
