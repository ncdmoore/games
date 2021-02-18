package engima.waratsea.viewmodel.taskforce.air;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This class represents the task force air view model. It contains properties based on the currently selected
 * task force. The presenter populates this class with the selected task force. The view binds its GUI elements
 * to the values in this class.
 */
public class TaskForceAirViewModel implements Comparable<TaskForceAirViewModel> {
    private static final String NOT_SET = "Not Set";

    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty nameAndTitle = new SimpleStringProperty();

    @Getter private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    @Getter private final StringProperty location = new SimpleStringProperty();

    @Getter private final MapProperty<AircraftType, Integer> squadronTypeMap = new SimpleMapProperty<>(FXCollections.emptyObservableMap());
    @Getter private final Map<String, IntegerProperty> squadronCounts = new LinkedHashMap<>();

    @Getter private final ObjectProperty<TaskForce> taskForce = new SimpleObjectProperty<>();

    @Getter private TaskForcesAirViewModel taskForcesAirViewModel;
    private final AirbasesViewModel airbasesViewModel;


    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param airbasesViewModel The airbase view models in this task force. Aircraft carriers, etc...
     */
    @Inject
    public TaskForceAirViewModel(final ImageResourceProvider imageResourceProvider,
                                 final ViewProps props,
                                 final AirbasesViewModel airbasesViewModel) {
        this.airbasesViewModel = airbasesViewModel;

        bindTitles();
        bindSquadronTypeMap();
        bindSquadronCounts();
        bindImages(imageResourceProvider, props);

        airbasesViewModel.setTaskForceAirViewModel(this);
    }

    /**
     * Set teh task forces air view model.
     *
     * @param parent The parent task forces air view model.
     * @return This task force air view model.
     */
    public TaskForceAirViewModel setTaskForcesAirViewModel(final TaskForcesAirViewModel parent) {
        taskForcesAirViewModel = parent;
        return this;
    }

    /**
     * Set the task force model.
     *
     * @param force The selected task force.
     * @return This task force view model.
     */
    public TaskForceAirViewModel setModel(final TaskForce force) {
        taskForce.setValue(force);
        airbasesViewModel.setModel(force);

        String taskForceLocation = Optional
                .ofNullable(force.getLocation())
                .orElse(NOT_SET);

        location.setValue(taskForceLocation);

        return this;
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
     * Save the task force's airbases data to the model.
     */
    public void save() {
        airbasesViewModel.save();
    }

    /**
     * bind the task force titles.
     */
    private void bindTitles() {
        name.bind(Bindings.createStringBinding(() -> Optional.ofNullable(taskForce.getValue()).map(TaskForce::getName).orElse(""), taskForce));
        title.bind(Bindings.createStringBinding(() -> Optional.ofNullable(taskForce.getValue()).map(TaskForce::getTitle).orElse(""), taskForce));
        nameAndTitle.bind(name.concat(new SimpleStringProperty(" ")).concat(title));
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
    public int compareTo(final @NotNull TaskForceAirViewModel o) {
        return taskForce.getValue().compareTo(o.getTaskForce().getValue());
    }
}