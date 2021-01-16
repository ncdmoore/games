package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.SquadronConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class SquadronConfigRules {


    private final Map<MissionRole, SquadronConfig> dropTankRules = new HashMap<>();
    private final Map<AirMissionType, SquadronConfig> leanEngineRules = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     */
    @Inject
    public SquadronConfigRules() {
        dropTankRules.put(MissionRole.ESCORT, SquadronConfig.DROP_TANKS);
        dropTankRules.put(MissionRole.MAIN, SquadronConfig.NONE);

        leanEngineRules.put(AirMissionType.FERRY, SquadronConfig.LEAN_ENGINE);
        leanEngineRules.put(AirMissionType.LAND_STRIKE, SquadronConfig.LEAN_ENGINE);
    }

    /**
     * Determine which squadron configuration are allowed for the given mission type and mission role of
     * the squadron.
     *
     * @param dto The data transfer object for squadron configuration rules.
     * @return A set of allowed squadron configurations given the mission type and mission role.
     */
    public Set<SquadronConfig> getAllowed(final SquadronConfigRulesDTO dto) {
        AirMissionType missionType = dto.getMissionType();
        MissionRole role = dto.getMissionRole();

        List<SquadronConfig> allowed = new ArrayList<>(List.of(
                SquadronConfig.NONE,
                isLeanEngineAllowed(missionType),
                areDropTanksAllowed(role),
                isStrippedDownAllowed(dto),
                isSearchAllowed(dto)));

        return new LinkedHashSet<>(allowed);
    }

    /**
     * Get the allowed squadron configuration for the given mission role.
     *
     * @param role The squadron's mission role.
     * @return A set of the allowed squadron configurations given the squadron's role.
     */
    private SquadronConfig areDropTanksAllowed(final MissionRole role) {
        return dropTankRules.getOrDefault(role, SquadronConfig.NONE);
    }

    /**
     * Get the allowed squadron configuration for the given mission type.
     *
     * @param missionType The type of mission that the squadron is assigned.
     * @return A set of the allowed squadron configurations given the type of mission.
     */
    private SquadronConfig isLeanEngineAllowed(final AirMissionType missionType) {
        return leanEngineRules.getOrDefault(missionType, SquadronConfig.NONE);
    }

    /**
     * Get the allowed squadron configuration for the given airfield type and mission type.
     *
     * @param dto The squadron config data transfer object.
     * @return A set of the allowed squadron configurations given the type of mission and type of airfield.
     */
    private SquadronConfig isStrippedDownAllowed(final SquadronConfigRulesDTO dto) {

        if (dto.getMissionType() == AirMissionType.FERRY
                && dto.getAirfieldType() == AirbaseType.CARRIER) {
            return SquadronConfig.STRIPPED_DOWN;
        }

        return SquadronConfig.NONE;
    }

    /**
     * Get the allowed squadron configurations for search.
     *
     * @param dto The squadron config data transfer object.
     * @return A set of squadron configurations.
     */
    private SquadronConfig isSearchAllowed(final SquadronConfigRulesDTO dto) {
        return dto.getPatrolType() == PatrolType.SEARCH
                ? SquadronConfig.SEARCH
                : SquadronConfig.NONE;
    }
}
