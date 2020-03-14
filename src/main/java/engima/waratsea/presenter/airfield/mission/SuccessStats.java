package engima.waratsea.presenter.airfield.mission;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import lombok.Getter;

import java.util.List;

public class SuccessStats {
    private AirMissionType missionType;
    private final Nation nation;
    private final Airbase airbase;

    @Getter
    private final List<Squadron> squadrons;
    private final Target target;

    /**
     * The constructor.
     *
     * @param missionType The mission type.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param airbase The mission's origin airbase.
     * @param squadrons The squadrons assigned to the mission.
     * @param target  The mission's target.
     */
    public SuccessStats(final AirMissionType missionType, final Nation nation, final Airbase airbase, final List<Squadron> squadrons, final Target target) {
        this.missionType = missionType;
        this.nation = nation;
        this.airbase = airbase;
        this.squadrons = squadrons;
        this.target = target;
    }

    /**
     * Get the mission probability of success.
     *
     * @return A list of mission success probabilities.
     */
    public List<ProbabilityStats> get() {
        AirMission airMission = airbase.getTemporaryMission(missionType, nation, squadrons, target);
        return airMission.getMissionProbability();
    }


}
