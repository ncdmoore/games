package engima.waratsea.model.taskForce;

/**
 * Represents a task force's mission.
 */
public enum TaskForceMission {
    BOMBARDMENT("Bombardment"),
    ESCORT("Escort"),
    FERRY("Ferry"),
    INTERCEPT("Intercept"),
    PATROL("Patrol"),
    WAIT_IN_PORT("Wait in port"),
    RETREAT("Retreat"),
    TRANSPORT("Transport");

    private final String value;

    /**
     * Construct a task force mission type.
     * @param value The string value. Used in the GUI.
     */
    TaskForceMission(final String value) {
        this.value = value;
    }

    /**
     * The string representation of the task force mission.
     * @return The string value of the task force mission.
     */
    @Override
    public String toString() {
        return value;
    }
}
