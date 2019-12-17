package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.target.data.TargetData;

public class TargetSeaGrid implements Target {

    private String reference;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     */
    @Inject
    public TargetSeaGrid(@Assisted final TargetData data) {

        reference = data.getLocation();
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
        data.setName(reference);
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
