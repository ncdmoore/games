package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolDAO;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The patrol view of a given airbase for a particular type of patrol.
 *
 * The patrol view is across all nations that can use the airbase.
 * This is because all nations squadrons count towards the effectiveness of the patrol.
 */
public class PatrolViewModel {
    @Getter private final SimpleListProperty<Squadron> assignedAllNations = new SimpleListProperty<>();   // All nation's squadrons on patrol. Needed for stats.

    @Getter private final Map<Nation, SimpleListProperty<Squadron>> available = new HashMap<>();
    @Getter private final Map<Nation, SimpleListProperty<Squadron>> assigned = new HashMap<>();

    @Getter private final Map<Nation, IntegerProperty> assignedCount = new HashMap<>();

    @Getter private final Map<Nation, BooleanProperty> availableExists = new HashMap<>();
    @Getter private final Map<Nation, BooleanProperty> assignedExists = new HashMap<>();

    @Getter private Patrol patrol;

    private PatrolType patrolType;
    private Airbase airbase;

    private final PatrolDAO patrolDAO;

    /**
     * Constructor called by guice.
     *
     * @param patrolDAO The patrol data access object. Needed for patrol statistics.
     */
    @Inject
    public PatrolViewModel(final PatrolDAO patrolDAO) {
        this.patrolDAO = patrolDAO;
    }

    /**
     * Set the model. This initializes the patrol view model.
     *
     * @param patrolModel The patrol.
     * @return This patrol view model.
     */
    public PatrolViewModel setModel(final Patrol patrolModel) {
        patrol = buildPatrol(patrolModel);

        patrolType = patrolModel.getType();
        airbase = patrolModel.getAirbase();

        Set<Nation> nations = patrolModel.getAirbase().getNations();

        nations.forEach(this::setAssignedSquadrons);
        assignedAllNations.set(FXCollections.observableArrayList(patrolModel.getAssignedSquadrons()));

        return this;
    }

    /**
     * Set the nation view models. This allows the patrols access to the airbase ready squadrons.
     * The available squadrons for a patrol are bound to the available squadrons of the nation's
     * view of the airbase. These available squadrons must be filtered on their ability to perform
     * this patrol.
     *
     * @param viewModels The nation's airbase view model.
     */
    public void setNationViewModels(final Map<Nation, NationAirbaseViewModel> viewModels) {

        Set<Nation> nations = viewModels.keySet();

        nations.forEach(nation -> {
            available.put(nation, new SimpleListProperty<>());
            SimpleListProperty<Squadron> allReady = viewModels.get(nation).getTotalReadySquadrons();
            available.get(nation).bind(Bindings.createObjectBinding(() -> FXCollections.observableArrayList(filter(allReady)), allReady));

            availableExists.put(nation, new SimpleBooleanProperty());
            availableExists.get(nation).bind(Bindings.createBooleanBinding(() -> available.get(nation).getValue().isEmpty(), available.get(nation)));
        });
    }
    /**
     * Add the given squadron to the patrol.
     *
     * @param squadron The squadron that is added.
     */
    public void addToPatrol(final Squadron squadron) {
        Nation nation = squadron.getNation();
        assigned.get(nation).get().add(squadron);

        // Must create a new list to the change listeners fire.
        List<Squadron> all = new ArrayList<>(assignedAllNations.get());
        all.add(squadron);

        // Must updates the status before assigned all nations is updated so listeners get the correct patrol info.
        updatePatrolStats(all);

        assignedAllNations.set(FXCollections.observableArrayList(all));
    }

    /**
     * Remove the given squadron from the patrol.
     *
     * @param squadron The squadron that is removed.
     */
    public void removeFromPatrol(final Squadron squadron) {
        Nation nation = squadron.getNation();
        assigned.get(nation).get().remove(squadron);

        // Must create a new list to the change listeners fire.
        List<Squadron> all = new ArrayList<>(assignedAllNations.get());
        all.remove(squadron);

        // Must updates the status before assigned all nations is updated so listeners get the correct patrol info.
        updatePatrolStats(all);

        assignedAllNations.set(FXCollections.observableArrayList(all));
    }

    /**
     * Determine if the given squadron is on this patrol.
     *
     * @param squadron A squadron that is checked to see if it is on this patrol.
     * @return True if the given squadron is on this patrol. False otherwise.
     */
    public boolean isSquadronOnPatrol(final Squadron squadron) {
        Nation nation = squadron.getNation();

        return assigned.get(nation).getValue().contains(squadron);
    }

    /**
     * Update the patrol stats. Build a new patrol with the given squadrons on patrol.
     *
     * @param squadronsOnPatrol The squadrons on patrol.
     */
    private void updatePatrolStats(final List<Squadron> squadronsOnPatrol) {
        patrol = buildPatrol(squadronsOnPatrol);
    }

    /**
     * Get a Patrol from the given model.
     *
     * @param patrolModel The patrol model.
     * @return A newly created patrol.
     */
    private Patrol buildPatrol(final Patrol patrolModel) {
        PatrolData data = new PatrolData();
        data.setType(PatrolType.getType(patrolModel));
        data.setAirbase(patrolModel.getAirbase());

        List<String> squadronNames = patrolModel
                .getAssignedSquadrons()
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        data.setSquadrons(squadronNames);

        return patrolDAO.load(data);
    }

    /**
     * Build a patrol model.
     *
     * @param squadronsOnPatrol The squadrons that make up the patrol.
     * @return A patrol.
     */
    private Patrol buildPatrol(final List<Squadron> squadronsOnPatrol) {
        PatrolData data = new PatrolData();
        data.setType(patrolType);
        data.setAirbase(airbase);

        List<String> squadronNames = squadronsOnPatrol
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        data.setSquadrons(squadronNames);

        return patrolDAO.load(data);
    }

    /**
     * Set the squadrons from the patrol model for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void setAssignedSquadrons(final Nation nation) {
        List<Squadron> assignedToPatrol = patrol.getAssignedSquadrons(nation);

        assigned.put(nation, new SimpleListProperty<>(FXCollections.observableArrayList(assignedToPatrol)));
        assignedExists.put(nation, new SimpleBooleanProperty(assignedToPatrol.isEmpty()));
        assignedExists.get(nation).bind(Bindings.createBooleanBinding(() -> assigned.get(nation).getValue().isEmpty(), assigned.get(nation)));

        assignedCount.put(nation, new SimpleIntegerProperty(assignedToPatrol.size()));
        assignedCount.get(nation).bind(assigned.get(nation).sizeProperty());
    }

    /**
     * Return a list of squadrons from the given list of squadrons that are allowed to perform this patrol.
     *
     * @param allReady All ready squadrons at this airbase.
     * @return A list of ready squadrons that are allowed to do this patrol.
     */
    private List<Squadron> filter(final SimpleListProperty<Squadron> allReady) {
        return allReady
                .getValue()
                .stream()
                .filter(squadron -> squadron.canDoPatrol(patrolType))
                .collect(Collectors.toList());
    }
}
