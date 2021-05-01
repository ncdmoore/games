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
            return READY_MAP.getOrDefault(action, READY);
        }
    },

    QUEUED_FOR_PATROL("On Patrol (Launching)") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return QUEUED_FOR_PATROL_MAP.getOrDefault(action, QUEUED_FOR_PATROL);
        }
    },

    QUEUED_FOR_MISSION("On Mission (Launching)") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return QUEUED_FOR_MISSION_MAP.getOrDefault(action, QUEUED_FOR_MISSION);
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
            return ON_PATROL_MAP.getOrDefault(action, ON_PATROL);
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
            return ON_MISSION_MAP.getOrDefault(action, ON_MISSION);
        }
    },

    QUEUED_FOR_HANGER("Returning") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return QUEUED_FOR_HANGER_MAP.getOrDefault(action, QUEUED_FOR_HANGER);
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
            return IN_HANGER_MAP.getOrDefault(action, HANGER);
        }
    },

    DESTROYED("Destroyed") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return DESTROYED;
        }
    },

    ALL("All") { //This is used to get all of the squadrons regardless of their state. It represents 'all' states.
                       //No squadron should ever have this state.
        @Override
        public SquadronState transition(final SquadronAction action) {
            throw new RuntimeException(); // No squadron should ever have this state.
        }
    };

    private static final Map<SquadronAction, SquadronState> READY_MAP = new HashMap<>();
    private static final Map<SquadronAction, SquadronState> QUEUED_FOR_PATROL_MAP = new HashMap<>();
    private static final Map<SquadronAction, SquadronState> QUEUED_FOR_MISSION_MAP = new HashMap<>();
    private static final Map<SquadronAction, SquadronState> ON_PATROL_MAP = new HashMap<>();
    private static final Map<SquadronAction, SquadronState> ON_MISSION_MAP = new HashMap<>();
    private static final Map<SquadronAction, SquadronState> QUEUED_FOR_HANGER_MAP = new HashMap<>();
    private static final Map<SquadronAction, SquadronState> IN_HANGER_MAP = new HashMap<>();


    static {
        READY_MAP.put(SquadronAction.ASSIGN_TO_MISSION, QUEUED_FOR_MISSION);
        READY_MAP.put(SquadronAction.ASSIGN_TO_PATROL, QUEUED_FOR_PATROL);

        QUEUED_FOR_PATROL_MAP.put(SquadronAction.TAKE_OFF, ON_PATROL);
        QUEUED_FOR_PATROL_MAP.put(SquadronAction.REMOVE_FROM_PATROL, READY);

        QUEUED_FOR_MISSION_MAP.put(SquadronAction.TAKE_OFF, ON_MISSION);
        QUEUED_FOR_MISSION_MAP.put(SquadronAction.REMOVE_FROM_MISSION, READY);

        ON_PATROL_MAP.put(SquadronAction.REMOVE_FROM_PATROL, QUEUED_FOR_HANGER);
        ON_PATROL_MAP.put(SquadronAction.SHOT_DOWN, DESTROYED);

        ON_MISSION_MAP.put(SquadronAction.LAND, HANGER);
        ON_MISSION_MAP.put(SquadronAction.SHOT_DOWN, DESTROYED);

        QUEUED_FOR_HANGER_MAP.put(SquadronAction.LAND, HANGER);

        IN_HANGER_MAP.put(SquadronAction.REFIT, READY);
    }

    private final String value;

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
