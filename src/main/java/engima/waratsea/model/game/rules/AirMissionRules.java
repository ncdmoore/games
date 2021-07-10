package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.squadron.Squadron;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This represents the air mission rules for the game.
 */
@Singleton
public class AirMissionRules {
    private final GameTitle gameTitle;

    private final Map<AirMissionType, Map<GameName, Function<Squadron, Boolean>>> missionMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param title The game title.
     */
    @Inject
    public AirMissionRules(final GameTitle title) {
        this.gameTitle = title;

        buildAirMissionRules();
    }

    /**
     * Execute the Mission filter rule. Determine if the given squadron can perform the given mission type.
     *
     * @param missionType The mission type.
     * @param squadron Any given squadron.
     * @return True if the given squadron can perform the given mission. False otherwise.
     */
    public boolean missionFilter(final AirMissionType missionType, final Squadron squadron) {
        return missionMap
                .get(missionType)
                .get(gameTitle.getName())
                .apply(squadron);
    }

    /**
     * Build the air mission rules.
     */
    private void buildAirMissionRules() {

        Function<Squadron, Boolean> sweepFilter = (squadron -> squadron.getType() == AircraftType.FIGHTER);

        Function<Squadron, Boolean> noFilter = (squadron -> true);

        Map<GameName, Function<Squadron, Boolean>> sweepFilterMap = new HashMap<>();
        sweepFilterMap.put(GameName.BOMB_ALLEY, sweepFilter);
        sweepFilterMap.put(GameName.CORAL_SEA, sweepFilter);

        Map<GameName, Function<Squadron, Boolean>> noFilterMap = new HashMap<>();
        noFilterMap.put(GameName.BOMB_ALLEY, noFilter);
        noFilterMap.put(GameName.CORAL_SEA, noFilter);

        missionMap.put(AirMissionType.DISTANT_CAP, sweepFilterMap);
        missionMap.put(AirMissionType.FERRY, noFilterMap);
        missionMap.put(AirMissionType.LAND_STRIKE, noFilterMap);
        missionMap.put(AirMissionType.NAVAL_PORT_STRIKE, noFilterMap);
        missionMap.put(AirMissionType.NAVAL_PORT_RECON, noFilterMap);
        missionMap.put(AirMissionType.NAVAL_TASK_FORCE_STRIKE, noFilterMap);
        missionMap.put(AirMissionType.SWEEP_AIRFIELD, sweepFilterMap);
        missionMap.put(AirMissionType.SWEEP_PORT, sweepFilterMap);
    }
}
