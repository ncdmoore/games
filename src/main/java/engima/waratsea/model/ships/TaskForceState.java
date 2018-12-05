package engima.waratsea.model.ships;

/**
 * Represents a task force state.
 * Active task forces are available for the player to control and use.
 * Reserve task forces are in reservie and are not available to the player.
 * Certain game events will trigger a task force to change states from reserve to active.
 */
public enum TaskForceState {
    ACTIVE,
    RESERVE
}
