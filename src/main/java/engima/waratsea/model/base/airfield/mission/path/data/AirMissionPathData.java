package engima.waratsea.model.base.airfield.mission.path.data;

import engima.waratsea.model.base.airfield.mission.AirMissionType;
import lombok.Data;

import java.util.List;

@Data
public class AirMissionPathData {
    private transient AirMissionType type;
    private List<String> gridPath;
    private int currentGridIndex;
}
