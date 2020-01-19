package engima.waratsea.model.base.airfield.mission.data;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.game.Nation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MissionData {
    @Getter
    @Setter
    private MissionType type;

    @Getter
    @Setter
    private Nation nation;

    @Getter
    private Airbase airbase;

    @Getter
    @Setter
    private String target;

    @Getter
    @Setter
    private List<String> squadrons;

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
