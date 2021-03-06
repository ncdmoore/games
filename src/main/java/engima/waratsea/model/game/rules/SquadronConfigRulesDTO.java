package engima.waratsea.model.game.rules;

import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import lombok.Getter;

public class SquadronConfigRulesDTO {
    @Getter private AirbaseType airfieldType;
    @Getter private AirMissionType missionType;
    @Getter private MissionRole missionRole;
    @Getter private PatrolType patrolType;

    /**
     * Set the airfield type.
     *
     * @param type The new type.
     * @return This object.
     */
    public SquadronConfigRulesDTO setAirfieldType(final AirbaseType type) {
        airfieldType = type;
        return this;
    }

    /**
     * Set the mission type.
     *
     * @param type The new type.
     * @return This object.
     */
    public SquadronConfigRulesDTO setMissionType(final AirMissionType type) {
        missionType = type;
        return this;
    }

    /**
     * Set the patrol type.
     *
     * @param type The new type.
     * @return This object.
     */
    public SquadronConfigRulesDTO setPatrolType(final PatrolType type) {
        patrolType = type;
        return this;
    }

    /**
     * Set the mission role.
     *
     * @param role The new role.
     * @return This object.
     */
    public SquadronConfigRulesDTO setMissionRole(final MissionRole role) {
        missionRole = role;
        return this;
    }
}
