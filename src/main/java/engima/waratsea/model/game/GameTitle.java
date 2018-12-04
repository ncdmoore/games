package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is a simple utility class that keeps the name/title of the game being played.
 * Many classes depend on this class. It essentially is a global variable that holds the
 * game's title.
 */
@Singleton
public class GameTitle {

    public static final String DEFAULT_GAME = "bombAlley";

    @Getter
    @Setter
    private String value;

    /**
     * Constructor called by guice.
     */
    @Inject
    public GameTitle() {
        value = DEFAULT_GAME;
    }
}
