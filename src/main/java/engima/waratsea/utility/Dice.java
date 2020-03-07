package engima.waratsea.utility;

import com.google.inject.Singleton;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.Random;

/**
 * Represents a dice in the game.
 */
@Singleton
public class Dice {
    private static final int PERCENTAGE = 100;

    private static final int DICE_SIX_SIDED = 6;

    private static final Random DICE = new Random();

    /**
     * Roll a six sided dice.
     *
     * @return  A value between 1 and 6.
     */
    public int roll6() {
        return DICE.nextInt(DICE_SIX_SIDED) + 1;
    }

    /**
     * Roll the given number of 6 sided dice and return the sum of all the dice rolls.
     *
     * @param numberDiceToRoll  number of 6 sided dice to roll.
     * @return                  The sum of all the dice rolls.
     */
    public int sumDiceRoll(final int numberDiceToRoll) {
        return DICE
                .ints(numberDiceToRoll, 1, DICE_SIX_SIDED + 1)
                .sum();
    }

    /**
     * Roll the a dice of a given number of sides.
     *
     * @param sides The number of sides of the dice.
     * @return A value between 1 and sides.
     */
    public int roll(final int sides) {
        return DICE.nextInt(sides) + 1;
    }

    /**
     * Get the probability of an individual successful die roll given the number of
     * successful outcomes. For example, if both a 5 and 6 are a successful outcome
     * then numHit is set to 2 and this method returns the probability that either
     * a 5 or 6 will be rolled.
     *
     * @param numHit The number of hits or successful outcomes.
     * @return The probability of achieving a successful outcome.
     */
    public double individualProbability(final int numHit) {
        return ((double) numHit) /  ((double) DICE_SIX_SIDED);
    }

    /**
     * The probability a number of six-sided dice will roll a given number.
     *
     * @param numHit The numbers on a six-sided dice that count as a hit.
     * @param numberDiceToRoll The number of six-sided dice that are rolled.
     * @return The probability that the given number of "numbers" will hit.
     */
    public double probability(final int numHit, final int numberDiceToRoll) {

        double num = Math.pow((DICE_SIX_SIDED - numHit), numberDiceToRoll);
        double den = Math.pow(DICE_SIX_SIDED, numberDiceToRoll);

        double result =  1.0 - (num / den);

        return (result > 0) ? result : 0;   // any negative results are set to 0.
    }

    /**
     * The probability a number of six-sided dice will roll a given number.
     *
     * @param numHit The numbers on a six-sided dice that count as a hit.
     * @param numberDiceToRoll The number of six-sided dice that are rolled.
     * @return The probability that the given number of "numbers" will hit.
     */
    public int probabilityPercentage(final int numHit, final int numberDiceToRoll) {

        double num = Math.pow((DICE_SIX_SIDED - numHit), numberDiceToRoll);
        double den = Math.pow(DICE_SIX_SIDED, numberDiceToRoll);

        int result = (int) ((1.0 - (num / den)) * PERCENTAGE);

        return (result > 0) ? result : 0;   // any negative results are set to 0.
    }

    /**
     * The probability of getting the given number of hits rolling the given number of dice with the individual
     * probability.
     *
     * @param numHit The number of hits. The successful trials.
     * @param numberDiceToRoll The number of dice to roll. The trials.
     * @param individualProbability The individual probability.
     * @return The probability that the given number of hits will be achieved given the number of dice to
     * roll and the individual probability that a single die roll will be successful.
     */
    public double probabilityHits(final int numHit, final int numberDiceToRoll, final double individualProbability) {
        BinomialDistribution binomialDistribution = new BinomialDistribution(numberDiceToRoll, individualProbability);

        // The cumulative probability is the probability of all unsuccessful trials. P(X <= numHit -1)
        // Subtract this value from 1.0 to get the probability of success.
        return  1.0 - binomialDistribution.cumulativeProbability(numHit - 1);
    }

    /**
     * The probability of getting the given number of hits rolling the given number of dice with the individual
     * probability.
     *
     * @param numHit The number of hits. The successful trials.
     * @param numberDiceToRoll The number of dice to roll. The trials.
     * @param individualProbability The probability that a given die roll will be successful.
     * @return The probability that the given number of hits will be achieved given the number of dice to
     * roll and the individual probability that a single die roll will be successful.
     */
    public int probabilityHitsPercentage(final int numHit, final int numberDiceToRoll, final double individualProbability) {
        BinomialDistribution binomialDistribution = new BinomialDistribution(numberDiceToRoll, individualProbability);

        // The cumulative probability is the probability of all unsuccessful trials. P(X <= numHit -1)
        // Subtract this value from 1.0 to get the probability of success.
        double probability = 1.0 - binomialDistribution.cumulativeProbability(numHit - 1);

        return (int) (probability * PERCENTAGE);
    }
}
