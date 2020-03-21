package engima.waratsea.model.squadron.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SquadronConfigRules {

    private final Map<MissionRole, Set<SquadronConfig>> dropTankRules = new HashMap<>();
    private final Map<AirMissionType, Set<SquadronConfig>> leanEngineRules = new HashMap<>();

    private final Map<AirfieldType, Set<SquadronConfig>> strippedDownRules = new HashMap<>();

    /**
     * The constructor called by guice.
     */
    @Inject
    public SquadronConfigRules() {
        dropTankRules.put(MissionRole.ESCORT, Set.of(SquadronConfig.NONE, SquadronConfig.DROP_TANKS));
        dropTankRules.put(MissionRole.MAIN, Set.of(SquadronConfig.NONE));

        leanEngineRules.put(AirMissionType.FERRY, Set.of(SquadronConfig.NONE, SquadronConfig.LEAN_ENGINE));
        leanEngineRules.put(AirMissionType.LAND_STRIKE, Set.of(SquadronConfig.NONE, SquadronConfig.LEAN_ENGINE));

        strippedDownRules.put(AirfieldType.TASKFORCE, Set.of(SquadronConfig.NONE, SquadronConfig.DROP_TANKS, SquadronConfig.STRIPPED_DOWN));
    }

    /**
     * Determine which squadron configuration are allowed for the given mission type and mission role of
     * the squadron.
     *
     * @param airfieldType The type of airfield: Land, Seaplane, Carrier, etc...
     * @param missionType The mission type.
     * @param role The squadron role.
     * @return A set of allowed squadron configurations given the mission type and mission role.
     */
    public Set<SquadronConfig> getAllowed(final AirfieldType airfieldType, final AirMissionType missionType, final MissionRole role) {
        return Stream
                .of(getAllowed(missionType),
                        getAllowed(role),
                        getAllowed(airfieldType, missionType))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Get the allowed squadron configuration for the given mission role.
     *
     * @param role The squadron's mission role.
     * @return A set of the allowed squadron configurations given the squadron's role.
     */
    private Set<SquadronConfig> getAllowed(final MissionRole role) {
        return dropTankRules.getOrDefault(role, Set.of(SquadronConfig.NONE));
    }

    /**
     * Get the allowed squadron configuration for the given mission type.
     *
     * @param missionType The type of mission that the squadron is assigned.
     * @return A set of the allowed squadron configurations given the type of mission.
     */
    private Set<SquadronConfig> getAllowed(final AirMissionType missionType) {
        return leanEngineRules.getOrDefault(missionType, Set.of(SquadronConfig.NONE));
    }

    /**
     * Get the allowed squadron configuration for the given airfield type and mission type.
     *
     * @param airfieldType The type of mission that the squadron is assigned.
     * @param missionType The squadron role.
     * @return A set of the allowed squadron configurations given the type of mission and type of airfield.
     */
    private Set<SquadronConfig> getAllowed(final AirfieldType airfieldType, final AirMissionType missionType) {
        if (missionType != AirMissionType.FERRY) {
            return Set.of(SquadronConfig.NONE);
        }

        return strippedDownRules.getOrDefault(airfieldType, Set.of(SquadronConfig.NONE));

    }
}
