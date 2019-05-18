package engima.waratsea.model.minefield.deployment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.minefield.deployment.data.DeploymentData;
import lombok.Getter;

import java.util.List;

/**
 * Represents a minefield deployemnt.
 */
public class MinefieldDeployment {

    @Getter
    private final String zoneName;

    @Getter
    private final List<String> grids;

    /**
     * The constructor called by guice.
     *
     * @param data The deployment data read in from a JSON file.
     */
    @Inject
    public MinefieldDeployment(@Assisted final DeploymentData data) {
        this.zoneName = data.getName();
        this.grids = data.getGrids();
    }
}
