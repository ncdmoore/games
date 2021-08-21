package engima.waratsea.model.game;

import java.util.stream.Stream;

/**
 * The phases of a game turn.
 *
 * To add a new phase be sure to add it in the correct position within the enum values.
 * The game executes each turn phase in the order of the enum definition.
 *
 * In fact, the major purpose of this enum is to guarantee the order of the phases.
 *
 *
 * A note about the position of WEATHER. It is last because it must be calculated before
 * the orders stage of the turn. A turn is composed of two stages:
 *
 *  1 - ORDER STAGE
 *  2 - EXECUTION STAGE
 *
 *  The execution stage is composed of the following phases defined in this enum. When
 *  a scenario is started it's initial weather is calculated based off of the scenario's
 *  initial weather value. So in reality the weather "phase" is still executed first
 *  just like in the board game. We want the turn's weather to be determined during the
 *  order's stage, so the human player is aware of the current weather conditions for
 *  the current turn. We would not want to be giving orders for a turn in which the
 *  weather suddenly changed.
 */
public enum Phase {
    HUMAN_PATROL,
    COMPUTER_PATROL,
    HUMAN_MISSION,
    COMPUTER_MISSION,
    WEATHER;                // The weather is the very last phase. Because it is calculated at the start of the game.
                            // So the phase calculation is for the next turn. Next turns weather.

    /**
     * Get a stream of the phase enums sorted by position.
     *
     * @return  Stream of the phase enums.
     */
    public static Stream<Phase> stream() {
        return Stream.of(values());  // Returns a stream of phases ordered by position in enum definition.
    }
}
