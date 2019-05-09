package engima.waratsea.model.squadron.deployment;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.deployment.data.DeploymentData;

/**
 * Factory used by guice to create squadron deployments.
 */
public interface DeploymentFactory {
    /**
     * Creates a squadron deployment.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Deployment data read from a JSON file.
     * @return A Deployment initialized with the data from the JSON file.
     */
    Deployment create(Side side, DeploymentData data);
}
