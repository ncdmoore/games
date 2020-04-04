package engima.waratsea.model.flotilla;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.flotilla.data.FlotillaData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.motorTorpedoBoat.MotorTorpedoBoat;
import engima.waratsea.model.motorTorpedoBoat.MotorTorpedoBoatDAO;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipyardException;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.vessel.Vessel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MotorTorpedoBoatFlotilla implements Flotilla {
    @Getter
    @Setter
    private String name;

    @Getter
    private final Side side;

    @Setter
    private List<MotorTorpedoBoat> boats;

    @Getter
    private String reference; //This is always a map reference and never a name.

    private MotorTorpedoBoatDAO boatDAO;
    private GameMap gameMap;

    /**
     * Constructor called by guice.
     *
     * @param side The side ALLIES or AXIS.
     * @param data The flotilla data read in from a JSON file.
     * @param boatDAO Loads and saves persistent motor torpedo boat data.
     * @param gameMap The game map.
     */
    @Inject
    public MotorTorpedoBoatFlotilla(@Assisted final Side side,
                                    @Assisted final FlotillaData data,
                                              final MotorTorpedoBoatDAO boatDAO,
                                              final GameMap gameMap) {
        this.name = data.getName();
        this.side = side;
        this.gameMap = gameMap;
        this.boatDAO = boatDAO;

        setReference(data.getLocation());

        buildBoats(data.getBoats());
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public FlotillaData getData() {
        FlotillaData data = new FlotillaData();
        data.setName(name);
        data.setLocation(reference);
        data.setBoats(getBoatNames(boats));

        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
        boats.forEach(submarine -> boatDAO.save(submarine));
    }

    /**
     * Get the flotilla's title.
     *
     * @return The flotilla's title.
     */
    @Override
    public String getTitle() {
        return getName();
    }

    /**
     * Determine if the flotilla is at a friendly port.
     *
     * @return True if the flotilla is currently located at a friendly port. False otherwise.
     */
    @Override
    public boolean atFriendlyBase() {
        return gameMap.isLocationBase(side, reference);
    }

    /**
     * Get the flotilla's reference. Return a port if the flotilla is in a port.
     *
     * @return The flotilla's reference. Mapped to a port name if the flotilla is in a port.
     */
    @Override
    public String getMappedLocation() {
        return gameMap.convertPortReferenceToName(reference);
    }

    /**
     * Get the flotilla's new reference.
     *
     * @param newLocation The flotilla's new reference.
     */
    public void setReference(final String newLocation) {
        reference = gameMap.convertNameToReference(newLocation);
    }

    /**
     * Flotilla's are always active.
     *
     * @return True.
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * Get the flotilla's state.
     *
     * @return Active. Flotilla's are always active.
     */
    @Override
    public TaskForceState getState() {
        return TaskForceState.ACTIVE;
    }

    /**
     * The String representation of the flotilla.
     *
     * @return The String representation of the flotilla.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Build all the flotilla's submarines.
     *
     * @param subNames list of sub names.
     */
    private void buildBoats(final List<String> subNames) {
        boats = subNames.stream()
                .map(boatName -> new ShipId(boatName, side))
                .map(this::buildBoat)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Build a given ship.
     *
     * @param shipId Uniquely identifies a ship.
     * @return The constructed ship.
     */
    private Optional<MotorTorpedoBoat> buildBoat(final ShipId shipId) {
        try {
            MotorTorpedoBoat boat = boatDAO.load(shipId);
            boat.setFlotilla(this);
            return Optional.of(boat);
        } catch (ShipyardException ex) {
            log.error("Unable to build sub '{}' for side {}", shipId.getName(), shipId.getSide());
            return Optional.empty();
        }
    }

    /**
     * Get a list of MTB names.
     *
     * @param mtbs A list of MTB objects.
     * @return A list of MTB names.
     */
    private List<String> getBoatNames(final List<MotorTorpedoBoat> mtbs) {
        return mtbs
                .stream()
                .map(boat -> boat.getShipId().getName())
                .collect(Collectors.toList());
    }

    /**
     * Get the vessels (MTBs in this case) for this flotilla.
     *
     * @return A list of vessels that make up the flotilla.
     */
    @Override
    public List<Vessel> getVessels() {
        return  new ArrayList<>(boats);
    }
}
