package engima.waratsea.utility;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.vessel.Vessel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

/**
 * Utility class to getImageView image view objects.
 */
@Singleton
@Slf4j
public class ResourceProvider {
    private static final String IMAGE_EXT = ".png";
    private static final String SOUND_EXT = ".mp3";
    private static final String AIRCRAFT_DIR = "/aircraft/";
    private static final String SCENARIO_DIR = "/scenarios/";
    private static final String SHIPS_DIR = "/ships/";
    private static final String IMAGES_DIR = "images/";
    private static final String SOUNDS_DIR = "sounds/";

    private final GameTitle gameTitle;
    private final Resource resource;

    /**
     * Construct a image resource provider.
     *
     * @param gameTitle The game title.
     * @param resource The game resources.
     */
    @Inject
    public ResourceProvider(final GameTitle gameTitle,
                            final Resource resource) {
        this.gameTitle = gameTitle;
        this.resource = resource;
    }

    /**
     * Get an image from a resource file that is wrapped in an image view so that it may be placed on the GUI.
     *
     * @param resourceName The resource file name.
     * @return The image wrapped in an image view.
     */
    public ImageView getImageView(final String resourceName)  {
        String scenario = resource.getScenario();
        return new ImageView(getImage(scenario, resourceName));
    }

    /**
     * Get an image. First, attempt to get a scenario specific image. If no scenario image is found attempt to get a
     * game specific image. If no game specific image is found then the application default image is used. This allows
     * any game to override the application default images and any scenario to override any game image.
     *
     * @param resourceName The resource file name.
     * @return The image if it exists.
     */
    public Image getImage(final String resourceName) {
        String scenario = resource.getScenario();
        String path = gameTitle.getValue() + SCENARIO_DIR + scenario + "/" + resourceName;
        log.debug("get image: {}", path);
        Optional<Image> image = loadImageResource(path);
        return image.orElseGet(() -> getGameImage(resourceName));
    }

    /**
     * Get a media.
     *
     * @param resourceName The resource file name.
     * @return The media if it exists.
     */
    public Media getMedia(final String resourceName) {
        return getGameMedia(resourceName);
    }

    /**
     * Get an image. First, attempt to get a scenario specific image. If no scenario image is found attempt to get a
     * game specific image. If no game specific image is found then the application default image is used. This allows
     * any game to override the application default images and any scenario to override any game image.
     * 
     * This method exists for scenario images where the scenario has not be selected; i.e., the scenario selection
     * GUI and the saved game GUI views.
     *
     * @param scenario The scenario name.
     * @param resourceName The image resource file name.
     * @return The image if it exists.
     */
    public Image getImage(final String scenario, final String resourceName) {
        String path = gameTitle.getValue() + SCENARIO_DIR + scenario + "/" + resourceName;
        log.debug("get image: {}", path);
        Optional<Image> image = loadImageResource(path);
        return image.orElseGet(() -> getGameImage(resourceName));
    }

    /**
     * Get a ship image. Attempt to get an image for the ship name. If that fails get the image for the ship's
     * class.
     *
     * @param ship The ship whose image is retrieved.
     * @return The ship's image view.
     */
    public ImageView getShipImageView(final Vessel ship) {
        Image image = getShipNameImage(ship)
                .orElseGet(() -> getShipClassImage(ship)
                        .orElse(null));

        if (image == null) {
            log.error("Unable to load ship image for {}", ship.getName());
        }

        return new ImageView(image);

    }

    /**
     * Get a ship image. Attempt to get an image for the ship name. If that fails get the image for the ship's
     * class.
     *
     * @param ship The ship whose image is retrieved.
     * @return The ship's image view.
     */
    public Image getShipImage(final Vessel ship) {
        Image image = getShipNameImage(ship)
                .orElseGet(() -> getShipClassImage(ship)
                        .orElse(null));

        if (image == null) {
            log.error("Unable to load ship image for {}", ship.getName());
        }

        return image;

    }

    /**
     * Get the ship profile image. Attempt to get an image for the ship name. If that fails get the image for the
     * ship's class.
     *
     * @param ship The ship whose image is retrieved.
     * @return The ship's image view.
     */
    public ImageView getShipProfileImageView(final Vessel ship) {
        Image image = getShipNameProfileImage(ship)
                .orElseGet(() -> getShipClassProfileImage(ship)
                        .orElse(null));

        if (image == null) {
            log.error("Unable to load the ship profile image for '{}' of class '{}'", ship.getName(), ship.getShipClass());
        }

        return new ImageView(image);
    }

    /**
     * Get the ship profile image. Attempt to get an image for the ship name. If that fails get the image for the
     * ship's class.
     *
     * @param ship The ship whose image is retrieved.
     * @return The ship's image view.
     */
    public Image getShipProfileImage(final Vessel ship) {
        Image image = getShipNameProfileImage(ship)
                .orElseGet(() -> getShipClassProfileImage(ship)
                        .orElse(null));

        if (image == null) {
            log.error("Unable to load the ship profile image for '{}' of class '{}'", ship.getName(), ship.getShipClass());
        }

        return image;
    }

    /**
     * Get the aircraft image.
     *
     * @param aircraft The aircraft whose image is retrieved.
     * @return The aircraft's image view.
     */
    public Image getAircraftImageView(final Aircraft aircraft) {
        Image image = getAircraftImage(aircraft)
                .orElse(null);

        if (image == null) {
            log.error("Unable to load aircraft image for {}", aircraft.getModel());
        }

        return image;
    }

    /**
     * Get the aircraft image.
     *
     * @param side The side: ALLIES or AXIS.
     * @param aircraft The aircraft whose image is retrieved.
     * @return The aircraft's image view.
     */
    public Image getAircraftProfileImageView(final Side side, final String aircraft) {
        Image image = getAircraftProfileImage(side, aircraft)
                .orElse(null);

        if (image == null) {
            log.error("Unable to load aircraft profile image for {}", aircraft);
        }

        return image;
    }

    /**
     * Load the image that corresponds to the ship name.
     *
     * @param ship The ship whose image is loaded.
     * @return An optional ship image.
     */
    private Optional<Image> getShipNameImage(final Vessel ship) {
        log.debug("look for ship name '{}'", ship.getName());
        String path = gameTitle.getValue() + SHIPS_DIR + ship.getSide() + "/images/" + ship.getName() + IMAGE_EXT;
        return loadImageResource(path);
    }

    /**
     * Load the image that corresponds to the ship's class.
     *
     * @param ship The ship whose image is loaded.
     * @return An optional ship image.
     */
    private Optional<Image> getShipClassImage(final Vessel ship) {
        log.debug("look for ship class '{}'", ship.getShipClass());
        String path = gameTitle.getValue() + SHIPS_DIR + ship.getSide() + "/images/" + ship.getShipClass() + IMAGE_EXT;
        return loadImageResource(path);
    }

    /**
     * Load the profile image that corresponds to the ship's name.
     *
     * @param ship The ship whose profile image is loaded.
     * @return An optional ship profile image.
     */
    private Optional<Image> getShipNameProfileImage(final Vessel ship) {
        log.debug("Look for ship name profile '{}'", ship.getName());
        String path = gameTitle.getValue() + SHIPS_DIR + ship.getSide() + "/images/" + ship.getName() + "-profile.png";
        return loadImageResource(path);
    }

    /**
     * Load the profile image that corresponds to the ship's class name.
     *
     * @param ship The ship whose profile image is loaded.
     * @return An optional ship profile image.
     */
    private Optional<Image> getShipClassProfileImage(final Vessel ship) {
        log.debug("Look for ship class profile '{}'", ship.getShipClass());
        String path = gameTitle.getValue() + SHIPS_DIR + ship.getSide() + "/images/" + ship.getShipClass() + "-profile.png";
        return loadImageResource(path);
    }

    /**
     * Load the image that corresponds to the aircraft's image.
     *
     * @param aircraft The aircraft whose image is laoded.
     * @return An optional aircraft image.
     */
    private Optional<Image> getAircraftImage(final Aircraft aircraft) {
        String path = gameTitle.getValue() + AIRCRAFT_DIR + aircraft.getSide() + "/images/" + aircraft.getModel() + IMAGE_EXT;
        return loadImageResource(path);
    }

    /**
     * Load the image that corresponds to the aircraft's profile image.
     *
     * @param side The side: ALLIES or AXIS
     * @param aircraft The aircraft whose profile image is loaded.
     * @return An optional aircraft profile image.
     */
    private Optional<Image> getAircraftProfileImage(final Side side, final String aircraft) {
        String path = gameTitle.getValue() + AIRCRAFT_DIR + side + "/images/" + aircraft + "-profile.png";
        return loadImageResource(path);
    }

    private Image getGameImage(final String resourceName) {
        return getGameSpecificImage(resourceName)
                .orElseGet(() -> getDefaultImage(resourceName));
    }

    private Media getGameMedia(final String resourceName) {
        return getDefaultMedia(resourceName);
    }

    /**
     * Get a game specific image.
     *
     * @param resourceName The image resource file name.
     * @return The game specific image if it exists.
     */
    private Optional<Image> getGameSpecificImage(final String resourceName) {
        String path = gameTitle.getValue() + "/" + IMAGES_DIR + resourceName;
        return loadImageResource(path);
    }

    /**
     * Get an application default image. This method returns null
     * to make it compatible with the lambda expression used in getImage.
     *
     * @param resourceName The image resource file name.
     * @return The application default image if it exists.
     */
    private Image getDefaultImage(final String resourceName) {
        String path = IMAGES_DIR + resourceName;
        Optional<Image> image = loadImageResource(path);

        if (image.isEmpty()) {
            log.error("Unable to load image resource: {}", path);
        }

        return image.orElse(null);
    }

    private Media getDefaultMedia(final String resourceName) {
        String path = SOUNDS_DIR + resourceName;
        log.debug("Load media at path: '{}'", path);

        Optional<Media> media = loadMediaResource(path);

        if (media.isEmpty()) {
            log.error("Unable to load media resource: {}", path);
        }

        return media.orElse(null);
    }

    /**
     * Gets the actual image file and loads/creates the image.
     *
     * @param path to the image file.
     * @return The image if it exists.
     */
    private Optional<Image> loadImageResource(final String path) {
        return Optional.ofNullable(getClass()
                .getClassLoader()
                .getResource(path))
                .map(this::getFileURI)
                .map(this::getImageFromURI);
    }

    private Optional<Media> loadMediaResource(final String path) {
        return Optional.ofNullable(getClass()
                .getClassLoader()
                .getResource(path))
                .map(this::getFileURI)
                .map(this::getMediaFromURI);
    }

    /**
     * Get the image file.
     *
     * @param url The image file url.
     * @return The image from the file.
     */
    private URI getFileURI(final URL url) {
       try {
           File file =  new File(url.toURI().getPath());
           log.debug("Loaded image: {}", url.toURI().getPath());
           return file.toURI();
       } catch (URISyntaxException ex) {
           log.error("Unable to get URI from URL.", ex);
           return null;
       }
    }

    private Image getImageFromURI(final URI uri) {
        return new Image(uri.toString());
    }

    private Media getMediaFromURI(final URI uri) {
        return new Media(uri.toString());
    }

}
