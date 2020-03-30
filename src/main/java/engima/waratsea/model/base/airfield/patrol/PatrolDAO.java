package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PatrolDAO {
    private Map<PatrolType, Function<PatrolData, Patrol>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The patrol factory.
     */
    @Inject
    public PatrolDAO(final PatrolFactory factory) {
        factoryMap.put(PatrolType.ASW, factory::createAsw);
        factoryMap.put(PatrolType.CAP, factory::createCap);
        factoryMap.put(PatrolType.SEARCH, factory::createSearch);
    }

    /**
     * Load the patrol from the patrol data.
     *
     * @param data patrol data read in from a JSON file.
     * @return A newly created patrol.
     */
    public Patrol load(final PatrolData data) {
        return factoryMap.get(data.getType()).apply(data);
    }

}
