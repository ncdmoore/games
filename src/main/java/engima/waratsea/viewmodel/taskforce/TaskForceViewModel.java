package engima.waratsea.viewmodel.taskforce;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.model.taskForce.mission.rules.SeaMissionRules;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.ship.ShipViewType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This class represents the task force view model. It contains properties based on the currently selected
 * task force. The presenter populates this class with the selected task force. The view binds its GUI elements
 * to the values in this class.
 */
public class TaskForceViewModel {
    private final SeaMissionRules seaMissionRules;

    @Getter private final ObjectProperty<ObservableList<SeaMissionType>> missionTypes = new SimpleObjectProperty<>();   // The task force's available missions.

    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty nameAndTitle = new SimpleStringProperty();

    @Getter private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    @Getter private final StringProperty state = new SimpleStringProperty();
    @Getter private final ObjectProperty<Paint> stateColor = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<SeaMissionType> mission = new SimpleObjectProperty<>();
    @Getter private final StringProperty location = new SimpleStringProperty();
    @Getter private final StringProperty reason = new SimpleStringProperty();  // The reasons the task force becomes active.

    @Getter private final IntegerProperty numShipTypes = new SimpleIntegerProperty();
    @Getter private final MapProperty<ShipViewType, List<Ship>> shipTypeMap = new SimpleMapProperty<>(FXCollections.emptyObservableMap());
    @Getter private final ListProperty<Pair<String, String>> shipTypeSummary = new SimpleListProperty<>(FXCollections.observableArrayList());
    @Getter private final Map<String, IntegerProperty> shipCounts = new LinkedHashMap<>();

    @Getter private final IntegerProperty numSquadronTypes = new SimpleIntegerProperty();
    @Getter private final MapProperty<AircraftType, BigDecimal> squadronTypeMap = new SimpleMapProperty<>(FXCollections.emptyObservableMap());
    @Getter private final ListProperty<Pair<String, String>> squadronTypeSummary = new SimpleListProperty<>(FXCollections.observableArrayList());
    @Getter private final Map<String, IntegerProperty> squadronCounts = new LinkedHashMap<>();

    private final ObjectProperty<TaskForce> taskForce = new SimpleObjectProperty<>();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param seaMissionRules The sea mission rules.
     */
    @Inject
    public TaskForceViewModel(final ImageResourceProvider imageResourceProvider,
                              final ViewProps props,
                              final SeaMissionRules seaMissionRules) {
        this.seaMissionRules = seaMissionRules;

        bindTitles();
        bindDetails();
        bindShipTypeMap();
        bindSquadronTypeMap();
        bindShipTypeSummary();
        bindShipCounts();
        bindSquadronTypeSummary();
        bindSquadronCounts();
        bindImages(imageResourceProvider, props);
    }

    /**
     * Set the task force model.
     *
     * @param force The selected task force.
     * @return This task force view model.
     */
    public TaskForceViewModel setModel(final TaskForce force) {
        taskForce.setValue(force);
        return this;
    }

    /**
     * bind the task force titles.
     */
    private void bindTitles() {
        name.bind(Bindings.createStringBinding(() -> Optional.ofNullable(taskForce.getValue()).map(TaskForce::getName).orElse(""), taskForce));
        title.bind(Bindings.createStringBinding(() -> Optional.ofNullable(taskForce.getValue()).map(TaskForce::getTitle).orElse(""), taskForce));
        nameAndTitle.bind(name.concat(new SimpleStringProperty(" ")).concat(title));
    }

    /**
     * bind the task force details.
     */
    private void bindDetails() {
        state.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> t.getState().toString())
                .orElse(""), taskForce));

        mission.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> t.getMission().getType())
                .orElse(null), taskForce));

        missionTypes.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> FXCollections.observableArrayList(seaMissionRules.getMissions(t)))
                .orElse(FXCollections.emptyObservableList())));

        location.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> getLocationPrefix(t) + t.getMappedLocation())
                .orElse(""), taskForce));

        reason.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> String.join("\n", t.getActivatedByText()))
                .orElse(""), taskForce));

        stateColor.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .filter(t -> t.getState() == TaskForceState.RESERVE)
                .map(t -> Color.RED)
                .orElse(Color.BLACK), taskForce));
    }

    /**
     * bind the ship type map.
     */
    private void bindShipTypeMap() {
        Callable<MapProperty<ShipViewType, List<Ship>>> bindingFunction = () -> {
            Map<ShipViewType, List<Ship>> map = Optional
                    .ofNullable(taskForce.getValue())
                    .map(this::getShipMap)
                    .orElse(Collections.emptyMap());

            // Convert to a Javafx map.
            MapProperty<ShipViewType, List<Ship>> oMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
            map.forEach(oMap::put);
            return oMap;
        };

        shipTypeMap.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
    }

    private void bindSquadronTypeMap() {
        Callable<MapProperty<AircraftType, BigDecimal>> bindingFunction = () -> {
            Map<AircraftType, BigDecimal> map = Optional
                    .ofNullable(taskForce.getValue())
                    .map(this::getAircraftMap)
                    .orElse(Collections.emptyMap());

            // Convert to a Javafx map.
            MapProperty<AircraftType, BigDecimal> oMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
            map.forEach(oMap::put);
            return oMap;
        };

        squadronTypeMap.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
    }

    /**
     * Bind The ship type summary.
     */
    private void bindShipTypeSummary() {
        Callable<ObservableList<Pair<String, String>>> bindingFunction = () -> {
            List<Pair<String, String>> list = Optional
                    .ofNullable(taskForce.getValue())
                    .map(this::getShipViewTypeMap)
                    .orElse(Collections.emptyList());

            return FXCollections.observableArrayList(list);
        };

        shipTypeSummary.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
        numShipTypes.bind(shipTypeSummary.sizeProperty());
    }

    /**
     * Bind the ship counts. These are the counts of each type of ship in the task force.
     */
    private void bindShipCounts() {
        ShipViewType.stream().sorted().forEach(type -> {
            IntegerProperty shipCount = new SimpleIntegerProperty(0);
            shipCounts.put(type.toString(), shipCount);

            Callable<Integer> bindingFunction = () ->
                    Optional.ofNullable(shipTypeMap.getValue())
                            .map(m -> getShipCount(m.get(type)))
                            .orElse(0);

            shipCount.bind(Bindings.createIntegerBinding(bindingFunction, shipTypeMap));
        });
    }

    /**
     * Bind the squadron type summary.
     */
    private void bindSquadronTypeSummary() {
        Callable<ObservableList<Pair<String, String>>> bindingFunction = () -> {
            List<Pair<String, String>> list = Optional
                    .ofNullable(taskForce.getValue())
                    .map(this::getAircraftTypeMap)
                    .orElse(Collections.emptyList());

            return FXCollections.observableArrayList(list);
        };
        squadronTypeSummary.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
        numSquadronTypes.bind(squadronTypeSummary.sizeProperty());
    }

    /**
     * Bind the ship counts. These are the counts of each type of ship in the task force.
     */
    private void bindSquadronCounts() {
        AircraftType.stream().sorted().forEach(type -> {
            IntegerProperty squadronCount = new SimpleIntegerProperty(0);
            squadronCounts.put(type.toString(), squadronCount);

            Callable<Integer> bindingFunction = () ->
                    Optional.ofNullable(squadronTypeMap.getValue())
                            .map(m -> getSquadronCount(m.get(type)))
                            .orElse(0);

            squadronCount.bind(Bindings.createIntegerBinding(bindingFunction, shipTypeMap));
        });
    }

    /**
     * Bind the task force image.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     */
    private void bindImages(final ImageResourceProvider imageResourceProvider, final ViewProps props) {
        image.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> imageResourceProvider.getImage(getImageName(t, props)))
                .orElse(null), taskForce));
    }

    /**
     * Get the task force's aircraft for a given task force.
     *
     * @param force The task force.
     * @return A map of aircraft type to number of aircraft of that type.
     */
    private List<Pair<String, String>> getAircraftTypeMap(final TaskForce force) {
        return getAircraftMap(force)
                .entrySet()
                .stream()
                .filter(entry -> !(entry.getValue().compareTo(BigDecimal.ZERO) == 0))
                .map(entry -> new Pair<>(entry.getKey().toString(), stripFraction(entry.getValue().setScale(2, RoundingMode.HALF_UP) + "")))
                .collect(Collectors.toList());
    }

    /**
     * Get the ship view type map for the given task force.
     *
     * @param force A task force.
     * @return A map of ship view type to list of ship names.
     */
    private List<Pair<String, String>> getShipViewTypeMap(final TaskForce force) {
        return getShipMap(force)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> new Pair<>(entry.getKey().toString(), entry.getValue().size() + ""))
                .collect(Collectors.toList());
    }

    /**
     * Get the ship map from the task force.
     *
     * @param force The task force.
     * @return A map of ship view type to list of ships.
     */
    private Map<ShipViewType, List<Ship>> getShipMap(final TaskForce force) {
        return force
                .getShipTypeMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> ShipViewType.get(entry.getKey()),
                        Map.Entry::getValue,
                        ListUtils::union,
                        LinkedHashMap::new));
    }

    /**
     * Get a map of aircraft types within the given task force to the number of steps of the types.
     *
     * @param force The selected task force.
     * @return A map of aircraft type to number of steps of the type.
     */
    private Map<AircraftType, BigDecimal> getAircraftMap(final TaskForce force) {
        return force.getShips()
                .stream()
                .map(Ship::getSquadronSummary)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        BigDecimal::add,
                        LinkedHashMap::new));
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

    private String getImageName(final TaskForce force, final ViewProps props) {
        return props.getString(force.getSide().toLower() + ".taskforce.details.image");
    }

    private int getShipCount(final List<Ship> ships) {
        return Optional.ofNullable(ships).map(List::size).orElse(0);
    }

    private int getSquadronCount(final BigDecimal count) {
        return Optional.ofNullable(count).map(BigDecimal::intValue).orElse(0);
    }

    private String getLocationPrefix(final TaskForce force) {
        return force.atFriendlyBase() ? "At port " : "At sea zone ";
    }
}
