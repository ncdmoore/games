package engima.waratsea.model.game.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.weather.WeatherType;
import lombok.Data;

import javax.annotation.Nonnull;

/**
 * The game data that is persisted. This data is read when a saved game is loaded. This data is written when a
 * game is saved. This class contains all the persistent data needed by the game class.
 */
@Data
public class GameData implements Comparable<GameData> {
    private Side humanSide;
    private Scenario scenario;
    private String savedGameName;
    private TurnData turn;
    private WeatherType weather;
    private int airMissionId;

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
