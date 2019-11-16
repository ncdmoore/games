package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.name.Named;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;

/**
 * Creates ships.
 */
public interface PatrolFactory {
    /**
     * Creates a search patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("search")
    Patrol createSearchPatrol(PatrolData data);

    /**
     * Creates a ASW patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("asw")
    Patrol createAswPatrol(PatrolData data);

    /**
     * Creates a CAP patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("cap")
    Patrol createCapPatrol(PatrolData data);
}
