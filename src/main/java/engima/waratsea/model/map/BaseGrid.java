package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents a base grid on the game map.
 *
 * A base grid may contain an airfield, a port or both an airfield and a port.
 *
 * The type of base grid indicates what the grid contains.
 */
public class BaseGrid implements MarkerGrid {

    private final Provider<GameMap> gameMapProvider;

    @Getter private Side side;
    @Getter private GameGrid gameGrid;
    @Getter private BaseGridType type;
    private Port port;
    private Airfield airfield;

    /**
     * The constructor called by guice.
     *
     * @param gameMapProvider Provides the game map.
     */
    @Inject
    public BaseGrid(final Provider<GameMap> gameMapProvider) {
        this.gameMapProvider = gameMapProvider;
    }

    /**
     * Initialize from a port.
     *
     * @param seaPort The port contained in the base grid.
     * @return This base grid.
     */
    public BaseGrid initPort(@Nonnull final Port seaPort) {
        this.port = seaPort;
        this.type = BaseGridType.PORT;
        this.side = seaPort.getSide();
        this.gameGrid = gameMapProvider
                .get()
                .getGrid(seaPort.getReference())
                .orElseThrow(() -> new RuntimeException("Unable to get port: " + seaPort.getName() + " map reference."));

        return this;
    }

    /**
     * Initialize from an airfield.
     *
     * @param field The airfield contained in the base grid.
     * @return This base grid.
     */
    public BaseGrid initAirfield(@Nonnull final Airfield field) {
        this.airfield = field;
        this.type = determineInitialBaseType(field);
        this.side = field.getSide();
        this.gameGrid = gameMapProvider
                .get()
                .getGrid(field.getReference())
                .orElseThrow(() -> new RuntimeException("Unable to get airfield: " + field.getName() + " map reference."));

        return this;
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
     * Get the marker grid's patrol groups.
     *
     * @param airbaseGroup Unused.
     * @return The marker grids patrol groups.
     */
    @Override
    public Optional<PatrolGroups> getPatrolGroups(final AirbaseGroup airbaseGroup) {
        return getAirfield().map(Airfield::getPatrolGroups);
    }

    /**
     * Get the marker grid's air missions grouped by target.
     *
     * @return A map of the marker grid's air missions keyed by the mission's target.
     */
    @Override
    public Optional<Map<Target, List<AirMission>>> getMissions(final AirbaseGroup airbaseGroup) {
          return getAirfield()
                  .map(Airfield::getMissions)
                  .map(airMissions -> airMissions
                          .stream()
                          .collect(Collectors.groupingBy(AirMission::getTarget)));
    }

    /**
     * Determine the base grid type based on the airfield alone.
     *
     * @param field A given airfield.
     * @return The base grid type.
     */
    private BaseGridType determineInitialBaseType(final Airfield field) {
        return field.getAirbaseType() == AirbaseType.SEAPLANE ? BaseGridType.SEAPLANE : BaseGridType.AIRFIELD;
    }

    /**
     * Determine the base grid type based on the airfield knowing that a port is also present.
     *
     * @param field The given airfield.
     * @return The base grid type.
     */
    private BaseGridType determineBaseType(final Airfield field) {
        return field.getAirbaseType() == AirbaseType.SEAPLANE ? BaseGridType.PORT : BaseGridType.BOTH;
    }
}
