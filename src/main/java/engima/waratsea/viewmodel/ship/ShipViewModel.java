package engima.waratsea.viewmodel.ship;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static engima.waratsea.model.squadron.StepSize.ONE_THIRD;
import static engima.waratsea.model.squadron.StepSize.TWO_THIRDS;

public class ShipViewModel {
    @Getter
    private final ObjectProperty<Ship> ship = new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty prefixAndTitle = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty shipClass = new SimpleStringProperty();
    private final StringProperty nation = new SimpleStringProperty();
    private final StringProperty victory = new SimpleStringProperty();

    private final StringProperty primary = new SimpleStringProperty();
    private final StringProperty secondary = new SimpleStringProperty();
    private final StringProperty tertiary = new SimpleStringProperty();

    private final StringProperty antiAir = new SimpleStringProperty();
    private final StringProperty torpedoFactor = new SimpleStringProperty();
    private final StringProperty torpedoRounds = new SimpleStringProperty();

    private final StringProperty primaryArmour = new SimpleStringProperty();
    private final StringProperty secondaryArmour = new SimpleStringProperty();
    private final StringProperty tertiaryArmour = new SimpleStringProperty();
    private final StringProperty antiAirArmour = new SimpleStringProperty();
    private final StringProperty hullArmour = new SimpleStringProperty();
    private final StringProperty deckArmour = new SimpleStringProperty();

    private final StringProperty evenMovement = new SimpleStringProperty();
    private final StringProperty oddMovement = new SimpleStringProperty();
    private final StringProperty aswCapable = new SimpleStringProperty();
    private final StringProperty cargo = new SimpleStringProperty();
    private final StringProperty fuel = new SimpleStringProperty();

    private final ObjectProperty<ObservableMap<String, String>> squadronSummary = new SimpleObjectProperty<>(FXCollections.emptyObservableMap());


    @Inject
    public ShipViewModel() {
        bindDetails();
        bindSurface();
        bindAntiAir();
        bindTorpedo();
        bindArmour();
        bindMovement();
        bindAsw();
        bindFuel();
        bindCargo();
        bindSquadronCount();
    }

    /**
     * Set the ship for the ship view model.
     *
     * @param newShip The ship that this view model is bound to.
     */
    public void set(final Ship newShip) {
        ship.setValue(newShip);
    }

    /**
     * Get the ship's details data.
     *
     * @return A map of the ship's details data.
     */
    public Map<String, StringProperty> getShipDetailsData() {
        Map<String, StringProperty> details = new LinkedHashMap<>();
        details.put("Name:", title);
        details.put("Type:", type);
        details.put("Class:", shipClass);
        details.put("Nationality:", nation);
        details.put("Victory Points:", victory);
        details.put("", new SimpleStringProperty());
        return details;
    }

    /**
     * Get the ship's surface weapon data.
     *
     * @return A map of the ship's surface weapon data.
     */
    public Map<String, StringProperty> getSurfaceWeaponData() {
        Map<String, StringProperty> weapons = new LinkedHashMap<>();
        weapons.put("Primary:", primary);
        weapons.put("Secondary:", secondary);
        weapons.put("Tertiary:", tertiary);
        return weapons;
    }

    /**
     * Get the ship's anti air weapon data.
     *
     * @return A map of the ship's anti-air weapon data.
     */
    public Map<String, StringProperty> getAntiAirWeaponData() {
        Map<String, StringProperty> weapons = new LinkedHashMap<>();
        weapons.put("Anti Air:", antiAir);
        return weapons;
    }

    /**
     * Get the ship's torpedo data.
     *
     * @return A map of the ship's torpedo data.
     */
    public Map<String, List<StringProperty>> getTorpedoData() {
        Map<String, List<StringProperty>> weapons = new LinkedHashMap<>();
        StringProperty roundsTitle = new SimpleStringProperty("Rounds:");
        weapons.put("Torpedo:", List.of(torpedoFactor, roundsTitle, torpedoRounds));
        return weapons;
    }

    /**
     * Get the ship's armour data.
     *
     * @return A map of the armour type to armour value.
     */
    public Map<String, StringProperty> getArmourData() {
        Map<String, StringProperty> armour = new LinkedHashMap<>();
        armour.put("Primary:", primaryArmour);
        armour.put("Secondary:", secondaryArmour);
        armour.put("Tertiary:", tertiaryArmour);
        armour.put("Anti Air:", antiAirArmour);
        armour.put("Hull:", hullArmour);
        armour.put("Deck:", deckArmour);
        return armour;
    }

    /**
     * Get the ship's movement data.
     *
     * @return A map of the movement per turn type.
     */
    public Map<String, StringProperty> getMovementData() {
        Map<String, StringProperty> speed = new LinkedHashMap<>();
        speed.put("Even turns:", evenMovement);
        speed.put("Odd turns:", oddMovement);
        speed.put("", new SimpleStringProperty(""));
        return speed;
    }

    /**
     * Get the ship's ASW data.
     *
     * @return A map of the ASW data.
     */
    public Map<String, StringProperty> getAswData() {
        Map<String, StringProperty> asw = new LinkedHashMap<>();
        asw.put("ASW Capable:", aswCapable);
        return asw;
    }

    /**
     * Get the ship's fuel data.
     *
     * @return The ship's fuel data.
     */
    public Map<String, StringProperty> getFuelData() {
        Map<String, StringProperty> fuelData = new LinkedHashMap<>();
        fuelData.put("Remaining Fuel:", fuel);
        return fuelData;
    }

    /**
     * Get the ship's cargo data.
     *
     * @return The ship's cargo data.
     */
    public Map<String, StringProperty> getCargoData() {
        Map<String, StringProperty> cargoData = new LinkedHashMap<>();
        cargoData.put("Current Cargo:", cargo);
        return cargoData;
    }

    /**
     * Get the ship's squadron data.
     *
     * @return The ship's squadron data.
     */
    public Map<String, StringProperty> getSquadronSummary() {
        return squadronSummary
                .getValue()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new SimpleStringProperty(e.getValue())));
    }

    private void bindDetails() {
        title.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(Ship::getTitle)
                .orElse(""), ship));

        prefixAndTitle.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getNation().getShipPrefix() + " " + s.getTitle())
                .orElse(""), ship));

        prefixAndTitle.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> "title-pane-" + s.getSide().getPossessive().toLowerCase())
                .orElse(""), ship));

        name.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(Ship::getName)
                .orElse(""), ship));

        type.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getType().toString())
                .orElse(""), ship));

        shipClass.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(Ship::getShipClass)
                .orElse(""), ship));

        nation.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getNation().toString())
                .orElse(""), ship));

        victory.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getVictoryPoints() + "")
                .orElse(""), ship));
    }

    private void bindSurface() {
        primary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getPrimary().getHealth() + "")
                .orElse(""), ship));

        secondary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getSecondary().getHealth() + "")
                .orElse(""), ship));

        tertiary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getTertiary().getHealth() + "")
                .orElse(""), ship));
    }

    private void bindAntiAir() {
        antiAir.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getAntiAir().getHealth() + "")
                .orElse(""), ship));
    }

    private void bindTorpedo() {
        torpedoFactor.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getTorpedo().getHealth() + "")
                .orElse(""), ship));

        torpedoRounds.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getTorpedo().getNumber() + "")
                .orElse(""), ship));
    }

    private void bindArmour() {
        primaryArmour.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getPrimary().getArmour().toString())
                .orElse(""), ship));

        secondaryArmour.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getSecondary().getArmour().toString())
                .orElse(""), ship));

        tertiaryArmour.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getTertiary().getArmour().toString())
                .orElse(""), ship));

        antiAirArmour.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getAntiAir().getArmour().toString())
                .orElse(""), ship));

        hullArmour.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getHull().getArmour().toString())
                .orElse(""), ship));

        deckArmour.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getHull().isDeck() + "")
                .orElse(""), ship));
    }

    private void bindMovement() {
        evenMovement.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getMovement().getEven() + "")
                .orElse(""), ship));

        oddMovement.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getMovement().getOdd() + "")
                .orElse(""), ship));
    }

    private void bindAsw() {
        aswCapable.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getAsw().isAsw() + "")
                .orElse(""), ship));
    }

    private void bindFuel() {
        fuel.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getFuel().getHealth() + "")
                .orElse(""), ship));
    }

    private void bindCargo() {
        cargo.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> s.getCargo().getHealth() + "")
                .orElse(""), ship));
    }

    private void bindSquadronCount() {
        Callable<ObservableMap<String, String>> bindingFunction = () -> {
            Map<String, String> summary = Optional
                    .ofNullable(ship.getValue())
                    .map(s -> convertToView(s.getSquadronSummary()))
                    .orElse(noSquadronMap());

            return FXCollections.observableMap(summary);
        };

        squadronSummary.bind(Bindings.createObjectBinding(bindingFunction, ship));
    }


    private Map<String, String> convertToView(final Map<AircraftType, BigDecimal> inputMap) {
        Map<String, String> squadronMap = SquadronViewType
                .convertBigDecimal(inputMap)
                .entrySet()
                .stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toMap(e -> e.getKey().toString() + ":",
                        e -> formatSteps(e.getValue())));

        if (squadronMap.isEmpty()) {
            squadronMap.put("No aircraft", "");
        }

        return squadronMap;
    }

    private Map<String, String> noSquadronMap() {
        return Map.of("No aircraft", "");
    }

    /**
     * Format the aircraft type steps.
     *
     * @param steps The number of steps of a given aircraft type.
     * @return A string value that represents the total number of steps of the aircraft type.
     */
    private String formatSteps(final BigDecimal steps) {
        String stepString = steps + "";

        BigDecimal oneThird = new BigDecimal(ONE_THIRD);
        BigDecimal twoThirds = new BigDecimal(TWO_THIRDS);

        if (steps.compareTo(BigDecimal.ZERO) > 0 && steps.compareTo(oneThird) <= 0) {
            return "1/3 of a step";
        } else if (steps.compareTo(oneThird) > 0 && steps.compareTo(twoThirds) <= 0) {
            return "2/3 of a step";
        } else if (steps.compareTo(BigDecimal.ONE) == 0) {
            return stepString + " step";
        } else {
            return stepString + " steps";
        }
    }
}
