package engima.waratsea.model.squadron.state;

/**
 * The actions that a squadron may perform.
 */
public enum SquadronAction {
    ASSIGN_TO_PATROL,
    ASSIGN_TO_MISSION,
    REMOVE_FROM_PATROL,
    REMOVE_FROM_MISSION,
    DO_PATROL,
    DO_MISSION,
    RETURN,
    REFIT
}
