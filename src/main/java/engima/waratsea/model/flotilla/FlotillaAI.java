package engima.waratsea.model.flotilla;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.flotilla.deployment.FlotillaDeployment;
import engima.waratsea.model.flotilla.deployment.FlotillaDeploymentDAO;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Represents the flotilla AI. It deploy's flotilla's for both the human and computer player.
 * Flotilla deployment is controlled by a list of deployment zones that have a priority and a
 * maximum number of flotillas that may be deployed in the zone.
 *
 * Starting with the highest priority zone flotillas are randomly deployed within the zone
 * until the maximum number of flotillas for the zone is reached. Then we move on to the next
 * zone in priority and the process continues.
 *
 * Game rules prohibit a flotilla from be deployed in the same zone as a enemy task force,
 * thus if the human player is allowed to deploy his own flotillas it could potentially give
 * vital enemy information away at the start of the game. To avoid this issue the AI deploys
 * all flotillas.
 */
@Singleton
@Slf4j
public class FlotillaAI {

    private static final Random SELECT = new Random();

    private FlotillaDeploymentDAO flotillaDeploymentDAO;

    /**
     * Constructor calle by guice.
     *
     * @param flotillaDeploymentDAO The flotilla deployment DAO.
     */
    @Inject
    public FlotillaAI(final FlotillaDeploymentDAO flotillaDeploymentDAO) {
        this.flotillaDeploymentDAO = flotillaDeploymentDAO;
    }

    /**
     * Deploy the given side's flotillas.
     *
     * @param selectedScenario The selected scenario.
     * @throws ScenarioException if the deployment cannot be loaded.
     * @param player The player.
     */
    public void deploy(final Scenario selectedScenario, final Player player) throws ScenarioException {
        Side side = player.getSide();

        List<Flotilla> flotillas = player.getFlotillas(FlotillaType.SUBMARINE);

        if (!flotillas.isEmpty()) {
            log.info("Deploy flotillas for side: {}", side);

            List<FlotillaDeployment> deployment = flotillaDeploymentDAO
                    .load(side)
                    .stream()
                    .sorted(Comparator.comparingInt(FlotillaDeployment::getPriority))
                    .collect(Collectors.toList());

            flotillas.forEach(flotilla -> {

                FlotillaDeployment flotillaDeployment = deployment.get(0);   // Get the highest ranking deployment.

                if (flotillaDeployment.getNumber() > 0) {
                    String grid = selectGrid(flotillaDeployment.getGrids());
                    flotillaDeployment.deploy(grid);
                    flotilla.setLocation(grid);

                    log.info("Deploy flotilla '{}' to grid: '{}'", flotilla.getName(), grid);

                } else {
                    deployment.remove(flotillaDeployment);
                }
            });
        }
    }

    /**
     * Select a grid to mine.
     *
     * @param grids The available grids that may contain a flotilla.
     * @return The selected grid to that will contain a flotilla.
     */
    private String selectGrid(final List<String> grids) {
        int selection = SELECT.nextInt(grids.size());
        return grids.get(selection);
    }
}
