package engima.waratsea.model.base.airfield.mission.data;

import engima.waratsea.model.base.Airbase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MissionsData {
    @Getter @Setter private transient Airbase airbase;
    @Getter @Setter private List<MissionData> missions;

}
