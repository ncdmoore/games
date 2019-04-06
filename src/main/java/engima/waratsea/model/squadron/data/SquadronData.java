package engima.waratsea.model.squadron.data;

import engima.waratsea.model.squadron.SquadronStrength;
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
    private SquadronStrength strength;
}
