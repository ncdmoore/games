package engima.waratsea.model.game.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import lombok.Getter;
import lombok.Setter;

/**
 * The game data that is persisted. This data is read when a saved game is loaded. This data is written when a
 * game is saved. This class contains all the persistent data needed by the game class.
 */
public class GameData  {
    @Getter
    @Setter
    private Side humanSide;

    @Getter
    @Setter
    private Scenario scenario;
}
