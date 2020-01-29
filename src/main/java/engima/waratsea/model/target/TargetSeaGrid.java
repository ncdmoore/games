package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetSeaGrid implements Target {

    private String reference;
    private final GameMap gameMap;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param gameMap The game map.
     */
    @Inject
    public TargetSeaGrid(@Assisted final TargetData data,
                                   final GameMap gameMap) {

        this.gameMap = gameMap;
        reference = data.getName();
    }
    /**
     * Get the name of the target.
     *
     * @return The target's name.
     */
    @Override
    public String getName() {
        return reference;
    }

    /**
     * Get the title of the target.
     *
     * @return The target's title.
     */
    @Override
    public String getTitle() {
        return reference;
    }

    /**
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getLocation() {
        return reference;
    }

    /**
     * Get the target persistent data.
     *
     * @return The target's persistent data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setType(TargetType.SEA_GRID);
        data.setName(reference);
        return data;
    }

    /**
     * Get the underlying object of the target.
     *
     * @return The underlying object of the target.
     */
    @Override
    public Object getView() {
        return reference;
    }

    /**
     * The String representation of this target.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return reference;
    }

    /**
     * Determine if this target is equal or is the same as the given target.
     *
     * @param target the target that this target is tested for equality.
     * @return True if this target is equal to the given target. False, otherwise.
     */
    @Override
    public boolean isEqual(final Target target) {
        return reference == target.getView();
    }

    /**
     * Get the distance to this target from the given airbase.
     *
     * @param airbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    @Override
    public int getDistance(final Airbase airbase) {
        String targetReference = gameMap.convertNameToReference(getLocation());
        String airbaseReference = airbase.getReference();

        return gameMap.determineDistance(targetReference, airbaseReference);
    }

    /**
     * Determine if this squadron is in range of the given squadron.
     *
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    @Override
    public boolean inRange(final Squadron squadron) {
        String targetReference = gameMap.convertNameToReference(getLocation());
        String airbaseReference = squadron.getAirfield().getReference();

        return gameMap.inRange(airbaseReference, targetReference, squadron.getMaxRadius());
    }

    /**
     * Get the total number of squadron steps that assigned this target.
     *
     * @return The total number of squadron steps that are assigned this target.
     */
    @Override
    public int getTotalSteps(final Airbase airbase) {
        return 0;
    }

    /**
     * Get the total number of squadron steps that may be assigned to this target.
     *
     * @return The total number of squadron steps that may be assigned to this target.
     */
    @Override
    public int getTotalCapacitySteps() {
        return 0;
    }

    /**
     * Get the number of squadron steps that are currently assigned to this target.
     *
     * @return The number of squadron steps that are currently assigned to this target.
     */
    @Override
    public int getCurrentSteps() {
        return 0;
    }

    /**
     * Determine if this target has capacity for more squadron steps.
     *
     * @return True if this target has capacity for more squadron steps.
     */
    @Override
    public boolean hasCapacity() {
        return true;
    }

    /**
     * Determine if this target has the capacity for the given squadron.
     *
     * @param squadron The squadron assigned the target.
     * @return True if the target has capacity for the given squadron. False otherwise.
     */
    @Override
    public AirfieldOperation hasCapacity(final Squadron squadron) {
        return AirfieldOperation.SUCCESS;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }
}
