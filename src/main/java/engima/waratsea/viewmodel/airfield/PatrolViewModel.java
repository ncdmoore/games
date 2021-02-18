package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolDAO;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

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
    @Getter private final ListProperty<SquadronViewModel> assignedAllNations = new SimpleListProperty<>();   // All nation's squadrons on patrol. Written to model.

    @Getter private final Map<Nation, SimpleListProperty<SquadronViewModel>> available = new HashMap<>();
    @Getter private final Map<Nation, SimpleListProperty<SquadronViewModel>> assigned = new HashMap<>();

    @Getter private final Map<Nation, IntegerProperty> assignedCount = new HashMap<>();

    @Getter private final Map<Nation, BooleanProperty> availableExists = new HashMap<>();
    @Getter private final Map<Nation, BooleanProperty> assignedExists = new HashMap<>();

    @Getter private final BooleanProperty isAffectedByWeather = new SimpleBooleanProperty(false);

    @Getter private SquadronsViewModel squadrons;

    private Patrol patrol; // Used only to determine squadron configuration. This is not saved.

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
     * Set the squadrons view model. The squadrons view model is used to get individual squadron view models.
     *
     * @param squadronsViewModel The squadrons view model.
     * @return This patrol view model.
     */
    public PatrolViewModel setSquadrons(final SquadronsViewModel squadronsViewModel) {
        squadrons = squadronsViewModel;
        return this;
    }

    /**
     * Set the model. This initializes the patrol view model.
     *
     * @param patrolModel The patrol model.
     * @return This patrol view model.
     */
    public PatrolViewModel setModel(final Patrol patrolModel) {
        patrolType = patrolModel.getType();

        List<SquadronViewModel> squadronsOnPatrol = squadrons                    // Get the squadron view models
                .get(patrolModel.getAssignedSquadrons());                        // For the squadrons on patrol.

        patrol = buildPatrol(patrolModel);                                       // Make a copy of the patrol to update stats.
        airbase = patrolModel.getAirbase();

        Set<Nation> nations = patrolModel.getAirbase().getNations();

        nations.forEach(n -> setAssignedSquadrons(n, patrolModel));              // The assigned comes directory from the model.
        assignedAllNations.set(FXCollections.observableArrayList(squadronsOnPatrol));

        isAffectedByWeather.setValue(patrolModel.isAffectedByWeather());

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
            ListProperty<SquadronViewModel> allReady = viewModels.get(nation).getTotalReadySquadrons();
            available.get(nation).bind(Bindings.createObjectBinding(() -> FXCollections.observableArrayList(filter(allReady)), allReady));

            availableExists.put(nation, new SimpleBooleanProperty());
            availableExists.get(nation).bind(available.get(nation).emptyProperty());
        });
    }
    /**
     * Add the given squadron to the patrol.
     *
     * @param squadron The squadron that is added.
     */
    public void addToPatrol(final SquadronViewModel squadron) {
        Nation nation = squadron.getNation();
        assigned.get(nation).get().add(squadron);
        squadron.setOnPatrol();
        assignedAllNations.add(squadron);
    }

    /**
     * Remove the given squadron from the patrol.
     *
     * @param squadron The squadron that is removed.
     */
    public void removeFromPatrol(final SquadronViewModel squadron) {
        Nation nation = squadron.getNation();
        assigned.get(nation).get().remove(squadron);
        squadron.setOffPatrol();
        assignedAllNations.remove(squadron);
    }

    /**
     * Save the patrol to the model.
     */
    public void savePatrol() {
        List<Squadron> squadronsOnPatrol = assignedAllNations
                .getValue()
                .stream()
                .map(s -> s.getSquadron().getValue())
                .collect(Collectors.toList());

        airbase.updatePatrol(patrolType, squadronsOnPatrol);
    }

    /**
     * Determine the best squadron configuration for the patrol.
     *
     * @return The best squadron configuration for the patrol.
     */
    public SquadronConfig determineSquadronConfig() {
        return patrol.getBestSquadronConfig();
    }

    /**
     * Get a Patrol from the given model. This is a temporary patrol object used for
     * keeping the patrol status updated in the view. If we don't make a copy then we
     * end up updating the patrol in the model which is not a good idea as the update
     * may be cancelled by the user.
     *
     * @param patrolModel The patrol model.
     * @return A newly created patrol.
     */
    private Patrol buildPatrol(final Patrol patrolModel) {
        PatrolData data = new PatrolData();
        data.setType(patrolModel.getType());
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
     * Set the squadrons from the patrol model for the given nation.
     *
     * @param patrolModel The patrol model.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void setAssignedSquadrons(final Nation nation, final Patrol patrolModel) {
        List<SquadronViewModel> assignedToPatrol = squadrons.get(patrolModel.getAssignedSquadrons(nation));

        assigned.put(nation, new SimpleListProperty<>(FXCollections.observableArrayList(assignedToPatrol)));
        assignedExists.put(nation, new SimpleBooleanProperty(assignedToPatrol.isEmpty()));
        assignedExists.get(nation).bind(assigned.get(nation).emptyProperty());

        assignedCount.put(nation, new SimpleIntegerProperty(assignedToPatrol.size()));
        assignedCount.get(nation).bind(assigned.get(nation).sizeProperty());
    }

    /**
     * Return a list of squadrons from the given list of squadrons that are allowed to perform this patrol.
     *
     * @param allReady All ready squadrons at this airbase.
     * @return A list of ready squadrons that are allowed to do this patrol.
     */
    private List<SquadronViewModel> filter(final ListProperty<SquadronViewModel> allReady) {
        return allReady
                .getValue()
                .stream()
                .filter(squadron -> squadron.canDoPatrol(patrolType))
                .collect(Collectors.toList());
    }
}
