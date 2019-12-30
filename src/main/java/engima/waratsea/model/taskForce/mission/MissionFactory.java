package engima.waratsea.model.taskForce.mission;

import com.google.inject.name.Named;
import engima.waratsea.model.taskForce.mission.data.MissionData;

public interface MissionFactory {

    /**
     * Create a task force air raid mission.
     *
     * @param data The air raid mission data read in from a JSON file.
     * @return A task force air raid mission.
     */
    @Named("airRaid")
    Mission createAirRaid(MissionData data);
    /**
     * Create a task force bombardment mission.
     *
     * @param data The bombardment data read in from a JSON file.
     * @return A task force bombardment mission.
     */
    @Named("bombardment")
    Mission createBombardment(MissionData data);

    /**
     * Create a task force escort mission.
     *
     * @param data The escort mission data read in from a JSON file.
     * @return A task force escort mission.
     */
    @Named("escort")
    Mission createEscort(MissionData data);

    /**
     * Create a task force ferry mission.
     *
     * @param data The ferry mission data read in from a JSON file.
     * @return A task force ferry mission.
     */
    @Named("ferry")
    Mission createFerry(MissionData data);

    /**
     * Create a task force ferry aircraft mission.
     *
     * @param data The ferry aircraft mission data read in from a JSON file.
     * @return A task force ferry aircraft mission.
     */
    @Named("ferryAircraft")
    Mission createFerryAircraft(MissionData data);

    /**
     * Create a task force intercept mission.
     *
     * @param data The intercept mission data read in from a JSON file.
     * @return A task force intercept mission.
     */
    @Named("intercept")
    Mission createIntercept(MissionData data);

    /**
     * Create a task force invasion mission.
     *
     * @param data The invasion mission data read in from a JSON file.
     * @return A task force invasion mission.
     */
    @Named("invasion")
    Mission createInvasion(MissionData data);

    /**
     * Create a task force mine laying mission.
     *
     * @param data The mine laying mission data read in from a JSON file.
     * @return A task force mine laying mission.
     */
    @Named("minelaying")
    Mission createMinelaying(MissionData data);

    /**
     * Create a task force patrol mission.
     *
     * @param data The patrol mission data read in from a JSON file.
     * @return A task force patrol mission.
     */
    @Named("patrol")
    Mission createPatrol(MissionData data);

    /**
     * Create a task force transport mission.
     *
     * @param data The transport mission data read in from a JSON file.
     * @return A task force transport mission.
     */
    @Named("transport")
    Mission createTransport(MissionData data);
}
