package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

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

    private Gun primary;
    private Gun secondary;
    private Gun tertiary;
    private Gun antiAir;
    private Torpedo torpedo;
    private Movement movement;
    private Fuel fuel;
    private Hull hull;

    @Getter
    private Cargo cargo;

    @Getter
    private String originPort;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     */
    @Inject
    public SurfaceShip(@Assisted final ShipData data) {
        shipId = data.getShipId();
        type = data.getType();
        shipClass = data.getShipClass();
        nationality = data.getNationality();
        victoryPoints = data.getVictoryPoints();

        primary = new Gun(data.getPrimary());
        secondary = new Gun(data.getSecondary());
        tertiary = new Gun(data.getTertiary());
        antiAir = new Gun(data.getAntiAir());
        torpedo = new Torpedo(data.getTorpedo());

        movement = new Movement(data.getMovement());
        fuel = new Fuel(data.getFuel());
        hull = new Hull(data.getHull());
        cargo = new Cargo((data.getCargo()));

        originPort = data.getOriginPort();
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

        data.setMovement(movement.getData());
        data.setFuel(fuel.getData());
        data.setHull(hull.getData());
        data.setCargo(cargo.getData());

        data.setOriginPort(originPort);

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
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return false;
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
     * Get the ship's surface weapon data.
     *
     * @return A map of the ship's surface weapon data.
     */
    @Override
    public Map<String, String> getSurfaceWeaponData() {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Primary:", primary.getHealth() + "");
        weapons.put("Secondary:", secondary.getHealth() + "");
        weapons.put("Tertiary:", tertiary.getHealth() + "");
        return weapons;
    }

    /**
     * Get the ship's anti air weapon data.
     *
     * @return A map of the ship's anti-air weapon data.
     */
    @Override
    public Map<String, String> getAntiAirWeaponData() {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Anti Air:", antiAir.getHealth() + "");
        return weapons;
    }

    /**
     * Get the ship's torpedo data.
     *
     * @return A map of the ship's torpedo data.
     */
    @Override
    public Map<String, String> getTorpedoData() {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Torpeodo:", torpedo.getHealth() + "");
        return weapons;
    }


    /**
     * Get the ship's armour data.
     *
     * @return A map of the armour type to armour value.
     */
    @Override
    public Map<String, String> getArmourData() {
        Map<String, String> armour = new LinkedHashMap<>();
        armour.put("Primary:", primary.getArmour().toString());
        armour.put("Secondary:", secondary.getArmour().toString());
        armour.put("Tertiary:", tertiary.getArmour().toString());
        armour.put("Anti Air:", antiAir.getArmour().toString());
        armour.put("Hull:", hull.getArmour().toString());

        return armour;
    }

    /**
     * Get the ship's movement data.
     *
     * @return A map of the movement per turn type.
     */
    @Override
    public Map<String, String> getMovementData() {
        Map<String, String> speed = new LinkedHashMap<>();
        speed.put("Even turns:", movement.getEven() + "");
        speed.put("Odd turns:", movement.getOdd() + "");
        return speed;
    }

}
