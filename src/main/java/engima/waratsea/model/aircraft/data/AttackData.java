package engima.waratsea.model.aircraft.data;


import lombok.Data;

/**
 * An aircraft's attack factor data.
 */
@Data
public class AttackData {
    private int modifier;
    private int full;
    private int half;
    private int sixth;
    private boolean defensive;
    private double finalModifier;

    /**
     * Build a zero attack factor.
     **/
    public AttackData() {
        modifier = 0;
        full = 0;
        half = 0;
        sixth = 0;
        defensive = false;
        finalModifier = 1.0;
    }
}
