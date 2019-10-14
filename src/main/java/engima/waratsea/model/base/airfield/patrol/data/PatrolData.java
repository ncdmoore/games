package engima.waratsea.model.base.airfield.patrol.data;

import engima.waratsea.model.base.Airbase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The list of squadrons on the patrol.
 */
public class PatrolData {
    @Getter
    @Setter
    private Airbase airbase;    //This is not persisted. It's here just to pass the airfield to the patrol classes.

    @Getter
    @Setter
    private List<String> squadrons;
}

