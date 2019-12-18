package engima.waratsea.model.player;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.port.PortView;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.victory.VictoryException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents players in the game. Note, there are only two players/sides: ALLIES and AXIS.
 */
public interface Player {


    /**
     * This gets the side of the player.
     *
     * @return The player's side ALLIES or AXIS.
     */
    Side getSide();

    /**
     * This sets the side of the player.
     *
     * @param side ALLIES or AXIS.
     */
    void setSide(Side side);

    /**
     * This gets the nations of the player.
     *
     * @return A set of the player's nations: BRITISH, ITALIAN, etc...
     */
    Set<Nation> getNations();

    /**
     * Set the player's nations.
     */
    void setNations();

    /**
     * This sets the player's victory conditions.
     *
     * @param scenario The selected scenario.
     * @throws VictoryException Indicates the victory conditions could not be loaded.
     */
    void buildVictory(Scenario scenario) throws VictoryException;

    /**
     * This sets the player's assets.
     *
     * @param scenario The selected scenario.
     * @throws ScenarioException Indicates the assets could not be loaded.
     */
    void buildAssets(Scenario scenario) throws ScenarioException;

    /**
     * Deploy assets. This is only called for new games.
     *
     * @param scenario The selected scenario.
     * @throws ScenarioException Indicates the assets could not be loaded.
     */
    void deployAssets(Scenario scenario) throws ScenarioException;

    /**
     * This sets the player's views of the enemy assets.
     *
     * @param opposingPlayer The opposing player.
     */
    void buildViews(Player opposingPlayer);

    /**
     * This saves the player's victory conditions.
     *
     * @param scenario The selected scenario.
     */
    void saveVictory(Scenario scenario);

    /**
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    void saveAssets(Scenario scenario);

    /**
     * Load the player's squadrons.
     *
     * @param scenario The selected scenario.
     */
    void loadSquadrons(Scenario scenario);

    /**
     * Set the player's squadrons. This is only called on existing games.
     */
    void setSquadrons();

    /**
     * This gets the player's task forces.
     *
     * @return The player's task forces.
     */
    List<TaskForce> getTaskForces();

    /**
     * Determines if the player has any flotilla's of the given type.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     * @return True if the player has a flotilla of the given type.
     */
    boolean hasFlotilla(FlotillaType flotillaType);

    /**
     * This gets the player's flotillas.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return The player's flotillas.
     */
    List<Flotilla> getFlotillas(FlotillaType flotillaType);

    /**
     * This gets all of the player's squadrons.
     *
     * @return A list of all the player's squadrons.
     */
    List<Squadron> getSquadrons();

    /**
     * This gets the player's squadrons for the given nation.
     *
     * @param nation A nation BRITISH, ITALIAN, etc...
     * @return A list of squadrons for the given nation.
     */
    List<Squadron> getSquadrons(Nation nation);

    /**
     * This gets the player's airfields.
     *
     * @return The player's airfields.
     */
    List<Airfield> getAirfields();

    /**
     * Get the player's airfield map. Airfield name to airfield.
     *
     * @return The airfield map.
     */
    Map<String, Airfield> getAirfieldMap();

    /**
     * This gets the enemy player's airfield map.
     *
     * @return The enemy player's airfield map.
     */
    Map<String, AirfieldView> getEnemyAirfieldMap();

    /**
     * This gets the player's ports.
     *
     * @return The player's ports.
     */
    List<Port> getPorts();

    /**
     * Get the player's port map. Port name to port.
     *
     * @return The port map.
     */
    Map<String, Port> getPortMap();

    /**
     * Get the enemy player's port map. Port name to Port view.
     *
     * @return The enemy player's port map.
     */
    Map<String, PortView> getEnemyPortMap();

    /**
     * This gets the player's minefields.
     *
     * @return The player's minefields.
     */
    List<Minefield> getMinefields();
}
