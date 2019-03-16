package engima.waratsea.model.aircraft.data;


import lombok.Getter;
import lombok.Setter;

/**
 * An aircraft's attack factor data.
 */
public class AttackFactorData {
    @Getter
    @Setter
    private int modifier;

    @Getter
    @Setter
    private int full;

    @Getter
    @Setter
    private int half;
}
