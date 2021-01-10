package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.utility.Dice;
import javafx.util.Pair;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Probability {
    private static final int BASE_MODIFIER = 1;
    private final Dice dice;

    @Setter private Set<SquadronConfig> configurations;

    /**
     * The constructor called by guice.
     *
     * @param dice A dice utility
     */
    @Inject
    public Probability(final Dice dice) {
        this.dice = dice;
    }

    /**
     * Get the hit probability for the individual squadron. This indicates how well a squadron does on
     * a particular task in the absence of any outside affects such as weather.
     *
     * @param attackFactor The squadron attack factor.
     * @param strength The squadron strength.
     * @return A map of squadron configurations to squadron success probability.
     */
    public Map<SquadronConfig, Double> getHitProbability(final Map<SquadronConfig, Attack> attackFactor,
                                                          final SquadronStrength strength) {
        return configurations
                .stream()
                .map(config -> getProbability(attackFactor, config, strength))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));   // SquadronConfig -> probability
    }

    /**
     * Get the individual hit probability for the individual squadron's individual die roll.
     *
     * @param attackFactor The squadron attack factor.
     * @param modifier The attack factor modifier. Weather, etc...
     * @return A map of squadron configurations to squadron success probability.
     */
    public Map<SquadronConfig, Double> getIndividualHitProbability(final Map<SquadronConfig, Attack> attackFactor,
                                                                   final int modifier) {
        return configurations
                .stream()
                .map(config -> getIndividualProbability(attackFactor, config, modifier))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
    /**
     * Get the probability.
     *
     * @param attackFactor The squadron attack factor.
     * @param config The squadron configuration.
     * @param strength The squadron strength.
     * @return The probability.
     */
    private Pair<SquadronConfig, Double> getProbability(final Map<SquadronConfig, Attack> attackFactor,
                                                        final SquadronConfig config,
                                                        final SquadronStrength strength) {
        int numHit = attackFactor.get(config).getModifier() + BASE_MODIFIER;
        int numToRoll = attackFactor.get(config).getFactor(strength);
        double prob = dice.probability(numHit, numToRoll) * attackFactor.get(config).getFinalModifier();
        return new Pair<>(config, prob);
    }

    /**
     * Get the individual probability.
     *
     * @param attackFactor The squadron attack factor.
     * @param config The squadron configuration.
     * @param modifier The attack modifier.
     * @return The individual probability.
     */
    private Pair<SquadronConfig, Double> getIndividualProbability(final Map<SquadronConfig, Attack> attackFactor,
                                                                  final SquadronConfig config,
                                                                  final int modifier) {
        int numHit = attackFactor.get(config).getModifier() + BASE_MODIFIER + modifier;
        double prob = dice.individualProbability(numHit) * attackFactor.get(config).getFinalModifier();
        return new Pair<>(config, prob);
    }
}
