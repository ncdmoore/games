package engima.waratsea.model.submarine;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.ship.Fuel;
import engima.waratsea.model.ship.Movement;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.Torpedo;
import engima.waratsea.model.submarine.data.SubmarineData;
import engima.waratsea.model.vessel.Vessel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a submarine.
 */
public class Submarine implements Vessel, PersistentData<SubmarineData> {
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
    private final Torpedo torpedo;

    @Getter
    private final Movement movement;

    @Getter
    private final Fuel fuel;

    @Getter
    @Setter
    private Flotilla flotilla;

    /**
     * Constructor called by guice.
     *
     * @param data The submarine data read in from a JSON file.
     */
    @Inject
    public Submarine(@Assisted final SubmarineData data) {
        this.shipId = data.getShipId();
        this.type = data.getType();
        this.shipClass = data.getShipClass();
        this.nationality = data.getNationality();
        this.victoryPoints = data.getVictoryPoints();

        torpedo = new Torpedo(data.getTorpedo());
        movement = new Movement(data.getMovement());
        fuel = new Fuel(data.getFuel());
    }

    /**
     * Get the persistent submarine data.
     *
     * @return Return the persistent submarine data.
     */
    public SubmarineData getData() {
        SubmarineData data = new SubmarineData();
        data.setShipId(shipId);
        data.setType(type);
        data.setShipClass(shipClass);
        data.setNationality(nationality);
        data.setVictoryPoints(victoryPoints);
        data.setTorpedo(torpedo.getData());
        data.setMovement(movement.getData());
        data.setFuel(fuel.getData());
        return data;
    }

    /**
     * Get the name of the submarine.
     *
     * @return The submarine name.
     */
    public String getName() {
        return shipId.getName();
    }

    /**
     * Get the side of the submarine.
     *
     * @return The submarine's side.
     */
    public Side getSide() {
        return shipId.getSide();
    }

    /**
     * The map reference of the asset.
     *
     * @return The map reference of the asset.
     */
    public String getLocation() {
        return flotilla.getReference();
    }

    /**
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    public List<Component> getComponents() {
        return Stream.of(torpedo, movement, fuel)
                .filter(Component::isPresent)
                .collect(Collectors.toList());
    }
}
