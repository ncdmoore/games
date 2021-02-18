package engima.waratsea.model.taskForce.patrol;

import com.google.inject.name.Named;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;

public interface PatrolGroupFactory {
    /**
     * Creates a search patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("search")
    PatrolGroup createSearch(PatrolGroupData data);

    /**
     * Creates a ASW patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("asw")
    PatrolGroup createAsw(PatrolGroupData data);

    /**
     * Creates a CAP patrol.
     *
     * @param data The patrol's data.
     * @return The patrol.
     */
    @Named("cap")
    PatrolGroup createCap(PatrolGroupData data);
}
