package engima.waratsea.model.base.airfield.patrol.data;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import lombok.Data;

import java.util.List;

/**
 * The list of squadrons on the patrol.
 */
@Data
public class PatrolData {
    private transient PatrolType type;
    private transient Airbase airbase;        //This is not persisted. It's here just to pass the airfield to the patrol classes.
    private List<String> squadrons;
}

