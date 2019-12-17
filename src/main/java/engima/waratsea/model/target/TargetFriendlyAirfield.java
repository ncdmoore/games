package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class TargetFriendlyAirfield implements Target {

    private final Game game;

    @Getter
    private final String name;

    private final Side side;

    //private int priority;

    private Airfield airfield;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param game The game.
     */
    @Inject
    public TargetFriendlyAirfield(@Assisted final TargetData data,
                                  final Game game) {
        this.game = game;

        name = data.getName();
        side = data.getSide();
    }

    /**
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getLocation() {
        return Optional
                .ofNullable(airfield)
                .orElseGet(this::getAirfield)
                .getReference();
    }

    /**
     * Get the target data that is persisted.
     *
     * @return The persistent target data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setName(name);
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
     * Get the airfield view for this target.
     *
     * @return This target's airfield view.
     */
    private Airfield getAirfield() {
        airfield = game.getPlayer(side)
                .getAirfieldMap()
                .get(name);

        if (airfield == null) {
            log.error("Cannot found airfield view: '{}'", name);
        }

        return airfield;
    }
}
