package engima.waratsea.view.game;

import engima.waratsea.model.game.data.GameData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

/**
 * Represents the selected game. The presenter populates this class with the selected game.
 * The view binds its GUI elements to the values in this class.
 */
public class GameViewModel {
    @Getter private StringProperty side = new SimpleStringProperty();

    /**
     * Set the model.
     *
     * @param game The selected game.
     */
    public void setModel(final GameData game) {
        side.set(game.getHumanSide().toString());
    }
}
