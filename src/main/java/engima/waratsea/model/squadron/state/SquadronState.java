package engima.waratsea.model.squadron.state;


import java.util.HashMap;
import java.util.Map;

/**
 * The state of a squadron.
 */
public enum SquadronState {

    READY("Ready") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return readyMap.getOrDefault(action, READY);
        }
    },

    QUEUED_FOR_PATROL("On Patrol") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return queuedForPatrolMap.getOrDefault(action, QUEUED_FOR_PATROL);
        }
    },

    QUEUED_FOR_MISSION("On Mission") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return queuedForMissionMap.getOrDefault(action, QUEUED_FOR_MISSION);
        }
    },

    ON_PATROL("On Patrol") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return onPatrolMap.getOrDefault(action, ON_PATROL);
        }
    },

    ON_MISSION("On Mission") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return onMissonMap.getOrDefault(action, ON_MISSION);
        }
    },

    HANGER("In Hanger") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return inHangerMap.getOrDefault(action, HANGER);
        }
    };

    private static Map<SquadronAction, SquadronState> readyMap = new HashMap<>();
    private static Map<SquadronAction, SquadronState> queuedForPatrolMap = new HashMap<>();
    private static Map<SquadronAction, SquadronState> queuedForMissionMap = new HashMap<>();
    private static Map<SquadronAction, SquadronState> onPatrolMap = new HashMap<>();
    private static Map<SquadronAction, SquadronState> onMissonMap = new HashMap<>();
    private static Map<SquadronAction, SquadronState> inHangerMap = new HashMap<>();


    static {
        readyMap.put(SquadronAction.ASSIGN_TO_MISSION, QUEUED_FOR_MISSION);
        readyMap.put(SquadronAction.ASSIGN_TO_PATROL, QUEUED_FOR_PATROL);

        queuedForPatrolMap.put(SquadronAction.DO_PATROL, ON_PATROL);
        queuedForPatrolMap.put(SquadronAction.REMOVE_FROM_PATROL, READY);

        queuedForMissionMap.put(SquadronAction.DO_MISSION, ON_MISSION);
        queuedForMissionMap.put(SquadronAction.REMOVE_FROM_MISSION, READY);

        onPatrolMap.put(SquadronAction.REMOVE_FROM_PATROL, HANGER);

        onMissonMap.put(SquadronAction.RETURN, HANGER);

        inHangerMap.put(SquadronAction.REFIT, READY);
    }

    private String value;

    /**
     * Transition to a new state.
     *
     * @param action The squadron action or event that occurred.
     * @return The new squadron state.
     */
    public abstract SquadronState transition(SquadronAction action);

    /**
     * The squadron state constructor.
     *
     * @param value The String representation of this enum.
     */
    SquadronState(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
