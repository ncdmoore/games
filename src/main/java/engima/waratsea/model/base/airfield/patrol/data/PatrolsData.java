package engima.waratsea.model.base.airfield.patrol.data;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class PatrolsData {
    @Getter @Setter private transient Airbase airbase;
    @Getter @Setter private Map<PatrolType, PatrolData> data;
}
