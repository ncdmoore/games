package engima.waratsea.model.base.airfield.mission.state;

/**
 * Air mission actions.
 */
public enum AirMissionAction {
    CREATE,    // The mission is created.
    TAKE_OFF,  // The mission takes off.
    RECALL,    // An out bound mission may be recalled.
    EXECUTE,   // Perform the mission.
    LAND       // AN in bound mission lands.
}
