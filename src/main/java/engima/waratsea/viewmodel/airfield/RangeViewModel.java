package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RangeViewModel {
    @Getter
    private final ListProperty<Aircraft> aircraftModels = new SimpleListProperty<>();                     // List of all aircraft models present at this airbase.
    @Getter private final ObjectProperty<Aircraft> selectedAircraft = new SimpleObjectProperty<>();       // The selected aircraft model.

    // List of squadron configs allowed for the selected aircraft model.
    @Getter private final ListProperty<SquadronConfig> squadronConfigs = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final ObjectProperty<SquadronConfig> selectedConfig = new SimpleObjectProperty<>();   // The selected squadron configuration for the selected aircraft model.

    @Getter private final ObjectProperty<Airbase> airbase = new SimpleObjectProperty<>();                 // Need this to support binding in constructor.

    private Nation nation;

    /**
     * The constructor called by guice.
     */
    @Inject
    public RangeViewModel() {
        bindAircraftModels();
    }

    /**
     * Set the range view model's backing model.
     *
     * @param newNation The nation.
     * @param viewModel The nation airbase view model. The parent view model of the range view model.
     */
    public void setModel(final Nation newNation, final NationAirbaseViewModel viewModel) {
        nation = newNation;
        airbase.bind(viewModel.getAirbase());

        selectAircraftModel();
    }

    /**
     * Select the first aircraft model.
     */
    private void selectAircraftModel() {
        if (!aircraftModels.getValue().isEmpty()) {
            selectedAircraft.setValue(aircraftModels.getValue().get(0));
        }
    }

    /**
     * Set the aircraft models present at this airbase.
     * This is a unique list of aircraft of all nations stationed at this airbase.
     */
    private void bindAircraftModels() {
        Callable<ObservableList<Aircraft>> modelBindingFunction = () -> Optional
                .ofNullable(airbase.getValue())
                .map(a -> FXCollections.observableArrayList(a.getAircraftModelsPresent(nation)))
                .orElse(FXCollections.emptyObservableList());

        aircraftModels.bind(Bindings.createObjectBinding(modelBindingFunction, airbase));

        Callable<ObservableList<SquadronConfig>> configBindingFunction = () -> {
            List<SquadronConfig> configs = Optional
                    .ofNullable(selectedAircraft.getValue())
                    .map(aircraft -> aircraft.getConfiguration()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            return FXCollections.observableArrayList(configs);
        };

        squadronConfigs.bind(Bindings.createObjectBinding(configBindingFunction, selectedAircraft));
    }
}
