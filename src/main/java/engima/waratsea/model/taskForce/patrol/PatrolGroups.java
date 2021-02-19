package engima.waratsea.model.taskForce.patrol;

import com.google.inject.Inject;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatrolGroups {
    private final Map<PatrolType, PatrolGroup> patrolMap = new HashMap<>();

    private final PatrolGroupDAO patrolGroupDAO;

    @Getter @Setter private AirbaseGroup airbaseGroup;  // The corresponding airbase group of these patrol groups.
    @Getter @Setter private AirbaseGroup homeGroup;     // The home airbase group of these patrol groups. The home group
                                                        // is represented by a single task force or airfield. This is
                                                        // needed to correlate the patrol groups with a task force marker
                                                        // on the game map.
    /**
     * Constructor called by guice.
     *
     * @param patrolGroupDAO Patrol Group data access object.
     */
    @Inject
    public PatrolGroups(final PatrolGroupDAO patrolGroupDAO) {
        this.patrolGroupDAO = patrolGroupDAO;
    }

    /**
     * Build the patrol groups for the given airbase group.
     *
     * @param group The airbase group for which the patrol groups are built.
     * @return The newly constructed patrol groups for the given airbase group.
     */
    public PatrolGroups build(final AirbaseGroup group) {
        airbaseGroup = group;
        homeGroup = group;  // By default the home group is equal to the airbase group.
                            // Task force groups will override this default.
        PatrolType
                .stream()
                .forEach(this::buildPatrolGroups);

        return this;
    }

    /**
     * Update the patrol groups with the current data from the air base group.
     */
    public void update() {
        PatrolType
                .stream()
                .forEach(this::buildPatrolGroups);
    }

    /**
     * Get the patrol group for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The patrol group corresponding to the given patrol type.
     */
    public PatrolGroup getPatrolGroup(final PatrolType patrolType) {
        return patrolMap.get(patrolType);
    }

    /**
     * Build an individual patrol group for the given type.
     *
     * @param patrolType The patrol type.
     */
    private void buildPatrolGroups(final PatrolType patrolType) {
        PatrolGroupData data = new PatrolGroupData();

        List<Squadron> squadronsOnPatrol = airbaseGroup
                .getAirbases()
                .stream()
                .flatMap(airbase -> airbase
                        .getPatrol(patrolType)
                        .getAssignedSquadrons()
                        .stream())
                .collect(Collectors.toList());

        data.setType(patrolType);
        data.setSquadrons(squadronsOnPatrol);
        data.setGroups(this);

        patrolMap.put(patrolType, patrolGroupDAO.load(data));
    }
}
