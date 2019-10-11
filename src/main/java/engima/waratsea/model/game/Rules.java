package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.squadron.Squadron;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class Rules {

    private GameTitle gameTitle;

    private final Map<GameName, Function<Squadron, Boolean>> aswFilterMap = new HashMap<>();

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

        aswFilterMap.put(GameName.BOMB_ALLEY, bombAlleyAswFilter);
        aswFilterMap.put(GameName.CORAL_SEA,  coralSeaAswFilter);
    }

    /**
     * Execute the ASW filter rule. Determine if the given squadron can perform an ASW patrol.
     *
     * @param squadron Any given squadron.
     * @return True if the given squadron can perform ASW. False otherwise.
     */
    public boolean aswFilter(final Squadron squadron) {
        return aswFilterMap
                .get(gameTitle.getName())
                .apply(squadron);
    }
}
