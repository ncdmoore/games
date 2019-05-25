package engima.waratsea.model.minefield;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.deployment.MinefieldDeployment;
import engima.waratsea.model.minefield.deployment.MinefieldDeploymentDAO;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This is the minefield AI class. It deploy's minefields for the computer player.
 */
@Slf4j
@Singleton
public class MinefieldAI {

    private static final Random SELECT = new Random();

    private MinefieldDeploymentDAO minefieldDeploymentDAO;

    /**
     * Constructor calle by guice.
     *
     * @param minefieldDeploymentDAO The minefield deployment DAO.
     */
    @Inject
    public MinefieldAI(final MinefieldDeploymentDAO minefieldDeploymentDAO) {
        this.minefieldDeploymentDAO = minefieldDeploymentDAO;
    }

    /**
     * Deploy the given side's minefields.
     *
     * @param selectedScenario The selected scenario.
     * @param computerPlayer The computer player.
     */
    public void deploy(final Scenario selectedScenario, final Player computerPlayer) {
        Side side = computerPlayer.getSide();

        List<Minefield> minefields = computerPlayer.getMinefields();

        if (!minefields.isEmpty()) {
            log.info("Deploy minefields for side: {}", side);

            List<MinefieldDeployment> deployment = minefieldDeploymentDAO.load(side);

            Map<String, List<String>> deploymentMap = getDeploymentMap(deployment);

            minefields.forEach(minefield -> {
                List<String> possibleGrids = deploymentMap.get(minefield.getZoneName());

                if (possibleGrids != null) {
                    for (int i = 0; i < minefield.getNumber(); i++) {
                        String grid = selectGrid(possibleGrids);
                        minefield.addMine(grid);
                        possibleGrids.remove(grid);
                    }
                } else {
                    log.error("No deployment found for minefield: {}", minefield.getZoneName());
                }
            });
        }
    }

    /**
     * Get a deployment map of minefield name/zone to a list of available grids for mining.
     *
     * @param deployment The minefield deployment for a given side.
     * @return A map of minefield name/zone to available grids for mining.
     */
    private Map<String, List<String>> getDeploymentMap(final List<MinefieldDeployment> deployment) {
        return deployment
                .stream()
                .collect(Collectors.toMap(MinefieldDeployment::getZoneName, MinefieldDeployment::getGrids));
    }

    /**
     * Select a grid to mine.
     *
     * @param grids The available grids that may be mined.
     * @return The selected grid to mine.
     */
    private String selectGrid(final List<String> grids) {

        int selection = SELECT.nextInt(grids.size());

        return grids.get(selection);
    }
}
