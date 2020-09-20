package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.victory.VictoryConditionDetails;
import engima.waratsea.model.victory.VictoryType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class VictoryViewModel {
    private final Game game;

    @Getter
    private final Map<VictoryType, ObjectProperty<ObservableList<VictoryConditionDetails>>> victoryConditions;

    @Getter
    private final Map<VictoryType, ObjectProperty<VictoryConditionDetails>> selectedConditions;

    @Getter
    private final Map<VictoryType, Map<String, StringProperty>> selectedConditionDetails;

    @Inject
    public VictoryViewModel(final Game game) {
        this.game = game;

        victoryConditions = Stream
                .of(VictoryType.values())
                .collect(Collectors.toMap(type -> type, this::buildList));

        selectedConditions = Stream
                .of(VictoryType.values())
                .collect(Collectors.toMap(type -> type, this::buildSelection));

        selectedConditionDetails = Stream
                .of(VictoryType.values())
                .collect(Collectors.toMap(type -> type, this::buildSelectedDetails));

        Stream
                .of(VictoryType.values())
                .forEach(this::bindSelected);
    }

    private ObjectProperty<ObservableList<VictoryConditionDetails>> buildList(final VictoryType victoryType) {
        List<VictoryConditionDetails> conditions = game
                .getHumanPlayer()
                .getVictoryConditions()
                .getDetails(victoryType);

        return new SimpleObjectProperty<>(FXCollections.observableArrayList(conditions));
    }

    private ObjectProperty<VictoryConditionDetails> buildSelection(final VictoryType victoryType) {
        return new SimpleObjectProperty<>();
    }

    private Map<String, StringProperty> buildSelectedDetails(final VictoryType victoryType) {
        // Get the first victory condition details of the given type.
        return game
                .getHumanPlayer()
                .getVictoryConditions()
                .getDetails(victoryType)
                .stream()
                .findFirst()
                .map(this::buildDetailsMap)
                .orElseGet(HashMap::new);
    }

    private void bindSelected(final VictoryType victoryType) {
        ObjectProperty<VictoryConditionDetails> selected = selectedConditions.get(victoryType);

        selectedConditionDetails
                .get(victoryType)
                .forEach((key, property) -> property.bind(Bindings.createStringBinding(() -> Optional
                        .ofNullable(selected.getValue())
                        .map(victoryConditionDetails -> victoryConditionDetails.getInfo().get(key))
                        .orElse(""), selected)));
    }


    private Map<String, StringProperty> buildDetailsMap(final VictoryConditionDetails victoryConditionDetails) {
        return victoryConditionDetails
                .getInfo()
                .keySet()
                .stream()
                .collect(Collectors.toMap(key -> key,           // The map's key.
                        key -> new SimpleStringProperty(),      // The map's value.
                        (oldValue, newValue) -> oldValue,       // How to handle key collisions. There should not be any.
                        LinkedHashMap::new));                   // Create a linked hash map.
    }
}
