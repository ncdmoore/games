package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.deployment.SquadronDeploymentAI;
import engima.waratsea.model.squadron.deployment.SquadronDeploymentManual;
import lombok.extern.slf4j.Slf4j;


/**
 * This class implements all of the squadron artificial intelligence.
 */
@Singleton
@Slf4j
public class SquadronAI {

    private final SquadronDeploymentAI squadronDeploymentAI;
    private final SquadronDeploymentManual squadronDeploymentManual;

    /**
     * Constructor called by guice.
     *
     * @param squadronDeploymentAI The squadron deployment AI.
     * @param squadronDeploymentManual The squadron manual deployment.
     */
    @Inject
    public SquadronAI(final SquadronDeploymentAI squadronDeploymentAI,
                      final SquadronDeploymentManual squadronDeploymentManual) {
        this.squadronDeploymentAI = squadronDeploymentAI;
        this.squadronDeploymentManual = squadronDeploymentManual;
    }

    /**
     * Deploy the player's squadrons.
     *
     * @param scenario The selected scenario.
     * @param player   The player.
     */
    public void deploy(final Scenario scenario, final Player player) {
        squadronDeploymentAI.deploy(scenario, player);
    }

    /**
     * Do nothing except set the region requirements as the human player deploys the squadrons manually.
     *
     * @param scenario The selected scenario.
     * @param player   The player.
     */
    public void manualDeployment(final Scenario scenario, final Player player) {
        squadronDeploymentManual.deploy(player);
    }


}
