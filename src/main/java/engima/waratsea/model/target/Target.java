package engima.waratsea.model.target;


import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.Setter;

/**
 * A task force or air strike targets.
 */
public class Target implements PersistentData<TargetData> {

    @Getter
    @Setter
    private String location;

    //private int priority;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param gameMap Game's map.
     */
    @Inject
    public Target(@Assisted final TargetData data,
                            final GameMap gameMap) {
        location = gameMap.convertNameToReference(data.getLocation());
    }

    /**
     * Get the target data that is persisted.
     *
     * @return The persistent target data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setLocation(location);
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }
}
