package engima.waratsea.model.base.airfield.mission.path.data;

import engima.waratsea.model.base.airfield.mission.AirMissionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AirMissionPathData {
    @Getter @Setter private transient AirMissionType type;
    @Getter @Setter private List<String> gridPath;
    @Getter @Setter private int currentGridIndex;
}
