package engima.waratsea.model.ships;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This class represents a task force, which is a collection of ships.
 */
public class TaskForce {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private TaskForceState state;

    @Getter
    @Setter
    private List<String> ships;

    /**
     * The string representation of this object.
     * @return The task force name and title.
     */
    @Override
    public String toString() {
        return name + "-" + title;
    }
}
