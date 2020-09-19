package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import engima.waratsea.model.game.data.GameData;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.Optional;

/**
 * Represents the selected saved game.
 * The view binds its GUI elements to the values in this class.
 */
public class SavedGameViewModel {

    @Getter private final ObjectProperty<GameData> game = new SimpleObjectProperty<>();

    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty imageName = new SimpleStringProperty();
    @Getter private final StringProperty turn = new SimpleStringProperty();
    @Getter private final StringProperty date = new SimpleStringProperty();
    @Getter private final StringProperty description = new SimpleStringProperty();
    @Getter private final StringProperty side = new SimpleStringProperty();

    @Inject
    public SavedGameViewModel() {
        name.bind(Bindings.createStringBinding(() -> Optional.ofNullable(game.getValue()).map(g -> g.getScenario().getName()).orElse(""), game));
        turn.bind(Bindings.createStringBinding(() -> Optional.ofNullable(game.getValue()).map(g -> g.getScenario().getMaxTurns() + "").orElse(""), game));
        date.bind(Bindings.createStringBinding(() -> Optional.ofNullable(game.getValue()).map(g -> g.getScenario().getDateString()).orElse(""), game));
        imageName.bind(Bindings.createStringBinding(() -> Optional.ofNullable(game.getValue()).map(g -> g.getScenario().getImage()).orElse(""), game));
        description.bind(Bindings.createStringBinding(() -> Optional.ofNullable(game.getValue()).map(g -> g.getScenario().getDescription()).orElse(""), game));
        side.bind(Bindings.createStringBinding(() -> Optional.ofNullable(game.getValue()).map(g -> g.getHumanSide().toString()).orElse(""), game));
    }
}
