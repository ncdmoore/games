package engima.waratsea.viewmodel.squadrons;

import com.google.inject.Inject;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This class represents a list of squadrons.
 * It also contains a currently selected squadron.
 */
public class SquadronsViewModel {
    @Getter private final ListProperty<Squadron> squadrons = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final ListProperty<SquadronConfig> configurations = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private final SquadronViewModel squadronViewModel;

    @Inject
    public SquadronsViewModel(final SquadronViewModel squadronViewModel) {
        this.squadronViewModel = squadronViewModel;
        bindConfiguration();
    }

    /**
     * Set the backing data for this view model.
     *
     * @param newSquadrons The squadrons.
     */
    public void set(final List<Squadron> newSquadrons) {
        squadrons.setValue(FXCollections.observableArrayList(newSquadrons));
    }

    private void bindConfiguration() {
        ObjectProperty<Squadron> selectedSquadron = squadronViewModel.getSquadron();

        Callable<ObservableList<SquadronConfig>> bindingFunction = () -> {
            List<SquadronConfig> configs = Optional
                    .ofNullable(selectedSquadron.getValue())
                    .map(squadron -> squadron
                            .getAircraft()
                            .getConfiguration()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            return FXCollections.observableArrayList(configs);
        };

        configurations.bind(Bindings.createObjectBinding(bindingFunction, selectedSquadron));
    }
}
