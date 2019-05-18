package engima.waratsea.model.minefield.deployment;

import engima.waratsea.model.minefield.deployment.data.DeploymentData;

/**
 * Create's minefield deployment.
 */
public interface MinefieldDeploymentFactory {
    /**
     * Create a minefield deployment.
     *
     * @param data The minefield deployment data read in from a JSON file.
     * @return A minefield deployment.
     */
    MinefieldDeployment create(DeploymentData data);
}
