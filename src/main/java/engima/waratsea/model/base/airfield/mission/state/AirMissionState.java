package engima.waratsea.model.base.airfield.mission.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Air mission states. Mission may last longer than one turn, thus we need to keep track of a mission's state.
 */
public enum AirMissionState {
    READY("Ready") {
        public AirMissionState transition(final AirMissionAction airMissionAction) {
            return READY_MAP.getOrDefault(airMissionAction, READY);
        }
    },

    LAUNCHING("Launching") {
        public AirMissionState transition(final AirMissionAction airMissionAction) {
            return LAUNCHING_MAP.getOrDefault(airMissionAction, LAUNCHING);
        }
    },

    OUT_BOUND("Out Bound") {
        public AirMissionState transition(final AirMissionAction airMissionAction) {
            return OUT_BOUND_MAP.getOrDefault(airMissionAction, OUT_BOUND);
        }
    },

    IN_BOUND("In Bound") {
        public AirMissionState transition(final AirMissionAction airMissionAction) {
            return IN_BOUND_MAP.getOrDefault(airMissionAction, IN_BOUND);
        }
    },

    DONE("Done") {
        public AirMissionState transition(final AirMissionAction airMissionAction) {
            return DONE;
        }
    };

    private static final Map<AirMissionAction, AirMissionState> READY_MAP = new HashMap<>();
    private static final Map<AirMissionAction, AirMissionState> LAUNCHING_MAP = new HashMap<>();
    private static final Map<AirMissionAction, AirMissionState> OUT_BOUND_MAP = new HashMap<>();
    private static final Map<AirMissionAction, AirMissionState> IN_BOUND_MAP = new HashMap<>();

    static {
        READY_MAP.put(AirMissionAction.CREATE, LAUNCHING);
        LAUNCHING_MAP.put(AirMissionAction.TAKE_OFF, OUT_BOUND);
        OUT_BOUND_MAP.put(AirMissionAction.EXECUTE, IN_BOUND);
        OUT_BOUND_MAP.put(AirMissionAction.RECALL, IN_BOUND);
        IN_BOUND_MAP.put(AirMissionAction.LAND, DONE);
    }


    private final String value;

    /**
     * Transition to a new state.
     *
     * @param action The air mission action or event that occurred.
     * @return The new air mission state.
     */
    public abstract AirMissionState transition(AirMissionAction action);

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
