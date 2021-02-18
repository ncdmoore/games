package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.port.PortView;
import engima.waratsea.model.enemy.views.taskForce.TaskForceView;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.model.victory.VictoryConditions;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NeutralPlayer implements Player {
    private final GameMap gameMap;
    private final AirfieldDAO airfieldDAO;
    private final PortDAO portDAO;

    @Getter private Set<Nation> nations;
    @Getter private List<Airfield> airfields;
    @Getter private List<Port> ports;
    private Map<String, Airfield> airfieldMap;
    private Map<String, Port> portMap;

    @Inject
    public NeutralPlayer(final GameMap gameMap,
                         final AirfieldDAO airfieldDAO,
                         final PortDAO portDAO) {
        this.gameMap = gameMap;
        this.airfieldDAO = airfieldDAO;
        this.portDAO = portDAO;
    }

    /**
     * This gets the side of the player.
     *
     * @return The player's side NEUTRAL.
     */
    @Override
    public Side getSide() {
        return Side.NEUTRAL;
    }

    /**
     * This sets the side of the player.
     *
     * @param side ALLIES or AXIS.
     */
    @Override
    public void setSide(final Side side) {
    }

    /**
     * This gets the nations of the player based on the type of squadron location.
     *
     * @param type The type of squadrons: LAND or SEA.
     * @return A set of the player's nations: BRITSH, ITALIAN, etc...
     */
    @Override
    public Set<Nation> getSquadronNations(final SquadronLocationType type) {
        return nations;
    }

    /**
     * Set the player's nations.
     */
    @Override
    public void setNations() {
        nations = gameMap.getNations(Side.NEUTRAL);
    }

    /**
     * This sets the player's victory conditions.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void buildVictory(final Scenario scenario) {
    }

    /**
     * This sets the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void buildAssets(final Scenario scenario) {
        //Note the airfields and ports used depend upon the map which is set by the scenario.
        airfields = gameMap.getAirfields(Side.NEUTRAL);

        airfieldMap = airfields
                .stream()
                .collect(Collectors.toMap(Airfield::getName, airfield -> airfield));

        ports = gameMap.getPorts(Side.NEUTRAL);

        portMap = ports
                .stream()
                .collect(Collectors.toMap(Port::getName, port -> port));
    }

    /**
     * Deploy assets. This is only called for new games.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void deployAssets(final Scenario scenario) {
    }

    /**
     * This sets the player's views of the enemy assets.
     *
     * @param opposingPlayer The opposing player.
     */
    @Override
    public void buildViews(final Player opposingPlayer) {
    }

    /**
     * This saves the player's victory conditions.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveVictory(final Scenario scenario) {
    }

    /**
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveAssets(final Scenario scenario) {
        portDAO.save(scenario, Side.NEUTRAL, ports);
        airfieldDAO.save(scenario, Side.NEUTRAL, airfields);
    }

    /**
     * Load the player's squadrons.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void loadSquadrons(final Scenario scenario) {
    }

    /**
     * Set the player's squadrons. This is only called on existing games.
     */
    @Override
    public void setSquadrons() {
    }

    /**
     * This gets the player's task forces.
     *
     * @return The player's task forces.
     */
    @Override
    public List<TaskForce> getTaskForces() {
        return Collections.emptyList();
    }

    /**
     * Get the player's task force given its name.
     *
     * @param name The task force name.
     * @return The task force corresponding to the given name.
     */
    @Override
    public TaskForce getTaskForce(final String name) {
        return null;
    }

    /**
     * This gets the enemy player's task force view given its name.
     *
     * @param name The name of the enemy task force.
     * @return The enemy player's task force view corresponding to the given name.
     */
    @Override
    public TaskForceView getEnemyTaskForce(final String name) {
        return null;
    }

    /**
     * Determines if the player has any flotilla's of the given type.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     * @return True if the player has a flotilla of the given type.
     */
    @Override
    public boolean hasFlotilla(final FlotillaType flotillaType) {
        return false;
    }

    /**
     * This gets the player's flotillas.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return The player's flotillas.
     */
    @Override
    public List<Flotilla> getFlotillas(final FlotillaType flotillaType) {
        return Collections.emptyList();
    }

    /**
     * This gets all of the player's squadrons.
     *
     * @return A list of all the player's squadrons.
     */
    @Override
    public List<Squadron> getSquadrons() {
        return Collections.emptyList();
    }

    /**
     * This gets the player's squadrons for the given nation.
     *
     * @param nation       A nation BRITISH, ITALIAN, etc...
     * @param locationType Where the squadron is located: LAND or SEA
     * @return A list of squadrons for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation nation, final SquadronLocationType locationType) {
        return Collections.emptyList();
    }

    /**
     * This gets the player's squadrons for the given location type.
     *
     * @param locationType Where the squadron is located; LAND or SEA.
     * @return A list of squadrons at the given location.
     */
    @Override
    public List<Squadron> getSquadrons(final SquadronLocationType locationType) {
        return Collections.emptyList();
    }

    /**
     * Get the player's airfield given its name.
     *
     * @param name The name of the airfield.
     * @return The airfield corresponding to the given name.
     */
    @Override
    public Airfield getAirfield(final String name) {
        return airfieldMap.get(name);
    }

    /**
     * Get the player's airbase given its name.
     *
     * @param name The name of the airbase.
     * @return The airbase corresponding to the given name.
     */
    @Override
    public Airbase getAirbase(final String name) {
        return airfieldMap.get(name);
    }

    /**
     * This gets the enemy player's airfield view given its name.
     *
     * @param name The name of the enemy airfield.
     * @return The enemy player's airfield view corresponding to the given name.
     */
    @Override
    public AirfieldView getEnemyAirfield(final String name) {
        return null;
    }


    /**
     * Get a port given its name.
     *
     * @param name The name of the port.
     * @return The port corresponding to the given name.
     */
    @Override
    public Port getPort(final String name) {
        return portMap.get(name);
    }

    /**
     * Get the enemy player's port view given its name.
     *
     * @param name The name of the enemy port.
     * @return The enemy port view corresponding to the given name.
     */
    @Override
    public PortView getEnemyPort(final String name) {
        return null;
    }

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @return A list of target for the given mission type.
     */
    @Override
    public List<Target> getTargets(final SeaMissionType missionType) {
        return Collections.emptyList();
    }

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @param nation      The nation: BRITISH, ITALIAN, etc.
     * @return A list of targets for the given mission type.
     */
    @Override
    public List<Target> getTargets(final AirMissionType missionType, final Nation nation) {
        return Collections.emptyList();
    }

    /**
     * This gets the player's minefields.
     *
     * @return The player's minefields.
     */
    @Override
    public List<Minefield> getMinefields() {
        return Collections.emptyList();
    }

    /**
     * Get the player's victory conditions.
     *
     * @return The player's victory conditions.
     */
    @Override
    public VictoryConditions getVictoryConditions() {
        return null;
    }
}
