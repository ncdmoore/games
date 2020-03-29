package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class SquadronConfigRules {
    private final GameTitle gameTitle;

    private final Map<GameName, Set<SquadronConfig>> gameRules = new HashMap<>();
    private final Map<MissionRole, SquadronConfig> dropTankRules = new HashMap<>();
    private final Map<AirMissionType, SquadronConfig> leanEngineRules = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param title The game title.
     */
    @Inject
    public SquadronConfigRules(final GameTitle title) {
        this.gameTitle = title;

        gameRules.put(GameName.BOMB_ALLEY, Set.of(
                SquadronConfig.NONE,
                SquadronConfig.DROP_TANKS,
                SquadronConfig.SEARCH,
                SquadronConfig.LEAN_ENGINE,
                SquadronConfig.STRIPPED_DOWN));

        gameRules.put(GameName.CORAL_SEA, Set.of(
                SquadronConfig.NONE,
                SquadronConfig.DROP_TANKS,
                SquadronConfig.SEARCH,
                SquadronConfig.REDUCED_PAYLOAD));

        dropTankRules.put(MissionRole.ESCORT, SquadronConfig.DROP_TANKS);
        dropTankRules.put(MissionRole.MAIN, SquadronConfig.NONE);

        leanEngineRules.put(AirMissionType.FERRY, SquadronConfig.LEAN_ENGINE);
        leanEngineRules.put(AirMissionType.LAND_STRIKE, SquadronConfig.LEAN_ENGINE);
    }

    /**
     * Determine if the current game allows the given configuration.
     *
     * @param config  A squadron configuration.
     * @return True if the current game allows the given configuration. False otherwise.
     */
    public boolean isAllowed(final SquadronConfig config) {
        GameName gameName = gameTitle.getName();

        return gameRules
                .get(gameName)
                .contains(config);
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
                isSearchAllowed(dto),
                isReducedPayloadAllowed(dto)));

        return new HashSet<>(allowed);
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
        GameName gameName = gameTitle.getName();

        if (gameName == GameName.BOMB_ALLEY
                && dto.getNation() == Nation.BRITISH
                && dto.getMissionType() == AirMissionType.FERRY
                && dto.getAirfieldType() == AirfieldType.TASKFORCE) {
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

    /**
     * Determine if reduced payload is allowed.
     *
     * @param dto The squadron config data transfer object.
     * @return The allowed squadron configuration.
     */
    private SquadronConfig isReducedPayloadAllowed(final SquadronConfigRulesDTO dto) {
        return gameTitle.getName() == GameName.CORAL_SEA
                ? SquadronConfig.REDUCED_PAYLOAD
                : SquadronConfig.NONE;
    }
}
