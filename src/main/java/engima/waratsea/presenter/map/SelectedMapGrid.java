package engima.waratsea.presenter.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
public class SelectedMapGrid {
    private final ObjectProperty<GameGrid> grid = new SimpleObjectProperty<>();

    @Getter private final StringProperty mapReference = new SimpleStringProperty();
    @Getter private final StringProperty locationName = new SimpleStringProperty();
    @Getter private final StringProperty type = new SimpleStringProperty();
    @Getter private final BooleanProperty isAirfield = new SimpleBooleanProperty();
    @Getter private final BooleanProperty isPort = new SimpleBooleanProperty();

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game's map.
     */
    @Inject
    public SelectedMapGrid(final GameMap gameMap) {
        mapReference.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(grid.getValue())
                .map(GameGrid::getMapReference)
                .orElse(""), grid));

        locationName.bind(Bindings.createStringBinding(() -> {
            if (StringUtils.isBlank(mapReference.getValue())) {
                return "";
            }
            String name = gameMap.convertReferenceToName(mapReference.getValue());
            name = name.equalsIgnoreCase(mapReference.getValue()) ? "--" : name;
            return name;
        }, mapReference));

        type.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(grid.getValue())
                .map(g -> g.getType().toString())
                .orElse(""), grid));

        isAirfield.bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(grid.getValue())
                .map(gameMap::isLocationAirbase)
                .orElse(false), grid));

        isPort.bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(grid.getValue())
                .map(gameMap::isLocationPort)
                .orElse(false), grid));

    }

    /**
     * Set the value of the selected game grid.
     *
     * @param selectedGameGrid The selected game grid.
     */
    public void set(final GameGrid selectedGameGrid) {
        grid.setValue(selectedGameGrid);
    }
}
