package engima.waratsea.model.taskForce.patrol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;

import java.util.Map;
import java.util.function.Function;

@Singleton
public class PatrolGroupDAO {
    private final Map<PatrolType, Function<PatrolGroupData, PatrolGroup>> factoryMap;

    /**
     * Constructor called by guice.
     *
     * @param factory The patrol group factory.
     */
    @Inject
    public PatrolGroupDAO(final PatrolGroupFactory factory) {
        factoryMap = Map.of(
                PatrolType.ASW, factory::createAsw,
                PatrolType.CAP, factory::createCap,
                PatrolType.SEARCH, factory::createSearch
        );
    }

    /**
     * Load the patrol group from the patrol group data.
     *
     * @param data patrol group's data.
     * @return A newly created patrol group.
     */
    public PatrolGroup load(final PatrolGroupData data) {
        return factoryMap
                .get(data.getType())
                .apply(data);
    }
}
