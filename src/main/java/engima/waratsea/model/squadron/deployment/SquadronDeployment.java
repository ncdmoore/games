package engima.waratsea.model.squadron.deployment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.deployment.data.DeploymentData;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the squadron deployment for a particular airfield.
 * The name field uniquely identifies the airfield.
 */
public class SquadronDeployment {
    @Getter
    @Setter
    private String name; //The airfield name.

    @Getter
    @Setter
    private List<String> mandatory; //List of mandatory aircraft model's that must be deployed.

    private Map<AircraftBaseType, Integer> rankings = new HashMap<>();

    /**
     * Constructor called by guice.
     * @param side The side ALLIES or AXIS.
     * @param data The deployment data read in from a JSON file.
     */
    @Inject
    public SquadronDeployment(@Assisted final Side side,
                              @Assisted final DeploymentData data) {
        name = data.getName();

        mandatory = Optional.ofNullable(data.getMandatory())
                .orElse(Collections.emptyList());

        rankings.put(AircraftBaseType.FIGHTER, data.getFighter());
        rankings.put(AircraftBaseType.BOMBER, data.getBomber());
        rankings.put(AircraftBaseType.RECON, data.getRecon());
    }

    /**
     * Get the ranking of the given base aircraft type.
     *
     * @param type The base aircraft type of the ranking to retrieve.
     * @return The given base aircraft type's ranking.
     */
    public int getRanking(final AircraftBaseType type) {
        return rankings.get(type);
    }
}
