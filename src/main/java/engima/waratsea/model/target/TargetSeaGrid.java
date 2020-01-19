package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.map.GameGrid;
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
     * The String representation of this target.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return reference;
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

        GameGrid targetGrid = gameMap.getGrid(targetReference);
        GameGrid airbaseGrid = gameMap.getGrid(squadron.getAirfield().getReference());

        // a^2 + b^2 <= c^2, where a, b and c are the sides of the right triangle.
        int a = Math.abs(targetGrid.getRow() - airbaseGrid.getRow());
        int b = Math.abs(targetGrid.getColumn() - airbaseGrid.getColumn());
        int c = squadron.getMaxRadius() + 1;


        log.info("a: {} ,b: {}, c: {}", new Object[]{a, b, c});

        return (a * a) + (b * b) <= (c * c);
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }
}
