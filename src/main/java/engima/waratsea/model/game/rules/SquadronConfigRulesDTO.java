package engima.waratsea.model.game.rules;

import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import lombok.Getter;

public class SquadronConfigRulesDTO {
    @Getter private AirfieldType airfieldType;
    @Getter private AirMissionType missionType;
    @Getter private MissionRole missionRole;
    @Getter private Nation nation;

    /**
     * Set the airfield type.
     *
     * @param type The new type.
     * @return This object.
     */
    public SquadronConfigRulesDTO setAirfieldType(final AirfieldType type) {
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
     * Set the mission role.
     *
     * @param role The new role.
     * @return This object.
     */
    public SquadronConfigRulesDTO setMissionRole(final MissionRole role) {
        missionRole = role;
        return this;
    }

    /**
     * Set the nation.
     *
     * @param newNation The new nation.
     * @return This object.
     */
    public SquadronConfigRulesDTO setNation(final Nation newNation) {
        nation = newNation;
        return this;
    }
}
