package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ship.data.GunData;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a surface ship: Battleship, cruisers, etc.
 */
public class SurfaceShip implements Ship {

    @Getter
    private final ShipId shipId;

    @Getter
    private final ShipType type;

    @Getter
    private final String shipClass;

    @Getter
    private final Nation nationality;

    @Getter
    private final int victoryPoints;

    @Getter
    @Setter
    private TaskForce taskForce;

    @Getter
    private Gun primary;

    @Getter
    private Gun secondary;

    @Getter
    private Gun tertiary;

    @Getter
    private Gun antiAir;

    @Getter
    private Torpedo torpedo;

    @Getter
    private Asw asw;

    @Getter
    private Movement movement;

    @Getter
    private Fuel fuel;

    @Getter
    private Hull hull;

    @Getter
    private Cargo cargo;

    @Getter
    private String originPort;

    @Getter
    private List<Squadron> aircraft;

    @Getter
    private Map<AircraftType, List<Squadron>> aircraftTypeMap;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     * @param factory The squadron factory.
     */
    @Inject
    public SurfaceShip(@Assisted final ShipData data,
                       final SquadronFactory factory) {
        shipId = data.getShipId();
        type = data.getType();
        shipClass = data.getShipClass();
        nationality = data.getNationality();
        victoryPoints = data.getVictoryPoints();

        primary = buildGun("Primary", data.getPrimary());
        secondary = buildGun("Secondary", data.getSecondary());
        tertiary = buildGun("Tertiary", data.getTertiary());
        antiAir = buildGun("Anti-Air", data.getAntiAir());
        torpedo = new Torpedo(data.getTorpedo());
        asw = new Asw(data.getAsw());

        movement = new Movement(data.getMovement());
        fuel = new Fuel(data.getFuel());
        hull = new Hull(data.getHull());
        cargo = new Cargo((data.getCargo()));

        originPort = data.getOriginPort();

        buildSquadrons(data.getAircraft(), factory);
    }

    /**
     * Build a gun.
     *
     * @param name The name of the gun.
     * @param data The gun's data.
     * @return The gun.
     */
    private Gun buildGun(final String name, final GunData data) {
        data.setName(name);
        return new Gun(data);
    }

    /**
     * Get the ship's persistent data.
     *
     * @return The ship's persistent data.
     */
    @Override
    public ShipData getData() {
        ShipData data = new ShipData();
        data.setShipId(shipId);
        data.setType(type);
        data.setShipClass(shipClass);
        data.setNationality(nationality);
        data.setVictoryPoints(victoryPoints);

        data.setPrimary(primary.getData());
        data.setSecondary(secondary.getData());
        data.setTertiary(tertiary.getData());
        data.setAntiAir(antiAir.getData());
        data.setTorpedo(torpedo.getData());
        data.setAsw(asw.getData());

        data.setMovement(movement.getData());
        data.setFuel(fuel.getData());
        data.setHull(hull.getData());
        data.setCargo(cargo.getData());

        data.setOriginPort(originPort);

        data.setAircraft(PersistentUtility.getData(aircraft));

        return data;
    }

    /**
     * Get the ship's side: ALLIES or AXIS.
     *
     * @return The ship's side.
     */
    @Override
    public Side getSide() {
        return shipId.getSide();
    }

    /**
     * Get the ship's name.
     *
     * @return The ship's name.
     */
    @Override
    public String getName() {
        return shipId.getName();
    }

    /**
     * Get the ship's title. Some ships have revisions or configurations in their name.
     * The getName routine returns this extra information. The get title routine only
     * returns the ship's name/title.
     *
     * @return The ship's title.
     */
    @Override
    public String getTitle() {
        return getName();
    }

    /**
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return false;
    }

    /**
     * Determines if this ship has any aircraft carrier based or float planes.
     *
     * @return True if this ship has aircraft. False otherwise.
     */
    @Override
    public boolean hasAircraft() {
        return !aircraft.isEmpty();
    }

    /**
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    @Override
    public List<Component> getComponents() {
        return Stream.of(hull, primary, secondary, tertiary, antiAir, torpedo, movement, fuel, cargo)
                .filter(Component::isPresent)
                .collect(Collectors.toList());
    }

    /**
     * Call this method to inform the ship that it is sailing from port.
     */
    @Override
    public void setSail() {
        originPort = taskForce.getLocation();
    }

    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    @Override
    public void loadCargo() {
        cargo.load();
    }

    /**
     * Get a summary map of aircraft type to number of steps of that type.
     *
     * @return A map of aircraft types to number of steps of that type.
     */
    @Override
    public Map<AircraftType, BigDecimal> getSquadronSummary() {
        return aircraftTypeMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          e -> sumSteps(e.getValue()),
                                          BigDecimal::add));
    }

    /**
     * Get the strength in steps of the given list of squadrons.
     *
     * @param squadrons A list of squadrons of a given aircraft type.
     * @return The total strength of the list of squadrons.
     */
    private BigDecimal sumSteps(final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Build the ship squadrons.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param factory The squadron factory that builds the actual squadron.
     */
    private void buildSquadrons(final List<SquadronData> data, final SquadronFactory factory) {
        aircraft =  Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronData -> factory.create(shipId.getSide(), squadronData))
                .collect(Collectors.toList());

        aircraftTypeMap = aircraft
                .stream()
                .collect(Collectors.groupingBy(Squadron::getType));
    }
}
