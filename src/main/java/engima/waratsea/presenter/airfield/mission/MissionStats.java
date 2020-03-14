package engima.waratsea.presenter.airfield.mission;

import engima.waratsea.model.base.airfield.mission.AirMissionType;
import lombok.Getter;
import lombok.Setter;


public class MissionStats {

    @Getter
    @Setter
    private AirMissionType missionType;

    @Getter
    @Setter
    private TargetStats targetStats;

    @Getter
    @Setter
    private RegionStats targetRegionStats;

    @Getter
    @Setter
    private RegionStats airfieldRegionStats;

    @Getter
    @Setter
    private SuccessStats successStats;
}
