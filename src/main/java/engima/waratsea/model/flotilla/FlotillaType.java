package engima.waratsea.model.flotilla;

import lombok.Getter;

import java.util.stream.Stream;

public enum FlotillaType {
    SUBMARINE("Submarine", SubmarineFlotilla.class),
    MTB("MTB", MotorTorpedoBoatFlotilla.class);

    @Getter
    private final String value;

    @Getter
    private final Class<?> clazz;         // This is the corresponding flotilla class.

    /**
     * Constructor.
     *
     * @param value The String representation of the enum.
     * @param clazz The corresponding flotilla class.
     */
    FlotillaType(final String value, final Class<?> clazz) {
        this.value = value;
        this.clazz = clazz;
    }

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
