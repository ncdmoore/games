package engima.waratsea.model.base.airfield.mission.data;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.game.Nation;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MissionData {
    private int id;
    private AirMissionState state;
    private AirMissionType type;
    private Nation nation;
    private transient Airbase airbase;
    private String target;
    private Map<MissionRole, List<String>> squadronMap;
    private AirMissionPathData airMissionPathData;

    /**
     * Set the air base.
     *
     * @param base The air base.
     * @return This mission data.
     */
    public MissionData setAirbase(final Airbase base) {
        this.airbase = base;
        return this;
    }
}
