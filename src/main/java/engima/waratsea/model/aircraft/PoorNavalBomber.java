package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.aircraft.data.AttackFactorData;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.configuration.SquadronConfig;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetEnemyPort;
import engima.waratsea.model.target.TargetEnemyTaskForce;
import engima.waratsea.utility.Dice;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Italian level bomber.
 */
public class PoorNavalBomber extends AircraftImpl {
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
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public double getNavalHitProbability(final SquadronStrength strength) {
        return getDice().probability(getNaval().getModifier() + BASE_MODIFIER, getNaval().getFactor(strength)) * POOR_NAVAL_MODIFIER;
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
    public double getNavalHitIndividualProbability(final Target target, final int modifier) {
        double factor = FACTOR_MAP.getOrDefault(target.getClass(), POOR_NAVAL_MODIFIER);
        return getDice().individualProbability(getNaval().getModifier() + BASE_MODIFIER + modifier) * factor;
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
        return Map.of(SquadronConfig.NONE, radius, SquadronConfig.LEAN_ENGINE, leanMixtureRadius);
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
        return Map.of(SquadronConfig.NONE, distance, SquadronConfig.LEAN_ENGINE, leanMixtureDistance);
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
        return Map.of(SquadronConfig.NONE, endurance, SquadronConfig.LEAN_ENGINE, leanMixtureEndurance);
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

        return Map.of(SquadronConfig.NONE, attack, SquadronConfig.LEAN_ENGINE, leanMixtureAttack);
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a land attack.
     */
    @Override
    public Map<SquadronConfig, Double> getLandHitProbability(final SquadronStrength strength) {
        double prob = getDice().probability(getLand().get(SquadronConfig.NONE).getModifier() + BASE_MODIFIER,
                getLand().get(SquadronConfig.NONE).getFactor(strength));

        double probLeanMixture = getDice().probability(getLand().get(SquadronConfig.LEAN_ENGINE).getModifier() + BASE_MODIFIER,
                getLand().get(SquadronConfig.LEAN_ENGINE).getFactor(strength));

        return Map.of(SquadronConfig.NONE, prob, SquadronConfig.LEAN_ENGINE, probLeanMixture);
    }
}
