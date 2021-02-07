package engima.waratsea.viewmodel.taskforce;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.model.taskForce.mission.rules.SeaMissionRules;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.ship.ShipViewType;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.ship.ShipViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
public class TaskForceViewModel implements Comparable<TaskForceViewModel> {
    private static final String NOT_SET = "Not Set";

    private final SeaMissionRules seaMissionRules;

    @Getter private final ListProperty<SeaMissionType> missionTypes = new SimpleListProperty<>();   // The task force's available missions.

    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty nameAndTitle = new SimpleStringProperty();

    @Getter private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    @Getter private final StringProperty state = new SimpleStringProperty();
    @Getter private final ObjectProperty<Paint> stateColor = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<SeaMissionType> mission = new SimpleObjectProperty<>();
    @Getter private final StringProperty location = new SimpleStringProperty();
    @Getter private final StringProperty reason = new SimpleStringProperty();  // The reasons the task force becomes active.

    @Getter private final ListProperty<String> possibleStartingLocations = new SimpleListProperty<>();

    @Getter private final MapProperty<AircraftType, Integer> squadronTypeMap = new SimpleMapProperty<>(FXCollections.emptyObservableMap());
    @Getter private final Map<String, IntegerProperty> squadronCounts = new LinkedHashMap<>();

    @Getter private final ObjectProperty<TaskForce> taskForce = new SimpleObjectProperty<>();

    private final ShipsViewModel shipsViewModel;
    private final AirbasesViewModel airbasesViewModel;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param seaMissionRules The sea mission rules.
     * @param shipsViewModel The ship view models in this task force.
     * @param airbasesViewModel The airbase view models in this task force. Aircraft carriers, etc...
     */
    @Inject
    public TaskForceViewModel(final ImageResourceProvider imageResourceProvider,
                              final ViewProps props,
                              final SeaMissionRules seaMissionRules,
                              final ShipsViewModel shipsViewModel,
                              final AirbasesViewModel airbasesViewModel) {
        this.seaMissionRules = seaMissionRules;
        this.shipsViewModel = shipsViewModel;
        this.airbasesViewModel = airbasesViewModel;

        bindTitles();
        bindDetails();
        bindSquadronTypeMap();
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
        shipsViewModel.setModel(force);
        airbasesViewModel.setModel(force);

        String taskForceLocation = Optional
                .ofNullable(force.getLocation())
                .orElse(NOT_SET);

        location.setValue(taskForceLocation);

        return this;
    }

    /**
     * Set the task force's location.
     *
     * @param newLocation The new location of the task force.
     */
    public void setLocation(final String newLocation) {
        location.setValue(newLocation);

        if (NOT_SET.equalsIgnoreCase(newLocation)) {
            taskForce.getValue().setReference(null);
        } else {
            taskForce.getValue().setReference(newLocation);
        }
    }

    /**
     * Get a map of ship type to list of ships of that type.
     *
     * @return A map of ship type to list of ships of that type.
     */
    public MapProperty<ShipViewType, ListProperty<ShipViewModel>> getShipTypeMap() {
        return shipsViewModel.getShipTypeMap();
    }

    /**
     * Get a map of ship type (String) to number of ships of that type.
     *
     * @return A map of ship type, the String representation, to number of ships of that type.
     */
    public Map<String, IntegerProperty> getShipCounts() {
        return shipsViewModel.getShipCounts();
    }

    /**
     * Get a map of ship type to an indication of whether that type is present in this task force.
     *
     * @return A map of ship type to an indication of whether that type is present in this task force.
     */
    public Map<ShipViewType, BooleanProperty> getShipPresent() {
        return shipsViewModel.getShipNotPresent();
    }

    /**
     * Get this task forces airbases.
     *
     * @return A list of this task forces airbases.
     */
    public ListProperty<AirbaseViewModel> getAirbases() {
        return airbasesViewModel.getAirbases();
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
                .orElse(FXCollections.emptyObservableList()), taskForce));

        reason.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(t -> String.join("\n", t.getActivatedByText()))
                .orElse(""), taskForce));

        stateColor.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .filter(t -> t.getState() == TaskForceState.RESERVE)
                .map(t -> Color.RED)
                .orElse(Color.BLACK), taskForce));

        possibleStartingLocations.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(taskForce.getValue())
                .map(TaskForce::getPossibleStartingLocations)
                .map(this::addNotSet)
                .map(FXCollections::observableArrayList)
                .orElse(FXCollections.emptyObservableList()), taskForce));
    }

    private void bindSquadronTypeMap() {
        Callable<MapProperty<AircraftType, Integer>> bindingFunction = () -> {
            Map<AircraftType, Integer> map = Optional
                    .ofNullable(taskForce.getValue())
                    .map(this::getAircraftMap)
                    .orElse(Collections.emptyMap());

            // Convert to a Javafx map.
            MapProperty<AircraftType, Integer> oMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
            map.forEach(oMap::put);
            return oMap;
        };

        squadronTypeMap.bind(Bindings.createObjectBinding(bindingFunction, taskForce));
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
                            .map(m -> m.get(type))
                            .orElse(0);

            squadronCount.bind(Bindings.createIntegerBinding(bindingFunction, squadronTypeMap));
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
     * Get a map of aircraft types within the given task force to the number of steps of the types.
     *
     * @param force The selected task force.
     * @return A map of aircraft type to number of steps of the type.
     */
    private Map<AircraftType, Integer> getAircraftMap(final TaskForce force) {
        return force.getAirbases()
                .stream()
                .map(Airbase::getSquadrons)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Squadron::getType))
                .collect(Collectors.toMap(
                        Squadron::getType,
                        s -> 1,
                        Integer::sum,
                        LinkedHashMap::new));
    }

    private String getImageName(final TaskForce force, final ViewProps props) {
        return props.getString(force.getSide().toLower() + ".taskforce.details.image");
    }

    /**
     * Add a "Not Set" to the starting locations. This is how a task force is temporarily removed from the game map.
     *
     * @param startingLocations All possible starting locations of a given task force.
     * @return The list of starting locations augmented with a "Not Set" choice.
     */
    private List<String> addNotSet(final List<String> startingLocations) {
        if (startingLocations.size() > 1 && !startingLocations.contains(NOT_SET)) {
            startingLocations.add(0, NOT_SET);
        }
        return startingLocations;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(final @NotNull TaskForceViewModel o) {
        return taskForce.getValue().compareTo(o.getTaskForce().getValue());
    }
}
