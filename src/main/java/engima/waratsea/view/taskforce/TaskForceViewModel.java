package engima.waratsea.view.taskforce;

import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.view.ship.ShipViewType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents the task force view model. It contains properties based on the currently selected
 * task force. The presenter populates this class with the selected task force. The view binds its GUI elements
 * to the values in this class.
 */
public class TaskForceViewModel {
    @Getter private StringProperty name = new SimpleStringProperty();
    @Getter private StringProperty state = new SimpleStringProperty();
    @Getter private StringProperty mission = new SimpleStringProperty();
    @Getter private StringProperty location = new SimpleStringProperty();
    @Getter private StringProperty reason = new SimpleStringProperty();  // The reasons the task force becomes active.

    @Getter private ObjectProperty<Paint> stateColor = new SimpleObjectProperty<>(this, "stateColor", Color.BLACK);

    @Getter private IntegerProperty numShipTypes = new SimpleIntegerProperty();
    @Getter private ObjectProperty<Map<ShipViewType, List<Ship>>> shipTypeMap = new SimpleObjectProperty<>(this, "shipTypeMap", Collections.emptyMap());
    @Getter private ObjectProperty<ObservableList<Pair<String, String>>> shipTypeSummary = new SimpleObjectProperty<>(this, "shipTypeSummary", FXCollections.observableArrayList());

    @Getter private IntegerProperty numSquadronTypes = new SimpleIntegerProperty();
    @Getter private ObjectProperty<Map<AircraftType, BigDecimal>> squadronTypeMap = new SimpleObjectProperty<>(this, "squadronTypeMap", Collections.emptyMap());
    @Getter private ObjectProperty<ObservableList<Pair<String, String>>> squadronTypeSummary = new SimpleObjectProperty<>(this, "squadronTypeSummary", FXCollections.observableArrayList());

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
        setSquadronTypeSummary(taskForce);
    }

    /**
     * Set The ship type summary.
     *
     * @param taskForce The task force for which the ship type summary is determined.
     */
    private void setShipTypeSummary(final TaskForce taskForce) {
        List<Pair<String, String>> shipRows = getShipViewTypeMap(taskForce)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> new Pair<>(entry.getKey().toString(), entry.getValue().size() + ""))
                .collect(Collectors.toList());

        ObservableList<Pair<String, String>> shipData = FXCollections.observableArrayList(shipRows);

        shipTypeSummary.set(shipData);

        numShipTypes.set(shipRows.size());
    }

    /**
     * Set the squadron type summary.
     *
     * @param taskForce The task force for which the squadron type summary is determined.
     */
    private void setSquadronTypeSummary(final TaskForce taskForce) {
        List<Pair<String, String>> squadronRows = getAircraftTypeStepMap(taskForce)
                .entrySet()
                .stream()
                .filter(entry -> !(entry.getValue().compareTo(BigDecimal.ZERO) == 0))
                .map(entry -> new Pair<>(entry.getKey().toString(), stripFraction(entry.getValue().setScale(2, RoundingMode.HALF_UP) + "")))
                .collect(Collectors.toList());

        ObservableList<Pair<String, String>> squadronData = FXCollections.observableArrayList(squadronRows);

        squadronTypeSummary.set(squadronData);

        numSquadronTypes.set(squadronRows.size());
    }

    /**
     * Get the ship view type map for the given task force.
     *
     * @param taskForce A task force.
     * @return A map of ship view type to list of ships of that view type in the task force.
     */
    private Map<ShipViewType, List<Ship>> getShipViewTypeMap(final TaskForce taskForce) {
        Map<ShipViewType, List<Ship>> shipMap =  taskForce
                .getShipTypeMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> ShipViewType.get(entry.getKey()),
                        Map.Entry::getValue,
                        ListUtils::union,
                        LinkedHashMap::new));

        shipTypeMap.set(shipMap);
        return shipMap;
    }

    /**
     * Get a map of aircraft types within the given task force to the number of steps of the types.
     *
     * @param taskForce The selected task force.
     * @return A map of aircraft type to number of steps of the type.
     */
    private Map<AircraftType, BigDecimal> getAircraftTypeStepMap(final TaskForce taskForce) {
        Map<AircraftType, BigDecimal> squadronMap = taskForce.getShips()
                .stream()
                .map(Ship::getSquadronSummary)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, BigDecimal::add, LinkedHashMap::new));

        squadronTypeMap.set(squadronMap);
        return squadronMap;
    }

    /**
     * Strip off the trailing zero's.
     *
     * @param number A number string.
     * @return If the given number has a zero fraction, then the number is returned with
     * no trailing zeros.
     */
    private String stripFraction(final String number) {
        String decimal = number.substring(number.indexOf('.') + 1);

        return (Integer.parseInt(decimal) == 0)
                ? number.substring(0, number.indexOf('.'))
                : number;
    }
}
