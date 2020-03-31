package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents a base grid on the game map.
 */
public class BaseGrid implements MarkerGrid {

    @Getter
    private Side side;

    @Getter
    @Setter
    private GameGrid gameGrid;

    private Port port;
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
     * Get the title of the base. Use the airfield title if it exists; otherwise use the port title.
     *
     * @return The base title.
     */
    public String getTitle() {
        return getAirfield()
                .map(Airfield::getTitle)
                .orElseGet(() -> getPort()
                        .map(Port::getName)
                        .orElse("Unknown"));
    }

    /**
     * Get an optional airfield for this base marker.
     *
     * @return An optional airfield.
     */
    public Optional<Airfield> getAirfield() {
        return Optional.ofNullable(airfield);
    }

    /**
     * Get an optional port for this base marker.
     *
     * @return An optional port.
     */
    public Optional<Port> getPort() {
        return Optional.ofNullable(port);
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
     * Get the marker grid's patrol radii map.
     *
     * @return A map of the true maximum patrol radius to a list of
     * patrols that can reach that true maximum radius.
     */
    @Override
    public Optional<Map<Integer, List<Patrol>>> getPatrolRadiiMap() {
        return getAirfield().map(Airfield::getPatrolRadiiMap);
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
