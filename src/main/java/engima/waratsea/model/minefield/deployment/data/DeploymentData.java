package engima.waratsea.model.minefield.deployment.data;

import lombok.Data;

import java.util.List;

/**
 * Specifies which grids should be used for minefields for a particular zone.
 */
@Data
public class DeploymentData {
    private String name;    //The name of the zone.
    private List<String> grids; //The eligible grids of the minefield zone where a mine is actually laid.
}
