package engima.waratsea.model.taskForce.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetDAO;
import engima.waratsea.model.taskForce.mission.data.MissionData;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Bombardment implements Mission {

    @Getter
    private final MissionType type;

    @Getter
    private final List<Target> targets;

    /**
     * The constructor called by guice.
     *
     * @param data read in from a JSON file.
     * @param targetDAO The target data access object.
     */
    @Inject
    public Bombardment(@Assisted final MissionData data,
                                 final TargetDAO targetDAO) {

        type = data.getType();

        targets = Optional.ofNullable(data.getTargets())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(targetData -> targetData.setSide(data.getSide()))
                .map(targetDAO::load)
                .collect(Collectors.toList());
    }

    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    @Override
    public MissionData getData() {
        MissionData data = new MissionData();
        data.setType(type);
        data.setTargets(PersistentUtility.getData(targets));
        return data;
    }
}
