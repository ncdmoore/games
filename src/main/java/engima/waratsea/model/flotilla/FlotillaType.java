package engima.waratsea.model.flotilla;

import lombok.Getter;

public enum FlotillaType {
    SUBMARINE("Submarine", SubmarineFlotilla.class),
    MTB("MTB", MotorTorpedoBoatFlotilla.class);

    @Getter
    private String value;

    @Getter
    private Class<?> clazz;         // This is the corresponding flotilla class.

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
}
