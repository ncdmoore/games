package engima.waratsea.model.base.airfield.mission.data;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.game.Nation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class MissionData {
    @Getter @Setter private AirMissionType type;
    @Getter @Setter private Nation nation;
    @Getter private transient Airbase airbase;
    @Getter @Setter private String target;
    @Getter @Setter private Map<MissionRole, List<String>> squadronMap;

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
