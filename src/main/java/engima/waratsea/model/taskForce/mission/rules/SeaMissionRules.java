package engima.waratsea.model.taskForce.mission.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.mission.SeaMissionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class SeaMissionRules {

    private Map<Side, Player> players = new HashMap<>();

    private List<SeaMissionType> unqualified = List.of(SeaMissionType.FERRY, SeaMissionType.PATROL, SeaMissionType.RETREAT);

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     */
    @Inject
    public SeaMissionRules(final Game game) {
        players.put(Side.ALLIES, game.getPlayer(Side.ALLIES));
        players.put(Side.AXIS, game.getPlayer(Side.AXIS));
    }

    /**
     * Determine which missions the given task force can perform.
     *
     * @param taskForce The task force.
     * @return A list of missions that the task force may perform.
     */
    public List<SeaMissionType> getMissions(final TaskForce taskForce) {
        List<SeaMissionType> missions = new ArrayList<>(unqualified);

        Map<ShipType, List<Ship>> ships = taskForce.getShipTypeMap();

        if (ships.containsKey(ShipType.AIRCRAFT_CARRIER)) {
            missions.add(SeaMissionType.AIR_RAID);
            missions.add(SeaMissionType.FERRY_AIRCRAFT);
        }

        if (ships.containsKey(ShipType.MINESWEEPER)) {
            missions.add(SeaMissionType.MINESWEEPING);
        }

        if (ships.containsKey(ShipType.MINELAYER)) {
            missions.add(SeaMissionType.MINELAYING);
        }

        if (taskForce.atFriendlyBase() || !taskForce.getCargoShips().isEmpty()) {
            missions.add(SeaMissionType.TRANSPORT);
        }

        // Determine if there is another task force that is not on an escort mission.
        // Task force's on escort missions cannot be escorted.
        boolean escort = players
                .get(taskForce.getSide())
                .getTaskForces()
                .stream()
                .filter(tf -> !tf.getName().equalsIgnoreCase(taskForce.getName()))
                .anyMatch(tf -> tf.getMission().getType() != SeaMissionType.ESCORT);

        if (escort) {
            missions.add(SeaMissionType.ESCORT);
        }

        if (!players.get(taskForce.getSide()).getTargets(SeaMissionType.INTERCEPT).isEmpty()) {
            missions.add(SeaMissionType.INTERCEPT);
        }

        if (taskForce.atFriendlyBase() || taskForce.hasBombardmentAmmo()) {
            missions.add(SeaMissionType.BOMBARDMENT);
        }

        return missions;
    }
}
