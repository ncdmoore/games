package engima.waratsea.model.enemy.views.taskForce.data;

import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;

public class TaskForceViewData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private transient TaskForce taskForce;
}
