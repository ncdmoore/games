package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.ship.data.GunData;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an aircraft carrier.
 */
public class AircraftCarrier implements Ship, Airbase {

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
    private FlightDeck flightDeck;

    @Getter
    private AircraftCapacity aircraftCapacity;

    @Getter
    private List<Squadron> aircraft;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     * @param factory Squadron factory that makes the aircraft carrier's squadrons.
     */
    @Inject
    public AircraftCarrier(@Assisted final ShipData data,
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
        cargo = new Cargo(data.getCargo());

        originPort = data.getOriginPort();

        flightDeck = new FlightDeck(data.getFlightDeck());
        aircraftCapacity = buildAircraftCapacity(flightDeck);

        aircraft = buildSquadrons(data.getAircraft(), factory);
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

        data.setFlightDeck(flightDeck.getData());

        data.setAircraft(PersistentUtility.getData(aircraft));

        return data;
    }

    /**
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    @Override
    public List<Component> getComponents() {
        return Stream.of(hull, flightDeck, aircraftCapacity, secondary, tertiary, antiAir, torpedo, movement, fuel, cargo)
                .filter(Component::isPresent)
                .collect(Collectors.toList());
    }

    /**
     * Get the aircraft carriers maximum squadron capacity in steps.
     *
     * @return The carrier's maximum squadron capacity in steps.
     */
    @Override
    public int getMaxCapacity() {
        return aircraftCapacity.getMaxHealth();
    }

    /**
     * Get The aircraft carrier's current aircraft capacity.
     *
     * @return The current aircraft capacity in steps.
     */
    @Override
    public int getCapacity() {
        return aircraftCapacity.getHealth();
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
     * Get the map reference of the base.
     *
     * @return The map reference of the base.
     */
    @Override
    public String getReference() {
        return taskForce.getLocation();
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
        return shipId.getName().replaceAll("-\\d*$", "");
    }

    /**
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return true;
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
     * Build the aircraft capacity.
     *
     * @param deck The aircraft carrier's flight deck.
     * @return The ship's aircraft carrier capacity.
     */
    private AircraftCapacity buildAircraftCapacity(final FlightDeck deck) {
        AircraftCapacity capacity = new AircraftCapacity();
        capacity.setHealth(deck.getCapacity());
        capacity.setMaxHealth(deck.getMaxCapacity());
        return capacity;
    }

    /**
     * Build the ship squadrons.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param factory The squadron factory that builds the actual squadron.
     * @return A list of squadrons.
     */
    private List<Squadron> buildSquadrons(final List<SquadronData> data, final SquadronFactory factory) {
        return Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronData -> factory.create(shipId.getSide(), squadronData))
                .collect(Collectors.toList());
    }
}
