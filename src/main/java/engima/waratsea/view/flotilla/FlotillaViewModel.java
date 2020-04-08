package engima.waratsea.view.flotilla;

import engima.waratsea.model.flotilla.Flotilla;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

public class FlotillaViewModel {

    @Getter private StringProperty name = new SimpleStringProperty();
    @Getter private StringProperty state = new SimpleStringProperty();
    @Getter private StringProperty location = new SimpleStringProperty();

    /**
     * Set the model.
     *
     * @param flotilla The selected flotilla.
     */
    public void setModel(final Flotilla flotilla) {
        String prefix = flotilla.atFriendlyBase() ? "At port " : "At sea zone ";

        name.set(flotilla.getName());
        state.set(prefix + flotilla.getMappedLocation());
        location.set(flotilla.getState().toString());
    }
}
