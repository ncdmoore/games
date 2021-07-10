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
    Patrol createSearch(PatrolData data);

    /**
     * Creates a ASW patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("asw")
    Patrol createAsw(PatrolData data);

    /**
     * Creates a CAP patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("cap")
    Patrol createCap(PatrolData data);

    /**
     * Creates a vitual CAP patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("virtualCap")
    Patrol createVirtualCap(PatrolData data);
}
