package engima.waratsea.model.enemy.views.port;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.BaseId;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.enemy.views.port.data.PortViewData;
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
public class PortViewDAO  {

    private final Resource resource;
    private final PortViewFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param resource The game resource configuration.
     * @param factory The port view factory.
     */
    @Inject
    public PortViewDAO(final Resource resource,
                       final PortViewFactory factory) {
        this.resource = resource;
        this.factory = factory;
    }

    /**
     * Load the port view.
     *
     * @param enemyPorts A list of enemy ports.
     * @return A list of enemy port views.
     */
    public List<PortView> load(final List<Port> enemyPorts) {
        return  resource.isNew() ? getNew(enemyPorts) : getExisting(enemyPorts);
    }

    /**
     * Save the port views. The allies and axis port view data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param portViews The port view data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<PortView> portViews) {
        log.info("Saving port views, scenario: '{}',side {}", scenario.getTitle(), side);
        portViews.forEach(portView -> {
            String fileName = resource.getSavedFileName(side, PortView.class, portView.getName() + ".json");
            PersistentUtility.save(fileName, portView);
            log.debug("Saving Port View: '{}'", portView.getName());
        });
    }

    /**
     * Get the enemy port views for a new game.
     *
     * @param enemyPorts A list of enemy ports.
     * @return A list of enemy port views.
     */
    private List<PortView> getNew(final List<Port> enemyPorts) {
        return enemyPorts
                .stream()
                .map(this::createPortData)
                .map(this::createPortView)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy port views for an existing game.
     *
     * @param enemyPorts A list of enemy ports.
     * @return A list of enemy port views.
     */
    private List<PortView> getExisting(final List<Port> enemyPorts) {
        return enemyPorts
                .stream()
                .map(this::readPortView)
                .filter(Objects::nonNull)
                .map(this::createPortView)
                .collect(Collectors.toList());
    }

    /**
     * Create the port data. This is only valid for new games where little is known about the
     * enemy port.
     *
     * @param port The enemy port.
     * @return The enemy port view data.
     */
    private PortViewData createPortData(final Port port) {
        PortViewData data = new PortViewData();
        data.setName(port.getName());
        data.setPort(port);
        return data;
    }

    /**
     * Create the port view from the port view data.
     *
     * @param data The port view data.
     * @return The port view of an enemy port.
     */
    private PortView createPortView(final PortViewData data) {
        return factory.create(data);
    }

    /**
     * REad the port view data.
     *
     * @param port The enemy port.
     * @return The port view data read in from a JSON file.
     */
    private PortViewData readPortView(final Port port) {

        BaseId portId = new BaseId(port.getName(), port.getSide());

        return getURL(portId)
                .map(url -> readPort(url, portId))
                .map(data -> addPort(data, port))
                .orElseGet(() -> logError(portId));
    }

    /**
     * Get the URL of the enemy port view.
     *
     * @param portId The port Id.
     * @return An optional URL corresponding to the specified enemy port Id.
     */
    private Optional<URL> getURL(final BaseId portId) {
        Side side = portId.getSide();
        String portName = portId.getName();
        return resource.getSavedURL(side.opposite(), PortView.class, portName + ".json");
    }

    /**
     * Read the airfield data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param portId Uniquely identifies a port.
     * @return The data read from the JSON file.
     */
    private PortViewData readPort(final URL url, final BaseId portId) {
        String portName = portId.getName();
        Side side = portId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            Gson gson = new Gson();
            PortViewData portData = gson.fromJson(br, PortViewData.class);

            log.debug("load enemy port view {} for side {}", portName, side);

            return portData;
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load enemy port view {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Add the port to the port data read in form the JSON file.
     *
     * @param data The port view data read in from the JSON file.
     * @param port The port that is added.
     * @return The modified port view data.
     */
    private PortViewData addPort(final PortViewData data, final Port port) {
        data.setPort(port);
        return data;
    }

    /**
     * Log an error for ports that cannot be loaded.
     *
     * @param portId The port Id that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private PortViewData logError(final BaseId portId) {
        log.error("Unable to load enemy port view '{}'", portId.getName());
        return null;
    }
}
