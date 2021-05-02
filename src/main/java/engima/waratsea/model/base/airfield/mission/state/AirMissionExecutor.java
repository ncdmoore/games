package engima.waratsea.model.base.airfield.mission.state;

/**
 * This class serves as the 'view' of the air mission state class of the actual mission classes.
 * It lets the state classes call methods on the air missions that other classes should not call.
 */
public abstract class AirMissionExecutor {
    /**
     * Launch the mission. Squadrons take off.
     */
    public abstract void launch();

    /**
     * Progress the mission forward.
     */
    public abstract void fly();

    /**
     * Execute the mission.
     */
    public abstract void execute();

    /**
     * Recall the mission.
     */
    public abstract void recall();

    /**
     * Land the mission. Squadrons land.
     */
    public abstract void land();

    /**
     * Determine if the mission has reached its target.
     *
     * @return True if the mission has reached its target. False otherwise.
     */
    protected abstract boolean reachedTarget();

    /**
     * Determine if the mission has reached its home airbase.
     *
     * @return True if the mission has reached its home airbase. False otherwise.
     */
    protected abstract  boolean reachedHome();
}
