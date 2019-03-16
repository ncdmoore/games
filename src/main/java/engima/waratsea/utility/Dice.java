package engima.waratsea.utility;

import com.google.inject.Singleton;

import java.util.Random;

/**
 * Represents a dice in the game.
 */
@Singleton
public class Dice {

    private static final int DICE_SIX_SIDED = 6;

    /**
     * Roll a six sided dice.
     *
     * @return  A value between 1 and 6.
     */
    public int roll6() {
        return roll(DICE_SIX_SIDED);
    }

    /**
     * Roll the given number of 6 sided dice and return the sum of all the dice rolls.
     *
     * @param numberDiceToRoll  number of 6 sided dice to roll.
     * @return                  The sum of all the dice rolls.
     */
    public int sumDiceRoll(final int numberDiceToRoll) {
        return new Random()
                .ints(numberDiceToRoll, 1, DICE_SIX_SIDED + 1)
                .sum();
    }

    /**
     * Roll a dice that has the given number of sides.
     *
     * @param max   The potential maximum value of the random number that is generated.
     * @return      A random number between min and max
     */
    private int roll(final int max) {
        return new Random()
                .nextInt(max) + 1;
    }
}
