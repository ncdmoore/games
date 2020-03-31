package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.data.MissionsData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class Missions {
    private final MissionDAO missionDAO;

    private Airbase airbase;

    @Getter private List<AirMission> missions;

    /**
     * Constructor called by guice.
     *
     * @param missionDAO The mission data access object.
     */
    @Inject
    public Missions(final MissionDAO missionDAO) {
        this.missionDAO = missionDAO;
    }

    /**
     * Get the missions' persistent data.
     *
     * @return The missions' persistent data.
     */
    public MissionsData getData() {
        MissionsData data = new MissionsData();

        List<MissionData> missionData = missions
                .stream()
                .map(AirMission::getData)
                .collect(Collectors.toList());

        data.setMissions(missionData);

        return data;
    }

    /**
     * Build the missions.
     *
     * @param base The missions' airbase.
     * @param data The missions' data.
     */
    public void build(final Airbase base, final MissionsData data) {
        airbase = base;
        missions = Optional.ofNullable(data)
                .map(MissionsData::getMissions)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(missionData -> missionData.setAirbase(airbase))
                .map(missionDAO::load)
                .collect(Collectors.toList());
    }

    /**
     * Get the airfield's missions for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of missions for the given nation.
     */
    public List<AirMission> getMissions(final Nation nation) {
        return missions
                .stream()
                .filter(mission -> mission.getNation() == nation)
                .collect(Collectors.toList());
    }

    /**
     * Add a mission to this air base.
     *
     * @param mission The mission that is added to this airbase.
     */
    public void addMission(final AirMission mission) {
        missions.add(mission);
        mission.addSquadrons();
    }

    /**
     * Get the total number of squadron steps on a mission of the given type
     * that are assigned to the given target. This is the total number of squadron steps
     * from all missions of the same type that have the given target as their target.
     *
     *  @param target The ferry mission destination.
     * @return The total number of steps being ferried to the given target.
     */
    public int getTotalMissionSteps(final Target target) {
        int steps =  missions
                .stream()
                .filter(mission -> mission.getTarget().isEqual(target))
                .map(AirMission::getSteps)
                .reduce(0, Integer::sum);

        log.debug("Airfield {} target {} steps {}", new Object[]{airbase.getTitle(), target.getName(), steps});
        return steps;
    }

    /**
     * Clear all squadrons from the missions.
     */
    public void clear() {
        missions.forEach(AirMission::removeSquadrons);
        missions.clear();
    }

    /**
     * Clear all squadrons from the missions for a given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    public void clear(final Nation nation) {
        List<AirMission> toRemove = missions
                .stream()
                .filter(mission -> mission.getNation() == nation)
                .collect(Collectors.toList());

        toRemove.forEach(AirMission::removeSquadrons);

        missions.removeAll(toRemove);
    }
}
