package engima.waratsea.model.taskForce.patrol.data;

import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import lombok.Data;

import java.util.List;

@Data
public class PatrolGroupData {
    private transient PatrolGroups groups;
    private transient PatrolType type;
    private transient TaskForce taskForce;
    private transient List<Squadron> squadrons;
}
