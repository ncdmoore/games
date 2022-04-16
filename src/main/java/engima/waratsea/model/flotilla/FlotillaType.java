package engima.waratsea.model.flotilla;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum FlotillaType {
    SUBMARINE("Submarine", SubmarineFlotilla.class),
    MTB("MTB", MotorTorpedoBoatFlotilla.class);

    @Getter
    private final String value;

    @Getter
    private final Class<?> clazz;         // This is the corresponding flotilla class.

    /**
     * The String representation of the enum.
     *
     * @return The String representation of the enum.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get a stream of this enum's values.
     *
     * @return A stream of this enum's values.
     */
    public static Stream<FlotillaType> stream() {
        return Stream.of(FlotillaType.values());
    }
}
