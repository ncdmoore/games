package engima.waratsea.model.squadron.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SquadronConfigRules {

    private final Map<MissionRole, Set<SquadronConfig>> dropTankRules = new HashMap<>();
    private final Map<AirMissionType, Set<SquadronConfig>> leanEngineRules = new HashMap<>();

    /**
     * The constructor called by guice.
     */
    @Inject
    public SquadronConfigRules() {
        dropTankRules.put(MissionRole.ESCORT, new HashSet<>(Arrays.asList(SquadronConfig.NONE, SquadronConfig.DROP_TANKS)));
        dropTankRules.put(MissionRole.MAIN, new HashSet<>(Collections.singletonList(SquadronConfig.NONE)));

        leanEngineRules.put(AirMissionType.FERRY, new HashSet<>(Arrays.asList(SquadronConfig.NONE, SquadronConfig.LEAN_ENGINE)));
        leanEngineRules.put(AirMissionType.LAND_STRIKE, new HashSet<>(Arrays.asList(SquadronConfig.NONE, SquadronConfig.LEAN_ENGINE)));
    }

    /**
     * Determine which squadron configuration are allowed for the given mission type and mission role of
     * the squadron.
     *
     * @param missionType The mission type.
     * @param role The squadron role.
     * @return A set of allowed squadron configurations given the mission type and mission role.
     */
    public Set<SquadronConfig> getAllowed(final AirMissionType missionType, final MissionRole role) {
        return Stream
                .concat(getAllowed(missionType).stream(), getAllowed(role).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Get the allowed squadron configuration for the given mission role.
     *
     * @param role The squadron's mission role.
     * @return A set of the allowed squadron configurations given the squadron's role.
     */
    private Set<SquadronConfig> getAllowed(final MissionRole role) {
        return dropTankRules.getOrDefault(role, new HashSet<>(Collections.singletonList(SquadronConfig.NONE)));
    }

    /**
     * Get the allowed squadron configuration for the given mission type.
     *
     * @param missionType The type of mission that the squadron is assigned.
     * @return A set of the allowed squadron configurations given the type of mission.
     */
    private Set<SquadronConfig> getAllowed(final AirMissionType missionType) {
        return leanEngineRules.getOrDefault(missionType, new HashSet<>(Collections.singletonList(SquadronConfig.NONE)));
    }
}
