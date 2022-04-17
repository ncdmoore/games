package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.viewmodel.DeploymentViewModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Each nation has its on unique airfield view model. Thus, all the property values of this view model are for
 * a given nation. This is just used in the preview screens.
 */
public class AirfieldViewModel {
    @Getter private final StringProperty maxCapacity = new SimpleStringProperty();
    @Getter private final StringProperty current = new SimpleStringProperty();
    @Getter private final StringProperty antiAir = new SimpleStringProperty();

    @Getter private final ListProperty<Squadron> airfieldSquadrons = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final ListProperty<Squadron> availableSquadrons = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private final StringProperty availableSquadronsTitle = new SimpleStringProperty();

    @Getter private final Map<AircraftType, StringProperty> airfieldSteps = new HashMap<>();

    private final Game game;
    private final DeploymentViewModel deploymentViewModel;
    private Airfield airfield;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param deploymentViewModel The squadron deployment view model.
     */
    @Inject
    public AirfieldViewModel(final Game game,
                             final DeploymentViewModel deploymentViewModel) {
        this.game = game;
        this.deploymentViewModel = deploymentViewModel;

        Stream
                .of(AircraftType.values())
                .forEach(type -> airfieldSteps.put(type, new SimpleStringProperty()));
    }

    /**
     * Set the model.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param field The airfield.
     */
    public void setModel(final Nation nation, final Airfield field) {
        airfield = field;

        maxCapacity.set(airfield.getMaxCapacity() + "");
        current.set(airfield.getCurrentSteps() + "");
        antiAir.set(airfield.getAntiAirRating() + "");

        airfieldSquadrons.set(FXCollections.observableArrayList(airfield.getSquadrons()));

        availableSquadronsTitle.set(airfield.getTitle() + " Squadrons:");

        availableSquadrons.set(FXCollections.observableArrayList(getAvailableSquadrons(nation)));

        setSteps();
    }

    /**
     * Deploy the given squadron to the airfield.
     *
     * @param squadron The deployed squadron.
     * @return The result of attempting to deploy the given squadron.
     */
    public AirfieldOperation deploy(final Squadron squadron) {
        AirfieldOperation result = airfield.addSquadron(squadron);
        refresh(squadron.getNation());
        return result;
    }

    /**
     * Remove or un-deploy the given squadron from the airfield.
     *
     * @param squadron The removed squadron.
     */
    public void remove(final Squadron squadron) {
        airfield.removeSquadron(squadron);
        refresh(squadron.getNation());
    }

    /**
     * Refresh the bindings that change.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void refresh(final Nation nation) {
        current.set(airfield.getCurrentSteps() + "");

        airfieldSquadrons.set(FXCollections.observableArrayList(airfield.getSquadrons()));
        availableSquadrons.set(FXCollections.observableArrayList(getAvailableSquadrons(nation)));

        setSteps();

        deploymentViewModel.setModel();
    }

    /**
     * Get the available squadrons for each nation of the human player.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A map of nation to available squadrons for the given airfield.
     */
    private List<Squadron> getAvailableSquadrons(final Nation nation) {
         return game
                 .getHumanPlayer()
                 .getSquadrons(nation, SquadronLocationType.LAND)
                 .stream()
                 .filter(Squadron::isAvailable)
                 .filter(airfield::canSquadronLand)
                 .collect(Collectors.toList());
    }

    /**
     * Get the airfield aircraft base step map.
     *
     */
    private void setSteps() {
         airfieldSteps.forEach((type, prop) -> prop.set(airfield.getStepsForType(type) + ""));
    }
}
