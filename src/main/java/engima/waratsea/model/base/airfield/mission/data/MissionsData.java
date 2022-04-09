package engima.waratsea.model.base.airfield.mission.data;

import engima.waratsea.model.base.Airbase;
import lombok.Data;

import java.util.List;

@Data
public class MissionsData {
    private transient Airbase airbase;
    private List<MissionData> missions;

}
