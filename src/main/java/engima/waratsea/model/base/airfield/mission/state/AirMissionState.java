package engima.waratsea.model.base.airfield.mission.state;

import java.util.Set;

/**
 * Air mission states. Mission may last longer than one turn, thus we need to keep track of a mission's state.
 */
public enum AirMissionState {
    READY("Ready") {
        public AirMissionState transition(final AirMissionAction action, final AirMissionExecutor mission) {
            return action == AirMissionAction.CREATE ? LAUNCHING : READY;
        }
    },

    LAUNCHING("Launching") {
        public AirMissionState transition(final AirMissionAction action, final AirMissionExecutor mission) {
            AirMissionState newState = LAUNCHING;

            if (action == AirMissionAction.EXECUTE) {
                mission.launch();
                mission.fly();
                newState = OUT_BOUND;

                if (mission.reachedTarget()) {
                    mission.execute();
                    newState = IN_BOUND;
                }

                // It is possible to reach the target and return home all in the same turn.
                if (mission.reachedHome()) {
                    mission.land();
                    newState = DONE;
                }
            }

            return newState;
        }
    },

    OUT_BOUND("Out Bound") {
        public AirMissionState transition(final AirMissionAction action, final AirMissionExecutor mission) {
            AirMissionState newState = OUT_BOUND;

            switch (action) {
                case EXECUTE:
                    mission.fly();

                    if (mission.reachedTarget()) {
                        mission.execute();
                        newState = IN_BOUND;
                    }

                    // This may happen for ferry missions. Other missions should stay in-bound
                    // as it took multiple turns to reach the target.
                    if (mission.reachedHome()) {
                        mission.land();
                        newState = DONE;
                    }
                    break;
                case RECALL:

                    mission.recall();

                    newState = IN_BOUND;
                    break;
                default:
                    newState = OUT_BOUND;
            }

            return newState;
        }
    },

    IN_BOUND("In Bound") {
        public AirMissionState transition(final AirMissionAction action, final AirMissionExecutor mission) {
            AirMissionState newState = IN_BOUND;

            if (action == AirMissionAction.EXECUTE) {
                mission.fly();

                if (mission.reachedHome()) {
                    mission.land();
                    newState = DONE;
                }
            }

            return newState;
        }
    },

    DONE("Done") {
        public AirMissionState transition(final AirMissionAction action, final AirMissionExecutor mission) {
            return DONE;
        }
    };

    public static final Set<AirMissionState> READ_ONLY = Set.of(OUT_BOUND, IN_BOUND, DONE);

    private final String value;

    /**
     * Transition to a new state.
     *
     * @param action The air mission action or event that occurred.
     * @param mission The air mission.
     * @return The new air mission state.
     */
    public abstract AirMissionState transition(AirMissionAction action, AirMissionExecutor mission);

    AirMissionState(final String value) {
        this.value = value;
    }

    /**
     * The String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
