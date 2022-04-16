package engima.waratsea.model.flotilla.deployment;


import engima.waratsea.model.flotilla.deployment.data.DeploymentData;

/**
 * Create flotilla deployment.
 */
public interface FlotillaDeploymentFactory {
    /**
     * Create a flotilla deployment.
     *
     * @param data The flotilla deployment data read in from a JSON file.
     * @return A flotilla deployment.
     */
    FlotillaDeployment create(DeploymentData data);
}
