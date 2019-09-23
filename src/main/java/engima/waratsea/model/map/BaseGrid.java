package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * This class represents a base grid on the game map.
 */
public class BaseGrid {

    @Getter
    private Side side;

    @Getter
    @Setter
    private GameGrid gameGrid;

    @Getter
    private Port port;

    @Getter
    private Airfield airfield;

    @Getter
    private BaseGridType type;

    /**
     * Constructor.
     *
     * @param port The port contained in the base grid.
     */
    public BaseGrid(@Nonnull final Port port) {
        this.port = port;
        this.type = BaseGridType.PORT;
        this.side = port.getSide();
    }

    /**
     * Constructor.
     *
     * @param airfield The airfield contained in the base grid.
     */
    public BaseGrid(@Nonnull final Airfield airfield) {
        this.airfield = airfield;
        this.type = determineInitialBaseType(airfield);
        this.side = airfield.getSide();
    }

    /**
     * Set the airfield for this grid.
     *
     * @param airfieldValue The airfield that is located at this grid.
     */
    public void setAirfield(@Nonnull final Airfield airfieldValue) {
        airfield = airfieldValue;

        Optional.ofNullable(port)
                .ifPresent(p -> type = determineBaseType(airfield));
    }

    /**
     * Set the port for this grid.
     *
     * @param portValue The port that is located at this grid.
     */
    public void setPort(@Nonnull final Port portValue) {
        port = portValue;

        Optional.ofNullable(airfield)
                .ifPresent(field -> type = determineBaseType(field));
    }

    /**
     * Get the base grid's map reference.
     *
     * @return This base grid's map reference.
     */
    public String getReference() {
        return gameGrid.getMapReference();
    }

    /**
     * Determine the base grid type based on the airfield alone.
     *
     * @param field A given airfield.
     * @return The base grid type.
     */
    private BaseGridType determineInitialBaseType(final Airfield field) {
        return field.getAirfieldType() == AirfieldType.SEAPLANE ? BaseGridType.SEAPLANE : BaseGridType.AIRFIELD;
    }

    /**
     * Determine the base grid type based on the airfield knowing that a port is also present.
     *
     * @param field The given airfield.
     * @return The base grid type.
     */
    private BaseGridType determineBaseType(final Airfield field) {
        return field.getAirfieldType() == AirfieldType.SEAPLANE ? BaseGridType.PORT : BaseGridType.BOTH;
    }
}
