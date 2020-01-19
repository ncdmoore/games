package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SweepPort implements Mission {
    private final Game game;

    @Getter
    private final Nation nation;

    @Getter
    private final Airbase airbase;

    @Getter
    private final List<Squadron> squadrons;
    private final String targetBaseName;      //The name of the target port.
    private Target targetPort;                //The actual target port.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     */
    @Inject
    public SweepPort(@Assisted final MissionData data,
                               final Game game) {
        this.game = game;
        nation = data.getNation();

        airbase = data.getAirbase();

        // The squadrons can be created here as they are guaranteed to be already created by the air base.
        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(airbase::getSquadron)
                .collect(Collectors.toList());

        //Note, we cannot go ahead and obtain the target port as it might not have been created at
        //this point in time. So we just save the name of the target port. The target port
        // must be determined outside the constructor.
        targetBaseName = data.getTarget();
    }

    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    @Override
    public MissionData getData() {
        MissionData data = new MissionData();

        data.setType(MissionType.SWEEP_PORT);
        data.setNation(nation);
        data.setTarget(targetBaseName);

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
        return Optional.ofNullable(targetPort)
                .orElseGet(this::getTargetPort);
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

    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {

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
     * Get the target port.
     *
     * @return The target port.
     */
    private Target getTargetPort() {
        targetPort = game
                .getPlayer(airbase.getSide())
                .getEnemyPortTargets()
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetBaseName))
                .findAny()
                .orElse(null);

        return targetPort;
    }
}
