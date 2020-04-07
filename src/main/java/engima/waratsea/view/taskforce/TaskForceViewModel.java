package engima.waratsea.view.taskforce;

import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.view.ship.ShipViewType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Pair;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskForceViewModel {
    @Getter private StringProperty name = new SimpleStringProperty();
    @Getter private StringProperty state = new SimpleStringProperty();
    @Getter private StringProperty mission = new SimpleStringProperty();
    @Getter private StringProperty location = new SimpleStringProperty();
    @Getter private StringProperty reason = new SimpleStringProperty();

    @Getter private ObjectProperty<Paint> stateColor = new SimpleObjectProperty<>(this, "stateColor", Color.BLACK);

    @Getter private ObjectProperty<ObservableList<Pair<String, String>>> shipTypeSummary = new SimpleObjectProperty<>(this, "shipTypeSummary", FXCollections.observableArrayList());

    /**
     * Set the task force model.
     *
     * @param taskForce The selected task force.
     */
    public void setModel(final TaskForce taskForce) {
        name.set(taskForce.getName());
        state.set(taskForce.getState().toString());
        mission.set(taskForce.getMission().getType().toString());

        String locationPrefix = taskForce.atFriendlyBase() ? "At port " : "At sea zone ";
        location.set(locationPrefix + taskForce.getMappedLocation());

        List<String> reasons = taskForce.getActivatedByText();
        reason.set(String.join("\n", reasons));

        Paint color = taskForce.getState() == TaskForceState.RESERVE ? Color.RED : Color.BLACK;
        stateColor.set(color);

        setShipTypeSummary(taskForce);
    }

    /**
     * Set The ship type summary.
     *
     * @param taskforce The task force for which the ship type summary is determined.
     */
    private void setShipTypeSummary(final TaskForce taskforce) {
        List<Pair<String, String>> shipRows = getShipViewTypeMap(taskforce)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> new Pair<>(entry.getKey().toString(), entry.getValue().size() + ""))
                .collect(Collectors.toList());

        ObservableList<Pair<String, String>> shipData = FXCollections.observableArrayList(shipRows);

        shipTypeSummary.set(shipData);
    }

    /**
     * Get the ship view type map for the given task force.
     *
     * @param taskForce A task force.
     * @return A map of ship view type to list of ships of that view type in the task force.
     */
    private Map<ShipViewType, List<Ship>> getShipViewTypeMap(final TaskForce taskForce) {
        return  taskForce
                .getShipTypeMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> ShipViewType.get(entry.getKey()),
                        Map.Entry::getValue,
                        ListUtils::union,
                        LinkedHashMap::new));
    }
}
