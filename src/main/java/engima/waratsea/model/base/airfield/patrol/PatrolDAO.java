package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class PatrolDAO {
    private final Map<PatrolType, Function<PatrolData, Patrol>> factoryMap = new HashMap<>();
    private final Map<PatrolType, Function<PatrolData, Patrol>> virtualFactoryMap = new HashMap<>();


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

        virtualFactoryMap.put(PatrolType.CAP, factory::createVirtualCap);
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

    /**
     * Load the virtual patrol from the patrol data.
     *
     * @param data patrol data read in from a JSON file.
     * @return A newly created virtual patrol.
     */
    public Patrol loadVirtual(final PatrolData data) {
        return virtualFactoryMap.get(data.getType()).apply(data);
    }
}
