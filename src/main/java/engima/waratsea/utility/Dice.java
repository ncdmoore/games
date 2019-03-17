package engima.waratsea.utility;

import com.google.inject.Singleton;

import java.util.Random;

/**
 * Represents a dice in the game.
 */
@Singleton
public class Dice {

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

}
