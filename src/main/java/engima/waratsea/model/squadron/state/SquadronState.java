package engima.waratsea.model.squadron.state;


import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * The state of a squadron.
 */
@RequiredArgsConstructor
public enum SquadronState {

    READY("Ready") {
        /**
         * Transition to a new state.
         *
         * @param action The squadron action or event that occurred.
         * @return The new squadron state.
         */
        public SquadronState transition(final SquadronAction action) {
            return action == null ? READY : READY_MAP.getOrDefault(action, READY);
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
            return action == null ? QUEUED_FOR_PATROL : QUEUED_FOR_PATROL_MAP.getOrDefault(action, QUEUED_FOR_PATROL);
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
            return action == null ? QUEUED_FOR_MISSION : QUEUED_FOR_MISSION_MAP.getOrDefault(action, QUEUED_FOR_MISSION);
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
            return action == null ? ON_PATROL : ON_PATROL_MAP.getOrDefault(action, ON_PATROL);
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
            return action == null ? ON_MISSION : ON_MISSION_MAP.getOrDefault(action, ON_MISSION);
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
            return action == null ? QUEUED_FOR_HANGER : QUEUED_FOR_HANGER_MAP.getOrDefault(action, QUEUED_FOR_HANGER);
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
            return action == null ? HANGER : IN_HANGER_MAP.getOrDefault(action, HANGER);
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

    private static final Map<SquadronAction, SquadronState> READY_MAP = Map.of(
            SquadronAction.ASSIGN_TO_MISSION, QUEUED_FOR_MISSION,
            SquadronAction.ASSIGN_TO_PATROL, QUEUED_FOR_PATROL
    );

    private static final Map<SquadronAction, SquadronState> QUEUED_FOR_PATROL_MAP = Map.of(
            SquadronAction.TAKE_OFF, ON_PATROL,
            SquadronAction.REMOVE_FROM_PATROL, READY
    );

    private static final Map<SquadronAction, SquadronState> QUEUED_FOR_MISSION_MAP = Map.of(
            SquadronAction.TAKE_OFF, ON_MISSION,
            SquadronAction.REMOVE_FROM_MISSION, READY
    );

    private static final Map<SquadronAction, SquadronState> ON_PATROL_MAP = Map.of(
            SquadronAction.REMOVE_FROM_PATROL, QUEUED_FOR_HANGER,
            SquadronAction.SHOT_DOWN, DESTROYED
    );

    private static final Map<SquadronAction, SquadronState> ON_MISSION_MAP = Map.of(
            SquadronAction.LAND, HANGER,
            SquadronAction.SHOT_DOWN, DESTROYED
    );

    private static final Map<SquadronAction, SquadronState> QUEUED_FOR_HANGER_MAP = Map.of(
            SquadronAction.LAND, HANGER
    );

    private static final Map<SquadronAction, SquadronState> IN_HANGER_MAP = Map.of(
            SquadronAction.REFIT, READY
    );

    private final String value;

    /**
     * Transition to a new state.
     *
     * @param action The squadron action or event that occurred.
     * @return The new squadron state.
     */
    public abstract SquadronState transition(SquadronAction action);

    @Override
    public String toString() {
        return value;
    }
}
