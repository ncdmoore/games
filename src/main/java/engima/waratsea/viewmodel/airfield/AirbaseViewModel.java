package engima.waratsea.viewmodel.airfield;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.Map;
import java.util.Set;

public interface AirbaseViewModel {
    boolean isReal();

    Airbase getAirbaseModel();

    ObjectProperty<Airbase> getAirbase();

    Set<Nation> getNations();

    Map<Nation, NationAirbaseViewModel> getNationViewModels();

    ListProperty<AirMissionViewModel> getTotalMissions();

    void addMission(Nation nation, AirMissionViewModel missionViewModel);

    void removeMission(Nation nation, AirMissionViewModel missionViewModel);

    PatrolGroup getPatrolGroup(PatrolType patrolType);

    SimpleListProperty<SquadronViewModel> getAssignedPatrolSquadrons(PatrolType type, Nation nation);

    SimpleListProperty<SquadronViewModel> getAvailablePatrolSquadrons(PatrolType type, Nation nation);

    BooleanProperty getAssignedPatrolExists(PatrolType type, Nation nation);

    BooleanProperty getAvailablePatrolExists(PatrolType type, Nation nation);

    void addToPatrol(PatrolType type, SquadronViewModel squadron);

    void removeFromPatrol(PatrolType type, SquadronViewModel squadron);

    Map<Nation, ListProperty<AirMissionViewModel>> getMissionViewModels();

    Map<PatrolType, PatrolViewModel> getPatrolViewModels();

    SquadronsViewModel getSquadronsViewModel();
}
