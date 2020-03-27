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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SquadronConfigRules {
    private final GameTitle gameTitle;

    private final Map<GameName, Set<SquadronConfig>> gameRules = new HashMap<>();
    private final Map<MissionRole, Set<SquadronConfig>> dropTankRules = new HashMap<>();
    private final Map<AirMissionType, Set<SquadronConfig>> leanEngineRules = new HashMap<>();

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

        dropTankRules.put(MissionRole.ESCORT, Set.of(SquadronConfig.DROP_TANKS));
        dropTankRules.put(MissionRole.MAIN, Set.of(SquadronConfig.NONE));

        leanEngineRules.put(AirMissionType.FERRY, Set.of(SquadronConfig.LEAN_ENGINE));
        leanEngineRules.put(AirMissionType.LAND_STRIKE, Set.of(SquadronConfig.LEAN_ENGINE));
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

        return Stream
                .of(getAllowed(),
                        isLeanEngineAllowed(missionType),
                        areDropTanksAllowed(role),
                        isStrippedDownAllowed(dto),
                        isSearchAllowed(dto))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Get the squadron configurations allowed for the current game.
     *
     * @return Set of squadron configurations allowed for the current game.
     */
    private Set<SquadronConfig> getAllowed() {
        GameName gameName = gameTitle.getName();
        return gameRules.get(gameName);
    }

    /**
     * Get the allowed squadron configuration for the given mission role.
     *
     * @param role The squadron's mission role.
     * @return A set of the allowed squadron configurations given the squadron's role.
     */
    private Set<SquadronConfig> areDropTanksAllowed(final MissionRole role) {
        return dropTankRules.getOrDefault(role, Set.of(SquadronConfig.NONE));
    }

    /**
     * Get the allowed squadron configuration for the given mission type.
     *
     * @param missionType The type of mission that the squadron is assigned.
     * @return A set of the allowed squadron configurations given the type of mission.
     */
    private Set<SquadronConfig> isLeanEngineAllowed(final AirMissionType missionType) {
        return leanEngineRules.getOrDefault(missionType, Set.of(SquadronConfig.NONE));
    }

    /**
     * Get the allowed squadron configuration for the given airfield type and mission type.
     *
     * @param dto The squadron config data transfer object.
     * @return A set of the allowed squadron configurations given the type of mission and type of airfield.
     */
    private Set<SquadronConfig> isStrippedDownAllowed(final SquadronConfigRulesDTO dto) {
        if (dto.getNation() == Nation.BRITISH
                && dto.getMissionType() == AirMissionType.FERRY
                && dto.getAirfieldType() == AirfieldType.TASKFORCE) {
            return Set.of(SquadronConfig.STRIPPED_DOWN);
        }

        return Set.of(SquadronConfig.NONE);
    }

    /**
     * Get the allowed squadron configurations for search.
     *
     * @param dto The squadron config data transfer object.
     * @return A set of squadron configurations.
     */
    private Set<SquadronConfig> isSearchAllowed(final SquadronConfigRulesDTO dto) {
        if (dto.getPatrolType() == PatrolType.SEARCH) {
            return Set.of(SquadronConfig.SEARCH);
        }

        return Set.of(SquadronConfig.NONE);
    }
}
