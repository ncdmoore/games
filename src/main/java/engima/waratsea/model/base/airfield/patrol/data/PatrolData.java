package engima.waratsea.model.base.airfield.patrol.data;

import engima.waratsea.model.base.airfield.Airfield;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The list of squadrons on the patrol.
 */
public class PatrolData {
    @Getter
    @Setter
    private Airfield airfield;

    @Getter
    @Setter
    private List<String> squadrons;
}
