package engima.waratsea.model.base.airfield.mission.state;

/**
 * Air mission actions.
 */
public enum AirMissionAction {
    CREATE,    // The mission is created.
    RECALL,    // An in progress mission may be recalled.
    EXECUTE,   // Perform the mission.
}
