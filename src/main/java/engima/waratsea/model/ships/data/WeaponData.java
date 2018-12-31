package engima.waratsea.model.ships.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a ship's weapons.
 */
public class WeaponData {

    @Getter
    @Setter
    private int primary;

    @Getter
    @Setter
    private int secondary;

    @Getter
    @Setter
    private int tertiary;

    @Getter
    @Setter
    private int torpedo;

    @Getter
    @Setter
    private int aa;
}
