package engima.waratsea.model.squadron.deployment.data;

import lombok.Data;

import java.util.List;

/**
 * Represents a squadron deployment data.
 */
@Data
public class DeploymentData {
    private String name;
    private List<String> mandatory;

    // Use the mandatory half strength to control deployment of half strength squadrons.
    // This is not implemented yet. This is just a place holder
    private List<String> mandatoryHalfStrength;

    private int recon;
    private int fighter;
    private int bomber;
    private int diveBomber;
    private int torpedoBomber;
}
