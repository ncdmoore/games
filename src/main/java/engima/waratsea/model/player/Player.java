package engima.waratsea.model.player;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.port.PortView;
import engima.waratsea.model.enemy.views.taskForce.TaskForceView;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryException;

import java.util.List;
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
     * @throws SquadronException if the squadrons cannot be loaded.
     */
    void loadSquadrons(Scenario scenario) throws SquadronException;

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
     * Get the player's task force given its name.
     *
     * @param name The task force name.
     * @return The task force corresponding to the given name.
     */
    TaskForce getTaskForce(String name);

    /**
     * This gets the enemy player's task force view given its name.
     *
     * @param name The name of the enemy task force.
     * @return The enemy player's task force view corresponding to the given name.
     */
    TaskForceView getEnemyTaskForce(String name);

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
     * @param locationType Where the squadron is located: LAND or SEA
     * @return A list of squadrons for the given nation.
     */
    List<Squadron> getSquadrons(Nation nation, SquadronLocationType locationType);

    /**
     * This gets the player's squadrons for the given location type.
     *
     * @param locationType Where the squadron is located; LAND or SEA.
     * @return A list of squadrons at the given location.
     */
    List<Squadron> getSquadrons(SquadronLocationType locationType);

    /**
     * This gets the player's airfields.
     *
     * @return The player's airfields.
     */
    List<Airfield> getAirfields();

    /**
     * Get the player's airfield given its name.
     *
     * @param name The name of the airfield.
     * @return The airfield corresponding to the given name.
     */
    Airfield getAirfield(String name);

    /**
     * This gets the enemy player's airfield view given its name.
     *
     * @param name The name of the enemy airfield.
     * @return The enemy player's airfield view corresponding to the given name.
     */
   AirfieldView getEnemyAirfield(String name);

    /**
     * This gets the player's ports.
     *
     * @return The player's ports.
     */
    List<Port> getPorts();

    /**
     * Get a port given its name.
     *
     * @param name The name of the port.
     * @return The port corresponding to the given name.
     */
    Port getPort(String name);

    /**
     * Get the enemy player's port view given its name.
     *
     * @param name The name of the enemy port.
     * @return The enemy port view corresponding to the given name.
     */
    PortView getEnemyPort(String name);

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @return A list of target for the given mission type.
     */
    List<Target> getTargets(SeaMissionType missionType);

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of targets for the given mission type.
     */
    List<Target> getTargets(AirMissionType missionType, Nation nation);

    /**
     * This gets the player's minefields.
     *
     * @return The player's minefields.
     */
    List<Minefield> getMinefields();

    /**
     * Get the player's victory conditions.
     *
     * @return The player's victory conditions.
     */
    VictoryConditions getVictoryConditions();
}
