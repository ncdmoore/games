package engima.waratsea.model.flotilla.deployment.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Flotilla deployment data read in from a JSON file.
 */
public class DeploymentData {
    @Getter
    @Setter
    private int priority;       // Lower values indicate higher priority.

    @Getter
    @Setter
    private int number;         // The maximum number of flotilla's to deploy in the following list of grids.

    @Getter
    @Setter
    private List<String> grids; // List of grids where a flotilla may be deployed.
}
