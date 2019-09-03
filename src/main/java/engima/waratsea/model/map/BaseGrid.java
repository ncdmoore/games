package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.Airfield;
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
        this.type = BaseGridType.AIRFIELD;
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
                .ifPresent(p -> type = BaseGridType.BOTH);
    }

    /**
     * Set the port for this grid.
     *
     * @param portValue The port that is located at this grid.
     */
    public void setPort(@Nonnull final Port portValue) {
        port = portValue;

        Optional.ofNullable(airfield)
                .ifPresent(a -> type = BaseGridType.BOTH);
    }


    /**
     * Get the base grid's map reference.
     *
     * @return This base grid's map reference.
     */
    public String getReference() {
        return gameGrid.getMapReference();
    }
}
