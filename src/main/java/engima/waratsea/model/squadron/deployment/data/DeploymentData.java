package engima.waratsea.model.squadron.deployment.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a squadron deployment data.
 */
public class DeploymentData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<String> mandatory;

    // Use the mandatory half strength to control deployment of half strength squadrons.
    // This is not implemented yet. This is just a place holder
    @Getter
    @Setter
    private List<String> mandatoryHalfStrength;

    @Getter
    @Setter
    private int recon;

    @Getter
    @Setter
    private int fighter;

    @Getter
    @Setter
    private int bomber;

    @Getter
    @Setter
    private int diveBomber;

    @Getter
    @Setter
    private int torpedoBomber;
}
