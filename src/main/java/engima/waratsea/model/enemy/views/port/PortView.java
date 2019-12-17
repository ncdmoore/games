package engima.waratsea.model.enemy.views.port;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.enemy.views.port.data.PortViewData;
import lombok.Getter;
import lombok.Setter;

public class PortView implements PersistentData<PortViewData> {
    @Getter
    @Setter
    private Port enemyPort;

    /**
     * Constructor called by guice.
     *
     * @param data The port view data read in from JSON or created from mission data.
     */
    @Inject
    public PortView(@Assisted final PortViewData data) {
        this.enemyPort = data.getPort();
    }

    /**
     * Get the port's name.
     *
     * @return The port's name.
     */
    public String getName() {
        return enemyPort.getName();
    }

    /**
     * Get the port's location.
     *
     * @return The port's map reference: location.
     */
    public String getLocation() {
        return enemyPort.getReference();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public PortViewData getData() {
        PortViewData data = new PortViewData();
        data.setName(enemyPort.getName());
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }
}
