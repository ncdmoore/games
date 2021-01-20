package engima.waratsea.model.game;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum GameName {
    ARCTIC_CONVOY("arcticConvoy"),
    BOMB_ALLEY("bombAlley"),
    CORAL_SEA("coralSea");

    private static final Map<String, GameName> CONVERSION_MAP = new HashMap<>();

    static {
        CONVERSION_MAP.put("arcticConvoy", GameName.ARCTIC_CONVOY);
        CONVERSION_MAP.put("ARCTIC_CONVOY", GameName.ARCTIC_CONVOY);
        CONVERSION_MAP.put("bombAlley", GameName.BOMB_ALLEY);
        CONVERSION_MAP.put("BOMB_ALLEY", GameName.BOMB_ALLEY);
        CONVERSION_MAP.put("coralSea", GameName.CORAL_SEA);
        CONVERSION_MAP.put("CORAL_SEA", GameName.CORAL_SEA);
    }

    /**
     * Convert the String representation to the enum.
     *
     * @param value The String representation of the enum.
     * @return The enum corresponding to the given String representation.
     */
    public static GameName convert(final String value) {
        return CONVERSION_MAP.get(value);
    }

    @Getter
    private final String value;

    /**
     * Constructor.
     *
     * @param value The String representation of this enum.
     */
    GameName(final String value) {
        this.value = value;
    }
}
