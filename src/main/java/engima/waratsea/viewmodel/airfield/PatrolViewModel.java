package engima.waratsea.viewmodel.airfield;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.Map;

public interface PatrolViewModel {
    void setNationViewModels(Map<Nation, NationAirbaseViewModel> viewModels);

    Map<Nation, SimpleListProperty<SquadronViewModel>> getAvailable();
    Map<Nation, SimpleListProperty<SquadronViewModel>> getAssigned();

    Map<Nation, BooleanProperty> getAvailableExists();
    Map<Nation, BooleanProperty> getAssignedExists();

    ListProperty<SquadronViewModel> getAssignedAllNations();

    Map<Nation, IntegerProperty> getAssignedCount();

    void addToPatrol(SquadronViewModel squadron);
    void removeFromPatrol(SquadronViewModel squadron);

    BooleanProperty getIsAffectedByWeather();

    SquadronConfig determineSquadronConfig();


    void save();
}
