package engima.waratsea.model.ships.data;

import engima.waratsea.model.ships.ArmourType;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a ship's armour.
 */
public class ArmourData {

    @Getter
    @Setter
    private ArmourType primary;

    @Getter
    @Setter
    private ArmourType flightDeck;

    @Getter
    @Setter
    private ArmourType secondary;

    @Getter
    @Setter
    private ArmourType tertiary;

    @Getter
    @Setter
    private ArmourType aa;

    @Getter
    @Setter
    private ArmourType hull;

    @Getter
    @Setter
    private boolean deck;
}
