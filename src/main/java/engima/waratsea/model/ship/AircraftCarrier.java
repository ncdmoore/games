package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.AirfieldOperation;
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
 * Represents an squadrons carrier.
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

    private List<LandingType> landingType;

    @Getter
    private List<Squadron> squadrons;

    @Getter
    private Map<AircraftType, List<Squadron>> aircraftTypeMap;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     * @param factory Squadron factory that makes the squadrons carrier's squadrons.
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

        landingType = Optional.ofNullable(data.getLandingType())
                        .orElseGet(Collections::emptyList);

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

        data.setAircraft(PersistentUtility.getData(squadrons));

        data.setLandingType(landingType);

        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
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
     * Get the squadrons carriers maximum squadron capacity in steps.
     *
     * @return The carrier's maximum squadron capacity in steps.
     */
    @Override
    public int getMaxCapacity() {
        return aircraftCapacity.getMaxHealth();
    }

    /**
     * Get The squadrons carrier's current squadrons capacity.
     *
     * @return The current squadrons capacity in steps.
     */
    @Override
    public int getCapacity() {
        return aircraftCapacity.getHealth();
    }

    /**
     * Indicates if this airbase has any squadrons.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    @Override
    public boolean areSquadronsPresent() {
        return !squadrons.isEmpty();
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
     * Determines if this ship is an squadrons carrier.
     *
     * @return True if this ship is an squadrons carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return true;
    }

    /**
     * Determines if this ship has any squadrons carrier based or float planes.
     *
     * @return True if this ship has squadrons. False otherwise.
     */
    @Override
    public boolean hasAircraft() {
        return !squadrons.isEmpty();
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
     * Get a summary map of squadrons type to number of steps of that type.
     *
     * @return A map of squadrons types to number of steps of that type.
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
     * Base a squadron from this airfield.
     *
     * @param squadron The squadron which is now based at this airfield.
     */
    @Override
    public AirfieldOperation addSquadron(final Squadron squadron) {
        Optional.ofNullable(squadron.getAirfield())
                .ifPresent(airfield -> airfield.removeSquadron(squadron));

        AirfieldOperation result = canStation(squadron);

        if (result == AirfieldOperation.SUCCESS) {
            squadrons.add(squadron);
        }

        return result;
    }

    /**
     * Remove a squadron from this airfield.
     *
     * @param squadron The squadron which is removed from this airfield.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
    }

    /**
     * Get the strength in steps of the given list of squadrons.
     *
     * @param squads A list of squadrons of a given squadrons type.
     * @return The total strength of the list of squadrons.
     */
    private BigDecimal sumSteps(final List<Squadron> squads) {
        return squads
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Build the squadrons capacity.
     *
     * @param deck The squadrons carrier's flight deck.
     * @return The ship's squadrons carrier capacity.
     */
    private AircraftCapacity buildAircraftCapacity(final FlightDeck deck) {
        AircraftCapacity capacity = new AircraftCapacity();
        capacity.setHealth(deck.getCapacity());
        capacity.setMaxHealth(deck.getMaxCapacity());
        return capacity;
    }

    /**
     * Build the ship squadrons. Do not examine the landing type. Some
     * scenario's require that carriers be loaded with squadrons that
     * can take off but not land. Thus, we ignore the landing type
     * on initial ship creation.
     *
     * @param data The squadrons data read in from a JSON file.
     * @param factory The squadron factory that builds the actual squadron.
     */
    private void buildSquadrons(final List<SquadronData> data, final SquadronFactory factory) {
        squadrons = Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronData -> factory.create(shipId.getSide(), nationality, squadronData))
                .collect(Collectors.toList());

        aircraftTypeMap = squadrons
                .stream()
                .collect(Collectors.groupingBy(Squadron::getType));
    }

    /**
     * Determine if this airfield has room for another squadron.
     *
     * @param squadron The new squadron.
     * @return True if this airfield can house the new squadron; false otherwise.
     */
    private AirfieldOperation canStation(final Squadron squadron) {
        if (!landingType.contains(squadron.getLandingType())) {
            return AirfieldOperation.LANDING_TYPE_NOT_SUPPORTED;
        }

        return hasRoom(squadron) ? AirfieldOperation.SUCCESS : AirfieldOperation.BASE_FULL;
    }

    /**
     * Determine the current number of steps deployed at this airfield.
     *
     * @return The current number of steps deployed at this airfield.
     */
    private int deployedSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    /**
     * Determine if the squadrons carrier has room for the given squadron.
     *
     * @param squadron A squadron that may be based at this squadrons carrier.
     * @return True if this squadrons carrier has room for the given squadron. False otherwise.
     */
    private boolean hasRoom(final Squadron squadron) {
        int steps = squadron.getSteps().intValue();
        return steps + deployedSteps() <= getMaxCapacity();
    }
}
