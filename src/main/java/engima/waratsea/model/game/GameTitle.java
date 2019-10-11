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

    public static final GameName DEFAULT_GAME = GameName.BOMB_ALLEY;

    @Getter
    @Setter
    private GameName name;            //This is the name of the game bombAlley, Coral Sea, etc.

    /**
     * Constructor called by guice.
     */
    @Inject
    public GameTitle() {
        name = DEFAULT_GAME;
    }

    /**
     * Set the value of the game title via it's String representation.
     *
     * @param value The String representation of the enum.
     */
    public void setValue(final String value) {
        name = GameName.convert(value);
    }

    /**
     * Get the String representation of the game title.
     *
     * @return The String representation of the enum.
     */
    public String getValue() {
        return name.getValue();
    }
}
