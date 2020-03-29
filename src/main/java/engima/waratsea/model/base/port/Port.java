package engima.waratsea.model.base.port;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.port.data.PortData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a port within the game.
 */
public class Port implements Asset, Base, PersistentData<PortData> {
    @Getter private final Side side;
    @Getter private final String name;
    @Getter private final String size;
    @Getter private final int antiAirRating;

    @Getter
    private final String reference; // A simple string is used to prevent circular logic on mapping names and references.
                                    // Ports are used to map port names to map references. Thus, we just need a map reference.

    @Getter
    private List<TaskForce> taskForces = new ArrayList<>();
    /**
     * Constructor called by guice.
     *
     * @param data The port data read in from a JSON file.
     */
    @Inject
    public Port(@Assisted final PortData data) {
        side = data.getSide();
        name = data.getName();
        size = data.getSize();
        antiAirRating = data.getAntiAir();
        reference = data.getLocation();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public PortData getData() {
       PortData data = new PortData();
       data.setName(name);
       data.setSize(size);
       data.setLocation(reference);
       return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }

    /**
     * The String representation of the port.
     *
     * @return The String representation of the port.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Add a task force to this port.
     *
     * @param taskForce The task force added to this port.
     */
    public void addTaskForce(final TaskForce taskForce) {
        taskForces.add(taskForce);
    }

    /**
     * Determine if this port has task forces present.
     *
     * @return True if task forces are present at this port. False, otherwsie.
     */
    public boolean areTaskForcesPresent() {
        return !taskForces.isEmpty();
    }

    /**
     * Get the title of the asset that is displayed on the GUI.
     *
     * @return The asset title.
     */
    @Override
    public String getTitle() {
        return name;
    }

    /**
     * Get the active state of the asset.
     *
     * @return True if the asset is active. False if the asset is not active.
     */
    @Override
    public boolean isActive() {
        return true;
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
    public int compareTo(@NotNull final Base o) {
        return getTitle().compareTo(o.getTitle());
    }
}
