package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import engima.waratsea.model.scenario.Scenario;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.Optional;

/**
 * Represents the selected scenario.
 * The view binds its GUI elements to the values in this class.
 */
public class ScenarioViewModel {

    @Getter private final ObjectProperty<Scenario> scenario = new SimpleObjectProperty<>();

    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty imageName = new SimpleStringProperty();
    @Getter private final StringProperty turn = new SimpleStringProperty();
    @Getter private final StringProperty date = new SimpleStringProperty();
    @Getter private final StringProperty description = new SimpleStringProperty();

    @Inject
    public ScenarioViewModel() {
        name.bind(Bindings.createStringBinding(() -> Optional.ofNullable(scenario.getValue()).map(Scenario::getName).orElse(""), scenario));
        turn.bind(Bindings.createStringBinding(() -> Optional.ofNullable(scenario.getValue()).map(s -> s.getMaxTurns() + "").orElse(""), scenario));
        date.bind(Bindings.createStringBinding(() -> Optional.ofNullable(scenario.getValue()).map(Scenario::getDateString).orElse(""), scenario));
        imageName.bind(Bindings.createStringBinding(() -> Optional.ofNullable(scenario.getValue()).map(Scenario::getImage).orElse(""), scenario));
        description.bind(Bindings.createStringBinding(() -> Optional.ofNullable(scenario.getValue()).map(Scenario::getDescription).orElse(""), scenario));
    }
}
