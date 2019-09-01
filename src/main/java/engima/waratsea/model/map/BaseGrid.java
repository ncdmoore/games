package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import lombok.Getter;
import lombok.Setter;

public class BaseGrid {
    @Getter
    @Setter
    private String reference;

    @Getter
    @Setter
    private GameGrid gameGrid;

    @Getter
    @Setter
    private Port port;

    @Getter
    @Setter
    private Airfield airfield;

    /**
     * Constructor.
     *
     * @param port The port contained in the base grid.
     */
    public BaseGrid(final Port port) {
        this.port = port;
        this.reference = port.getReference();
    }

    /**
     * Constructor.
     *
     * @param airfield The airfield contained in the base grid.
     */
    public BaseGrid(final Airfield airfield) {
        this.airfield = airfield;
        this.reference = airfield.getReference();
    }
}
