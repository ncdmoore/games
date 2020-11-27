package engima.waratsea.viewmodel.ship;

import engima.waratsea.model.ship.Component;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.Optional;

public class ComponentViewModel {
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty value = new SimpleStringProperty();
    @Getter private final DoubleProperty percent = new SimpleDoubleProperty();

    /**
     * The constructor.
     *
     * @param newComponent The backing ship component.
     */
    public ComponentViewModel(final Component newComponent) {
        component.setValue(newComponent);
        bind();
    }

    private void bind() {
        title.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(component.getValue())
                .map(Component::getName)
                .orElse(""), component));

        value.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(component.getValue())
                .map(c -> c.getHealth() + "/" + c.getMaxHealth() + " " + c.getUnits())
                .orElse(""), component));

        percent.bind(Bindings.createDoubleBinding(() -> Optional
                .ofNullable(component.getValue())
                .map(c -> c.getHealth() * 1.0 / c.getMaxHealth())
                .orElse(0.0), component));
    }
}
