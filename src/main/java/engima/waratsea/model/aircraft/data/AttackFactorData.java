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

    @Getter
    @Setter
    private int sixth;

    @Getter
    @Setter
    private boolean defensive;

    /**
     * Build a zero attack factor.
     **/
    public AttackFactorData() {
        modifier = 0;
        full = 0;
        half = 0;
        sixth = 0;
        defensive = false;
    }
}
