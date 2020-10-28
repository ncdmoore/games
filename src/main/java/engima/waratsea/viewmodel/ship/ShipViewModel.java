package engima.waratsea.viewmodel.ship;

import com.google.inject.Inject;
import engima.waratsea.model.ship.Ship;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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


    @Inject
    public ShipViewModel() {
        bindDetails();
        bindSurface();
        bindAntiAir();
        bindTorpedo();
        bindArmour();
        bindMovement();
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
}
