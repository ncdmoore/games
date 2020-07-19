package engima.waratsea.viewmodel;

import engima.waratsea.model.scenario.Scenario;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

/**
 * Represents the selected scenario. The presenter populates this class with the selected scenario.
 * The view binds its GUI elements to the values in this class.
 */
public class ScenarioViewModel {
    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty imageName = new SimpleStringProperty();
    @Getter private final StringProperty turn = new SimpleStringProperty();
    @Getter private final StringProperty date = new SimpleStringProperty();
    @Getter private final StringProperty description = new SimpleStringProperty();

    /**
     * Set the model.
     *
     * @param scenario The selected scenario.
     */
    public void setModel(final Scenario scenario) {
        name.set(scenario.getName());
        imageName.set(scenario.getImage());
        turn.set(scenario.getMaxTurns() + "");
        date.set(scenario.getDateString());
        description.set(scenario.getDescription());
    }
}
