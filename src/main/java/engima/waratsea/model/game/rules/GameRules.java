package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.TurnType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.weather.WeatherType;

import java.util.Set;


/**
 * Define the per game rules in this class. These are rules that very per game. Current games supported are:
 *  Bomb Alley
 *  Coral Sea
 *
 *  This is a facade class for the various game rules.
 */
@Singleton
public class GameRules {
    private final TurnRules turnRules;
    private final WeatherRules weatherRules;
    private final AirMissionRules airMissionRules;
    private final PatrolRules patrolRules;
    private final SquadronConfigRules squadronConfigRules;

    /**
     * Constructor called by guice.
     *
     * @param turnRules The game turn rules.
     * @param weatherRules The game weather rules.
     * @param airMissionRules The game air mission rules.
     * @param patrolRules The game patrol rules.
     * @param squadronConfigRules The game squadron configuration rules.
     */
    @Inject
    public GameRules(final TurnRules turnRules,
                     final WeatherRules weatherRules,
                     final AirMissionRules airMissionRules,
                     final PatrolRules patrolRules,
                     final SquadronConfigRules squadronConfigRules) {
        this.turnRules = turnRules;
        this.weatherRules = weatherRules;
        this.airMissionRules = airMissionRules;
        this.patrolRules = patrolRules;
        this.squadronConfigRules = squadronConfigRules;
    }

    /**
     * Execute the Twilight turn rule. Determine the true type of a twilight turn. It will either be
     * DAY or NIGHT.
     *
     * @param month The game month.
     * @return DAY or NIGHT.
     */
    public TurnType getTwilightTurnType(final int month) {
        return turnRules.getTwilightTurnType(month);
    }

    /**
     * Execute the Mission filter rule. Determine if the given squadron can perform the given mission type.
     *
     * @param missionType The mission type.
     * @param squadron Any given squadron.
     * @return True if the given squadron can perform the given mission. False otherwise.
     */
    public boolean missionFilter(final AirMissionType missionType, final Squadron squadron) {
        return airMissionRules.missionFilter(missionType, squadron);
    }

    /**
     * Execute the Patrol filter rule. Determine if the given squadron can perform the given patrol type.
     *
     * @param patrolType The patrol type.
     * @param squadron Any given squadron.
     * @return True if the given squadron can perform the given patrol. False otherwise.
     */
    public boolean patrolFilter(final PatrolType patrolType, final Squadron squadron) {
        return patrolRules.patrolFilter(patrolType, squadron);
    }

    /**
     * Execute the weather rule. Determine the turn's weather.
     *
     * @param result The dice result.
     * @param current The current weather.
     * @param month The current month.
     * @return The new weather.
     */
    public WeatherType determineWeather(final int result, final WeatherType current, final int month) {
        return weatherRules.determineWeather(result, current, month);
    }

    /**
     * Determine which squadron configuration are allowed for the given mission type and mission role of
     * the squadron.
     *
     * @param dto The data transfer object for squadron configuration rules.
     * @return A set of allowed squadron configurations given the mission type and mission role.
     */
    public Set<SquadronConfig> getAllowedSquadronConfig(final SquadronConfigRulesDTO dto) {
        return squadronConfigRules.getAllowed(dto);
    }
}
