package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NavalTaskForceStrike implements AirMission {
    private final Game game;

    @Getter
    private final Nation nation;

    @Getter
    private final Airbase airbase;

    @Getter
    private final List<Squadron> squadrons;
    private final String targetName;               //The name of the target task force.
    private Target targetTaskForce;                //The actual target task force.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     */
    @Inject
    public NavalTaskForceStrike(@Assisted final MissionData data,
                                          final Game game) {
        this.game = game;
        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        // The squadrons can be created here as they are guaranteed to be already created by the air base.
        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(airbase::getSquadron)
                .collect(Collectors.toList());

        //Note, we cannot go ahead and obtain the target task force as it might not have been created at
        //this point in time. So we just save the name of the target task force. The target task force
        // must be determined outside the constructor.
        targetName = data.getTarget();
    }
    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    @Override
    public MissionData getData() {
        MissionData data = new MissionData();

        data.setType(MissionType.NAVAL_TASK_FORCE_STRIKE);
        data.setNation(nation);
        data.setTarget(targetName);

        List<String> names = Optional
                .ofNullable(squadrons)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        data.setSquadrons(names);

        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }

    /**
     * Get the mission's type.
     *
     * @return The type of mission.
     */
    @Override
    public MissionType getType() {
        return MissionType.getType(this);
    }

    /**
     * Get the mission's target.
     *
     * @return The mission's target.
     */
    @Override
    public Target getTarget() {
        return Optional.ofNullable(targetTaskForce)
                .orElseGet(this::getTargetTaskForce);
    }

    /**
     * Get the number of steps assigned to this mission.
     *
     * @return the total number of steps assigned to this mission.
     */
    @Override
    public int getSteps() {
        return 0;
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        squadrons.forEach(squadron -> {
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.ASSIGN_TO_MISSION);
            squadron.setSquadronState(state);
        });
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadrons.forEach(squadron -> {
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_MISSION);
            squadron.setSquadronState(state);
        });

        squadrons.clear();
    }

    /**
     * Get the number of squadron in the mission.
     *
     * @return The number of squadrons in the mission.
     */
    @Override
    public int getNumber() {
        return squadrons.size();
    }

    /**
     * Get the probability of success for this mission.
     *
     * @return The probability that this mission will be successful.
     */
    @Override
    public List<ProbabilityStats> getMissionProbability() {
        return null;
    }

    /**
     * Get the target task force.
     *
     * @return The target task force.
     */
    private Target getTargetTaskForce() {
        targetTaskForce = game
                .getPlayer(airbase.getSide())
                .getEnemyTaskForceTargets()
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetName))
                .findAny()
                .orElse(null);

        return targetTaskForce;
    }
}
