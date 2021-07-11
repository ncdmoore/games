package engima.waratsea.model.enemy.views.airfield;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.BaseId;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.enemy.views.airfield.data.AirfieldViewData;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class AirfieldViewDAO {

    private final Resource resource;
    private final AirfieldViewFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param resource The game resource configuration.
     * @param factory The airfield view factory.
     */
    @Inject
    public AirfieldViewDAO(final Resource resource,
                           final AirfieldViewFactory factory) {
        this.resource = resource;
        this.factory = factory;
    }

    /**
     * Load the airfield view.
     *
     * @param enemyAirfields A list of enemy airfields.
     * @return A list of enemy airfields views.
     */
    public List<AirfieldView> load(final List<Airfield> enemyAirfields) {
        return  resource.isNew() ? getNew(enemyAirfields) : getExisting(enemyAirfields);
    }

    /**
     * Save the airfield views. The allies and axis airfield view data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param airfieldViews The airfield view data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<AirfieldView> airfieldViews) {
        log.debug("Saving airfield views, scenario: '{}',side {}", scenario.getTitle(), side);
        airfieldViews.forEach(airfieldView -> {
            String fileName = resource.getSavedFileName(side, AirfieldView.class, airfieldView.getName() + ".json");
            PersistentUtility.save(fileName, airfieldView);
            log.debug("Saving Airfield View: '{}'", airfieldView.getName());
        });
    }

    /**
     * Get the enemy airfield views for a new game.
     *
     * @param enemyAirfields A list of enemy airfields.
     * @return A list of enemy airfield views.
     */
    private List<AirfieldView> getNew(final List<Airfield> enemyAirfields) {
        return enemyAirfields
                .stream()
                .map(this::createAirfieldData)
                .map(this::createAirfieldView)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy airfield views for an existing game.
     *
     * @param enemyAirfields A list of enemy airfield.
     * @return A list of enemy airfield views.
     */
    private List<AirfieldView> getExisting(final List<Airfield> enemyAirfields) {
        return enemyAirfields
                .stream()
                .map(this::readAirfieldView)
                .filter(Objects::nonNull)
                .map(this::createAirfieldView)
                .collect(Collectors.toList());
    }

    /**
     * Create the airfield data. This is only valid for new games where little is known about the
     * enemy airfield.
     *
     * @param airfield The enemy airfield.
     * @return The enemy airfield view data.
     */
    private AirfieldViewData createAirfieldData(final Airfield airfield) {
        AirfieldViewData data = new AirfieldViewData();
        data.setName(airfield.getName());
        data.setAirfield(airfield);
        return data;
    }

    /**
     * Create the airfield view from the airfield view data.
     *
     * @param data The airfield view data.
     * @return The airfield view of an enemy airfield.
     */
    private AirfieldView createAirfieldView(final AirfieldViewData data) {
        return factory.create(data);
    }

    /**
     * Read the airfield view data.
     *
     * @param airfield The enemy airfield.
     * @return The airfield view data read in from a JSON file.
     */
    private AirfieldViewData readAirfieldView(final Airfield airfield) {

        BaseId airfieldId = new BaseId(airfield.getName(), airfield.getSide());

        return getURL(airfieldId)
                .map(url -> readAirfield(url, airfieldId))
                .map(data -> addAirfield(data, airfield))
                .orElseGet(() -> logError(airfieldId));
    }

    /**
     * Get the URL of the enemy airfield view.
     *
     * @param airfieldId The airfield Id.
     * @return An optional URL corresponding to the specified enemy airfield Id.
     */
    private Optional<URL> getURL(final BaseId airfieldId) {
        Side side = airfieldId.getSide();
        String airfieldName = airfieldId.getName();
        return resource.getSavedURL(side.opposite(), AirfieldView.class, airfieldName + ".json");
    }

    /**
     * Read the airfield data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param airfieldId Uniquely identifies a airfield.
     * @return The data read from the JSON file.
     */
    private AirfieldViewData readAirfield(final URL url, final BaseId airfieldId) {
        String airfieldName = airfieldId.getName();
        Side side = airfieldId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            Gson gson = new Gson();
            AirfieldViewData airfieldViewData = gson.fromJson(br, AirfieldViewData.class);

            log.debug("load enemy airfield view {} for side {}", airfieldName, side);

            return airfieldViewData;
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load enemy airfield view {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Add the airfield to the airfield data read in form the JSON file.
     *
     * @param data The airfield view data read in from the JSON file.
     * @param airfield The airfield that is added.
     * @return The modified airfield view data.
     */
    private AirfieldViewData addAirfield(final AirfieldViewData data, final Airfield airfield) {
        data.setAirfield(airfield);
        return data;
    }

    /**
     * Log an error for airfields that cannot be loaded.
     *
     * @param airfieldId The airfield Id that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private AirfieldViewData logError(final BaseId airfieldId) {
        log.error("Unable to load enemy airfield view '{}'", airfieldId.getName());
        return null;
    }
}
