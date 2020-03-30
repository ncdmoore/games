package engima.waratsea.presenter.airfield.mission;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronDAO;
import engima.waratsea.model.target.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is used by air missions to indicate the chances of success of the mission.
 *
 * A temporary mission with copies of squadrons currently assigned to a mission is created.
 * This temporary mission with squadron copies is used to determine the probability of success.
 *
 * We have to make a temporary mission with copies of squadrons as the mission may not have
 * been created. The squadrons are definitely not assigned at this point. Thus, we cannot use
 * the squadron's current configuration. We have to copy the squadrons and configure them based
 * on the target, mission type and mission roles.
 *
 * Once the probability of success is determined, the mission and the squadron copies are no
 * longer needed.
 */
public class SuccessStats {
    private AirMissionType missionType;
    private Nation nation;
    private Airbase airbase;
    private List<Squadron> squadrons;
    private Target target;
    private MissionDAO missionDAO;
    private SquadronDAO squadronDAO;

    /**
     * Set the mission type.
     *
     * @param type The mission type.
     * @return This success stats.
     */
    public SuccessStats setMissionType(final AirMissionType type) {
        missionType = type;
        return this;
    }

    /**
     * Set the nation.
     *
     * @param missionNation The nation.
     * @return This success stats.
     */
    public SuccessStats setNation(final Nation missionNation) {
        nation = missionNation;
        return this;
    }

    /**
     * Set the airbase.
     *
     * @param base The airbase.
     * @return This success stats.
     */
    public SuccessStats setAirbase(final Airbase base) {
        airbase = base;
        return this;
    }

    /**
     * Set the mission target.
     *
     * @param missionTarget The mission target.
     * @return This success stats.
     */
    public SuccessStats setTarget(final Target missionTarget) {
        target = missionTarget;
        return  this;
    }

    /**
     * Set the mission DAO.
     *
     * @param dao The mission data access object.
     * @return This success stats.
     */
    public SuccessStats setMissionDAO(final MissionDAO dao) {
        missionDAO = dao;
        return this;
    }

    /**
     * Set the squadron DAO.
     *
     * @param dao The squadron data access object.
     * @return This success stats.
     */
    public SuccessStats setSquadronDAO(final SquadronDAO dao) {
        squadronDAO = dao;
        return this;
    }

    /**
     * Set the mission squadrons.
     *
     * @param squadronList The squadrons.
     * @return This success stats.
     */
    public SuccessStats setSquadrons(final List<Squadron> squadronList) {
        squadrons = squadronList;
        return this;
    }

    /**
     * Get the mission probability of success.
     *
     * @return A list of mission success probabilities.
     */
    public List<ProbabilityStats> get() {
        // The squadrons must be copied so that they can be configured so that the
        // mission probability can be calculated correctly.
        List<Squadron> copies = squadrons
                .stream()
                .map(Squadron::getData)
                .map(squadronDAO::build)
                .map(this::setConfig)
                .collect(Collectors.toList());

        AirMission airMission = getTemporaryMission(copies);
        return airMission.getMissionProbability();
    }

    /**
     * Set the squadron's configuration.
     *
     * @param squadron The squadron for which the configuration is set.
     * @return The squadron with the configuration set.
     */
    private Squadron setConfig(final Squadron squadron) {
        squadron.setHome(airbase);
        squadron.equip(target, missionType, MissionRole.MAIN);
        return squadron;
    }

    /**
     * Get a temporary mission in order to get the stats for the mission.
     *
     * @param squadronList The squadron that make up the temporary mission.
     * @return The temporary mission.
     */
    private AirMission getTemporaryMission(final List<Squadron> squadronList) {
        MissionData data = new MissionData();
        data.setType(missionType);
        data.setAirbase(airbase);
        data.setNation(nation);
        data.setTarget(target.getName());

        Map<MissionRole, List<String>> tempMap = new HashMap<>();

        tempMap.put(MissionRole.MAIN, squadronList
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList()));

        data.setSquadronMap(tempMap);

        return missionDAO.load(data);
    }
}
