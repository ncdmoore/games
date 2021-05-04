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

/**
 * Utility class used to determine the 'probability' that a squadron successfully attacks.
 */
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
     * @param attack The squadron attack.
     * @param strength The squadron strength.
     * @return A map of squadron configurations to squadron success probability.
     */
    public Map<SquadronConfig, Double> getHitProbability(final Map<SquadronConfig, Attack> attack,
                                                          final SquadronStrength strength) {
        return configurations
                .stream()
                .map(config -> getProbability(attack, config, strength))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));   // SquadronConfig -> probability
    }

    /**
     * Get the individual hit probability for a squadron's individual die roll.
     *
     * @param attack The squadron attack.
     * @param modifier The attack modifier. Weather, etc...
     * @return A map of squadron configurations to squadron success probability.
     */
    public Map<SquadronConfig, Double> getIndividualHitProbability(final Map<SquadronConfig, Attack> attack,
                                                                   final int modifier) {
        return configurations
                .stream()
                .map(config -> getIndividualProbability(attack, config, modifier))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
    /**
     * Get the probability that a squadron will successfully attack - achieve at least one hit.
     * The squadron's attack factor determines how many dice are rolled. This returns the probability
     * at least one of these die roll results in a hit.
     *
     * @param attack The squadron attack.
     * @param config The squadron configuration.
     * @param strength The squadron strength.
     * @return The probability.
     */
    private Pair<SquadronConfig, Double> getProbability(final Map<SquadronConfig, Attack> attack,
                                                        final SquadronConfig config,
                                                        final SquadronStrength strength) {
        int numHit = attack.get(config).getModifier() + BASE_MODIFIER;
        int numToRoll = attack.get(config).getFactor(strength);
        double prob = dice.probability(numHit, numToRoll) * attack.get(config).getFinalModifier();
        return new Pair<>(config, prob);
    }

    /**
     * Get the individual probability that a single die roll will achieve a hit.
     *
     * @param attack The squadron attack.
     * @param config The squadron configuration.
     * @param modifier The attack modifier.
     * @return The individual probability.
     */
    private Pair<SquadronConfig, Double> getIndividualProbability(final Map<SquadronConfig, Attack> attack,
                                                                  final SquadronConfig config,
                                                                  final int modifier) {
        int numHit = attack.get(config).getModifier() + BASE_MODIFIER + modifier;
        double prob = dice.individualProbability(numHit) * attack.get(config).getFinalModifier();
        return new Pair<>(config, prob);
    }
}
