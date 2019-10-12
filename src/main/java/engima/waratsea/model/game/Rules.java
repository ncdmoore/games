package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class Rules {

    private GameTitle gameTitle;

    private final Map<PatrolType, Map<GameName, Function<Squadron, Boolean>>> patrolMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param title The game title.
     */
    @Inject
    public Rules(final GameTitle title) {
        this.gameTitle = title;

        Function<Squadron, Boolean> bombAlleyAswFilter = (squadron) -> squadron.getLandingType() != LandingType.LAND;
        Function<Squadron, Boolean> coralSeaAswFilter = (squadron) -> true;

        Function<Squadron, Boolean> capFilter = (squadron) -> squadron.getType() == AircraftType.FIGHTER;

        Function<Squadron, Boolean> searchFilter = (squadron -> true);

        Map<GameName, Function<Squadron, Boolean>> aswFilterMap = new HashMap<>();
        aswFilterMap.put(GameName.BOMB_ALLEY, bombAlleyAswFilter);
        aswFilterMap.put(GameName.CORAL_SEA,  coralSeaAswFilter);

        Map<GameName, Function<Squadron, Boolean>> capFilterMap = new HashMap<>();
        capFilterMap.put(GameName.BOMB_ALLEY, capFilter);
        capFilterMap.put(GameName.CORAL_SEA, capFilter);

        Map<GameName, Function<Squadron, Boolean>> searchFilterMap = new HashMap<>();
        searchFilterMap.put(GameName.BOMB_ALLEY, searchFilter);
        searchFilterMap.put(GameName.CORAL_SEA, searchFilter);

        patrolMap.put(PatrolType.ASW, aswFilterMap);
        patrolMap.put(PatrolType.CAP, capFilterMap);
        patrolMap.put(PatrolType.SEARCH, searchFilterMap);
    }

    /**
     * Execute the ASW filter rule. Determine if the given squadron can perform an ASW patrol.
     *
     * @param patrolType The patrol type.
     * @param squadron Any given squadron.
     * @return True if the given squadron can perform ASW. False otherwise.
     */
    public boolean patrolFilter(final PatrolType patrolType, final Squadron squadron) {
        return patrolMap
                .get(patrolType)
                .get(gameTitle.getName())
                .apply(squadron);
    }
}
