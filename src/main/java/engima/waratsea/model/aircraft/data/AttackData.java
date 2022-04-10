package engima.waratsea.model.aircraft.data;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An aircraft's attack factor data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttackData {
    private int modifier;
    private int full;
    private int half;
    private int sixth;
    private boolean defensive;
    private double finalModifier = 1.0;
}
