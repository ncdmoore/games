package engima.waratsea.model.taskForce.patrol.data;

import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PatrolGroupData {
    @Getter @Setter private transient PatrolGroups groups;
    @Getter @Setter private transient PatrolType type;
    @Getter @Setter private transient TaskForce taskForce;
    @Getter @Setter private transient List<Squadron> squadrons;
}
