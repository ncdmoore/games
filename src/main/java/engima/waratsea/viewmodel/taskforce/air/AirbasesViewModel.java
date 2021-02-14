package engima.waratsea.viewmodel.taskforce.air;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AirbasesViewModel {
    private final Provider<AirbaseViewModel> provider;

    private final ObjectProperty<TaskForce> taskForce = new SimpleObjectProperty<>();

    @Getter private final ListProperty<AirbaseViewModel> airbases = new SimpleListProperty<>();

    /**
     * Constructor called by guice.
     *
     * @param provider Provides airbase view models.
     */
    @Inject
    public AirbasesViewModel(final Provider<AirbaseViewModel> provider) {
        this.provider = provider;

        bindAirbases();
    }

    /**
     * Set the backing task force model.
     *
     * @param newTaskForce The new task force model.
     */
    public void setModel(final TaskForce newTaskForce) {
        taskForce.setValue(newTaskForce);
    }

    /**
     * Save the task force's airbases data to the model.
     */
    public void save() {
        airbases.forEach(AirbaseViewModel::save);
    }

    private void bindAirbases() {
        Callable<ObservableList<AirbaseViewModel>> bindingFunction = () -> Optional
                .ofNullable(taskForce.getValue())
                .map(this::getViewModels)
                .map(FXCollections::observableArrayList)
                .orElse(FXCollections.emptyObservableList());

        airbases.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
    }

    private List<AirbaseViewModel> getViewModels(final TaskForce force) {
        return force
                .getAirbases()
                .stream()
                .filter(Airbase::areSquadronsPresent)
                .map(airbase -> provider.get().setModel(airbase))
                .collect(Collectors.toList());
    }
}
