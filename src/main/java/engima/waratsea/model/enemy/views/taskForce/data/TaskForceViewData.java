package engima.waratsea.model.enemy.views.taskForce.data;

import engima.waratsea.model.taskForce.TaskForce;
import lombok.Data;

@Data
public class TaskForceViewData {
    private String name;
    private transient TaskForce taskForce;
}
