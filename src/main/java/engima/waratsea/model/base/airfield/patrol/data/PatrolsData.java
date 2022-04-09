package engima.waratsea.model.base.airfield.patrol.data;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import lombok.Data;

import java.util.Map;

@Data
public class PatrolsData {
    private transient Airbase airbase;
    private Map<PatrolType, PatrolData> patrols;
}
