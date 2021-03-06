package engima.waratsea.viewmodel.ship;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ResourceProvider;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ShipViewModel {
    @Getter private final ObjectProperty<Ship> ship = new SimpleObjectProperty<>();
    @Getter private final StringProperty fullTitle = new SimpleStringProperty();

    @Getter private final StringProperty title = new SimpleStringProperty();
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
    @Getter private final BooleanProperty noSquadrons = new SimpleBooleanProperty(false);

    @Getter private final ObjectProperty<Image> shipImage = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<Image> shipProfileImage = new SimpleObjectProperty<>();

    @Getter private final ListProperty<Squadron> squadrons = new SimpleListProperty<>(FXCollections.emptyObservableList());
    @Getter private final ListProperty<ComponentViewModel> components = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Inject
    public ShipViewModel(final ResourceProvider imageResourceProvider) {
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
        bindImages(imageResourceProvider);
        bindSquadrons();
        bindComponents();
    }

    /**
     * Set the ship for the ship view model.
     *
     * @param newShip The ship that this view model is bound to.
     * @return This ship view model.
     */
    public ShipViewModel setModel(final Ship newShip) {
        ship.setValue(newShip);
        return this;
    }

    /**
     * Get the ship's side.
     *
     * @return The ship's side.
     */
    public Side getSide() {
        return Optional.ofNullable(ship.getValue())
                .map(Ship::getSide)
                .orElseThrow();
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

    /**
     * Get the String representation of this class.
     *
     * @return The String representation of this class.
     */
    @Override
    public String toString() {
        return Optional.ofNullable(ship.getValue().getTitle()).orElse("");
    }

    private void bindDetails() {
        title.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(Ship::getTitle)
                .orElse(""), ship));

        fullTitle.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(s -> getPrefix(s) + s.getTitle())
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
                    .map(this::convertToAirbase)
                    .map(a -> convertToView(a.getSquadrons()))
                    .orElse(noSquadronMap());

            return FXCollections.observableMap(summary);
        };

        squadronSummary.bind(Bindings.createObjectBinding(bindingFunction, ship));
    }


    private void bindImages(final ResourceProvider imageResourceProvider) {
        shipImage.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(imageResourceProvider::getShipImage)
                .orElse(null), ship));

        shipProfileImage.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(ship.getValue())
                .map(imageResourceProvider::getShipProfileImage)
                .orElse(null), ship));
    }

    private void bindSquadrons() {
        Callable<ObservableList<Squadron>> bindingFunction = () -> {
            List<Squadron> shipsSquadrons = Optional
                    .ofNullable(ship.getValue())
                    .map(this::convertToAirbase)
                    .map(Airbase::getSquadrons)
                    .orElse(Collections.emptyList());

            return FXCollections.observableList(shipsSquadrons);
        };

        squadrons.bind(Bindings.createObjectBinding(bindingFunction, ship));

        noSquadrons.bind(squadrons.emptyProperty());
    }

    private void bindComponents() {
        Callable<ObservableList<ComponentViewModel>> bindingFunction = () -> {
            List<ComponentViewModel> shipComponents = Optional
                    .ofNullable(ship.getValue())
                    .map(Ship::getComponents)
                    .map(this::getComponentViewModels)
                    .orElse(Collections.emptyList());

            return FXCollections.observableList(shipComponents);
        };

        components.bind(Bindings.createObjectBinding(bindingFunction, ship));
    }

    private Map<String, String> convertToView(final List<Squadron> shipsSquadrons) {
        Map<String, Integer> tempMap = shipsSquadrons
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getType().toString() + ":",
                        s -> 1,
                        Integer::sum));

        Map<String, String> squadronMap = tempMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() + ""));

        if (squadronMap.isEmpty()) {
            squadronMap.put("No aircraft", "");
        }

        return squadronMap;
    }

    private Map<String, String> noSquadronMap() {
        return Map.of("No aircraft", "");
    }

    private List<ComponentViewModel> getComponentViewModels(final List<Component> componentList) {
        return componentList
                .stream()
                .map(ComponentViewModel::new)
                .collect(Collectors.toList());
    }

    private String getPrefix(final Ship aShip) {
        return aShip.getNation().getShipPrefix() + " ";
    }

    private Airbase convertToAirbase(final Ship aShip) {
        return aShip.isAirbase() ? (Airbase) aShip : null;
    }
}
