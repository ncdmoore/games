package engima.waratsea.model.flotilla.deployment.data;

import lombok.Data;

import java.util.List;

/**
 * Flotilla deployment data read in from a JSON file.
 */
@Data
public class DeploymentData {
    private int priority;       // Lower values indicate higher priority.
    private int number;         // The maximum number of flotilla's to deploy in the following list of grids.
    private List<String> grids; // List of grids where a flotilla may be deployed.
}
