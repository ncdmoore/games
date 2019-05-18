package engima.waratsea.model.minefield.deployment.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Specifies which grids should be used for minefields for a particular zone.
 */
public class DeploymentData {
    @Getter
    @Setter
    private String name;    //The name of the zone.

    @Getter
    @Setter
    private List<String> grids; //The eligible grids of the minefield zone where a mine is actually laid.
}
