package engima.waratsea.model.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public enum GameName {
    ARCTIC_CONVOY("arcticConvoy"),
    BOMB_ALLEY("bombAlley"),
    CORAL_SEA("coralSea");

    private static final Map<String, GameName> CONVERSION_MAP = Map.of(
            "arcticConvoy", GameName.ARCTIC_CONVOY,
            "ARCTIC_CONVOY", GameName.ARCTIC_CONVOY,
            "bombAlley", GameName.BOMB_ALLEY,
            "BOMB_ALLEY", GameName.BOMB_ALLEY,
            "coralSea", GameName.CORAL_SEA,
            "CORAL_SEA", GameName.CORAL_SEA
    );

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
}
