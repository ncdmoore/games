package engima.waratsea.model.game.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.weather.WeatherType;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * The game data that is persisted. This data is read when a saved game is loaded. This data is written when a
 * game is saved. This class contains all the persistent data needed by the game class.
 */
public class GameData implements Comparable<GameData> {
    @Getter
    @Setter
    private Side humanSide;

    @Getter
    @Setter
    private Scenario scenario;

    @Getter
    @Setter
    private String savedGameName;

    @Getter
    @Setter
    private TurnData turn;

    @Getter
    @Setter
    private WeatherType weather;

    /**
     * Called to sort saved games.
     *
     * @param otherGameData The 'other' saved game in which this saved game is compared.
     * @return an integer indicating whether this saved game is alphabetically first, last or equal with the other
     * saved game.
     */
    @Override
    public int compareTo(@Nonnull final GameData otherGameData) {
        return scenario.getId().compareTo(otherGameData.getScenario().getId());
    }

    /**
     * The String representation of the GameData.
     *
     * @return The String representation of the GameData.
     */
    @Override
    public String toString() {
        return scenario.toString() + savedGameName;
    }
}
