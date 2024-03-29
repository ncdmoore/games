package engima.waratsea.model.enemy.views.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.enemy.views.airfield.data.AirfieldViewData;
import engima.waratsea.model.map.GameGrid;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class AirfieldView implements PersistentData<AirfieldViewData> {
    @Getter
    @Setter
    private Airfield enemyAirfield;

    /**
     * Constructor called by guice.
     *
     * @param data The airfield view data read in from JSON or created from mission data.
     */
    @Inject
    public AirfieldView(@Assisted final AirfieldViewData data) {
        this.enemyAirfield = data.getAirfield();
    }

    /**
     * Get the airfield's name.
     *
     * @return The airfield's name.
     */
    public String getName() {
        return enemyAirfield.getName();
    }

    /**
     * Get the airfield's title.
     *
     * @return The airfield's title.
     */
    public String getTitle() {
        return enemyAirfield.getTitle();
    }

    /**
     * Get the airfield's location.
     *
     * @return The airfield's map reference: location.
     */
    public String getReference() {
        return enemyAirfield.getReference();
    }

    /**
     * Get the airfield's game grid.
     *
     * @return The airfield's game grid.
     */
    public Optional<GameGrid> getGrid() {
        return enemyAirfield.getGrid();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public AirfieldViewData getData() {
        AirfieldViewData data = new AirfieldViewData();
        data.setName(enemyAirfield.getName());
        return data;
    }
    
    /**
     * The airfield's CAP attempts to intercept and engage the attacking squadrons.
     *
     * @param squadrons The squadrons attacking the airfield.
     */
    public void capIntercept(final MissionSquadrons squadrons) {
        enemyAirfield.capIntercept(squadrons);
    }

    /**
     * The airfield fires its anti aircraft guns at the attacking squadrons.
     *
     * @param squadrons The squadrons attacking the airfield.
     */
    public void fireAntiAir(final MissionSquadrons squadrons) {
        enemyAirfield.fireAntiAir(squadrons);
    }
}
