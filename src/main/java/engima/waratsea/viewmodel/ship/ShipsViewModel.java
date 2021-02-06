package engima.waratsea.viewmodel.ship;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.view.ship.ShipViewType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ShipsViewModel {
    private final Provider<ShipViewModel> provider;

    private final ObjectProperty<TaskForce> taskForce = new SimpleObjectProperty<>();

    @Getter private final MapProperty<ShipViewType, ListProperty<ShipViewModel>> shipTypeMap = new SimpleMapProperty<>(FXCollections.emptyObservableMap());
    @Getter private final Map<ShipViewType, BooleanProperty> shipNotPresent = new HashMap<>();
    @Getter private final Map<String, IntegerProperty> shipCounts = new LinkedHashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param provider Provides ship view models.
     */
    @Inject
    public ShipsViewModel(final Provider<ShipViewModel> provider) {
        this.provider = provider;

        bindShipTypeMap();
        bindShipCounts();
        bindShipPresent();
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
     * bind the ship type map.
     */
    private void bindShipTypeMap() {
        Callable<MapProperty<ShipViewType, ListProperty<ShipViewModel>>> bindingFunction = () -> {
            Map<ShipViewType, List<ShipViewModel>> map = Optional
                    .ofNullable(taskForce.getValue())
                    .map(this::getShipMap)
                    .orElse(Collections.emptyMap());

            // Convert to a Javafx map.
            MapProperty<ShipViewType, ListProperty<ShipViewModel>> oMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
            map.forEach((k, v) -> oMap.put(k, new SimpleListProperty<>(FXCollections.observableArrayList(v))));
            return oMap;
        };

        shipTypeMap.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
    }

    /**
     * Bind the ship counts. These are the counts of each type of ship in the task force.
     */
    private void bindShipCounts() {
        ShipViewType.stream().sorted().forEach(type -> {
            IntegerProperty count = new SimpleIntegerProperty(0);
            shipCounts.put(type.toString(), count);

            Callable<Integer> bindingFunction = () ->
                    Optional.ofNullable(shipTypeMap.getValue())
                            .map(m -> m.get(type))
                            //.map(this::getShipCount)
                            .map(List::size)
                            .orElse(0);

            count.bind(Bindings.createIntegerBinding(bindingFunction, shipTypeMap));
        });
    }

    private void bindShipPresent() {
        ShipViewType.stream().sorted().forEach(type -> {
            BooleanProperty notPresent = new SimpleBooleanProperty(true);
            shipNotPresent.put(type, notPresent);

            Callable<Boolean> bindingFunction = () ->
                    Optional.ofNullable(shipTypeMap.getValue())
                            .map(m -> m.get(type))
                            .map(ListExpression::isEmpty)
                            .orElse(true);

            notPresent.bind(Bindings.createBooleanBinding(bindingFunction, shipTypeMap));
        });
    }

    /**
     * Get the ship map from the task force.
     *
     * @param force The task force.
     * @return A map of ship view type to list of ships.
     */
    private Map<ShipViewType, List<ShipViewModel>> getShipMap(final TaskForce force) {
        return force
                .getShipTypeMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> ShipViewType.get(entry.getKey()),
                        entry -> buildShipViewModel(entry.getValue()),
                        ListUtils::union,
                        LinkedHashMap::new));
    }

    private List<ShipViewModel> buildShipViewModel(final List<Ship> ships) {
        return ships
                .stream()
                .map(ship -> provider.get().setModel(ship))
                .collect(Collectors.toList());
    }

    private int getShipCount(final List<ShipViewModel> ships) {
        return Optional.ofNullable(ships).map(List::size).orElse(0);
    }
}
