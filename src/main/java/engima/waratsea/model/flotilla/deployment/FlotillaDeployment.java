package engima.waratsea.model.flotilla.deployment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.flotilla.deployment.data.DeploymentData;
import lombok.Getter;

import java.util.List;

/**
 * Represents the flotilla deployment.
 */
public class FlotillaDeployment {
    @Getter
    private final int priority;

    @Getter
    private int number;

    @Getter
    private final List<String> grids;

    /**
     * Constructor called by guice.
     *
     * @param data The deployment data read in from a JSON file.
     */
    @Inject
    public FlotillaDeployment(@Assisted final DeploymentData data) {
        this.priority = data.getPriority();
        this.number = data.getNumber();
        this.grids = data.getGrids();
    }

    /**
     * Deploy a flotilla to the given grid. This is used to decrease the number of
     * flotilla's desired in this deployment. And to remove a grid from the list
     * of possible flotilla deployment grids.
     *
     * @param grid The grid where the flotilla is deployed.
     */
    public void deploy(final String grid) {
        number--;
        grids.remove(grid);
    }
}
