package engima.waratsea.model.submarine.data;

import engima.waratsea.model.ship.Torpedo;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents submarine data read in from a JSON file.
 */
public class SubmarineData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Torpedo torpedo;
}
