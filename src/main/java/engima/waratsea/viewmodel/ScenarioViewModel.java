package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.scenario.Scenario;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Represents the selected scenario.
 * The view binds its GUI elements to the values in this class.
 */

@Singleton
public class ScenarioViewModel {

    @Getter private final ListProperty<Scenario> scenarios = new SimpleListProperty<>(FXCollections.emptyObservableList()); // The game's scenarios.

    @Getter private final ObjectProperty<Scenario> scenario = new SimpleObjectProperty<>();  // Bound to the currently selected scenario in the view's scenario list.

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

    /**
     * Set the scenario list. The list is fixed per game; i.e., Bomb Alley has always has the same list of scenarios;
     * Arctic Convoy has the same list of scenario's, etc. Thus, this value never changes once read in from the json
     * data files.
     *
     * @param scenarioList The list of the game's scenarios.
     */
    public void set(final List<Scenario> scenarioList) {
        scenarios.setValue(FXCollections.observableList(scenarioList));
    }
}
