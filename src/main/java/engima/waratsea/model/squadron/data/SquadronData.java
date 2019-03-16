package engima.waratsea.model.squadron.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents squadron data that is read and written to a JSON file.
 */
public class SquadronData {
    @Getter
    @Setter
    private String model;

    @Getter
    @Setter
    private int strength;

    /**
     * Default constructor called by gson.
     */
    public SquadronData() {
    }

    /**
     * Constructor.
     *
     * @param model The aircraft model in which the squadron is composed.
     */
    public SquadronData(final String model) {
        this.model = model;
        this.strength = 2;
    }

}
