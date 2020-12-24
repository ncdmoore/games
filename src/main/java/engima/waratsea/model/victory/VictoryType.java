package engima.waratsea.model.victory;

import lombok.Getter;

import java.util.stream.Stream;

public enum VictoryType {
    SHIP("Ship", "Ship events that produce victory points."),
    SQUADRON("Squadron", "Squadron events that produce victory points."),
    AIRFIELD("Airfield", "Airfield events that produce victory points."),
    REQUIRED_SHIP("Required Ship", "Ship events that are required to win the scenario.");

    private final String value;
    @Getter private final String description;

    VictoryType(final String value, final String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
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
    public static Stream<VictoryType> stream() {
        return Stream.of(VictoryType.values());
    }
}
