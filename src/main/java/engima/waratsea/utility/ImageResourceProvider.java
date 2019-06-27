package engima.waratsea.utility;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.vessel.Vessel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

/**
 * Utility class to getImageView image view objects.
 */
@Singleton
@Slf4j
public class ImageResourceProvider {

    private static final String IMAGES_DIR = "images/";
    private final GameTitle gameTitle;

    /**
     * Construct a image resource provider.
     *
     * @param gameTitle The game title.
     */
    @Inject
    public ImageResourceProvider(final GameTitle gameTitle) {
        this.gameTitle = gameTitle;
    }

    /**
     * Get an image from a resource file that is wrapped in an image view so that it may be placed on the GUI.
     *
     * @param resourceName The resource file name.
     * @return The image wrapped in an image view.
     */
    public ImageView getImageView(final String resourceName)  {
        return new ImageView(getImage(resourceName));
    }

    /**
     * Get an image from a resource file that is wrapped in an image view so that is may be placed on the GUI.
     * We will look for a scenario specific image first, then a default image.
     *
     * @param scenario The selected scenario name.
     * @param resourceName The resource file name.
     * @return The image wrapped in an image view.
     */
    public ImageView getImageView(final String scenario, final String resourceName) {
        return new ImageView(getImage(scenario, resourceName));
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
     * @param aircraft The aircraft whose image is retrieved.
     * @return The aircraft's image view.
     */
    public Image getAircraftProfileImageView(final Aircraft aircraft) {
        Image image = getAircraftProfileImage(aircraft)
                .orElse(null);

        if (image == null) {
            log.error("Unable to load aircraft profile image for {}", aircraft.getModel());
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
        String path = gameTitle.getValue() + "/ships/" + ship.getSide() + "/images/" + ship.getName() + ".png";
        return loadImage(path);
    }

    /**
     * Load the image that corresponds to the ship's class.
     *
     * @param ship The ship whose image is loaded.
     * @return An optional ship image.
     */
    private Optional<Image> getShipClassImage(final Vessel ship) {
        log.debug("look for ship class '{}'", ship.getShipClass());
        String path = gameTitle.getValue() + "/ships/" + ship.getSide() + "/images/" + ship.getShipClass() + ".png";
        return loadImage(path);
    }

    /**
     * Load the profile image that corresponds to the ship's name.
     *
     * @param ship The ship whose profile image is loaded.
     * @return An optional ship profile image.
     */
    private Optional<Image> getShipNameProfileImage(final Vessel ship) {
        log.info("Look for ship name profile '{}'", ship.getName());
        String path = gameTitle.getValue() + "/ships/" + ship.getSide() + "/images/" + ship.getName() + "-profile.png";
        return loadImage(path);
    }

    /**
     * Load the profile image that corresponds to the ship's class name.
     *
     * @param ship The ship whose profile image is loaded.
     * @return An optional ship profile image.
     */
    private Optional<Image> getShipClassProfileImage(final Vessel ship) {
        log.info("Look for ship class profile '{}'", ship.getShipClass());
        String path = gameTitle.getValue() + "/ships/" + ship.getSide() + "/images/" + ship.getShipClass() + "-profile.png";
        return loadImage(path);
    }

    /**
     * Load the image that corresponds to the aircraft's image.
     *
     * @param aircraft The aircraft whose image is laoded.
     * @return An optional aircraft image.
     */
    private Optional<Image> getAircraftImage(final Aircraft aircraft) {
        String path = gameTitle.getValue() + "/aircraft/" + aircraft.getSide() + "/images/" + aircraft.getModel() + ".png";
        return loadImage(path);
    }

    /**
     * Load the image that corresponds to the aircraft's profile image.
     *
     * @param aircraft The aircraft whose profile image is loaded.
     * @return An optional aircraft profile image.
     */
    private Optional<Image> getAircraftProfileImage(final Aircraft aircraft) {
        String path = gameTitle.getValue() + "/aircraft/" + aircraft.getSide() + "/images/" + aircraft.getModel() + "-profile.png";
        return loadImage(path);
    }

    /**
     * Get an image. First, attempt to getShipData a specific game image for a the current game. If no specific image is found
     * then the application default image is used. This allows any game to override the application default images.
     *
     * @param resourceName The resource file name.
     * @return The image if it exists.
     */
    private Image getImage(final String resourceName) {
        return getGameSpecificImage(resourceName)
                .orElseGet(() -> getDefaultImage(resourceName));
    }

    /**
     * Get an image from a scenario resource file. If the image is not under the scenario then a game specific image
     * is returned. If no game specific image exists then a default application image is returned if it exists.
     *
     * @param scenario The scenario name.
     * @param resourceName The image resource file name.
     * @return The image if it exists.
     */
    public Image getImage(final String scenario, final String resourceName) {
        String path = gameTitle.getValue() + "/scenarios/" + scenario + "/" + resourceName;
        Optional<Image> image = loadImage(path);
        return image.orElseGet(() -> getImage(resourceName));
    }

    /**
     * Get a game specific image.
     *
     * @param resourceName The image resource file name.
     * @return The game specific image if it exists.
     */
    private Optional<Image> getGameSpecificImage(final String resourceName) {
        String path = gameTitle.getValue() + "/" + IMAGES_DIR + resourceName;
        return loadImage(path);
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
        Optional<Image> image = loadImage(path);

        if (!image.isPresent()) {
            log.error("Unable to load resource: {}", path);
        }

        return image.orElse(null);
    }

    /**
     * Gets the actual image file and loads/creates the image.
     *
     * @param path to the image file.
     * @return The image if it exists.
     */
    private Optional<Image> loadImage(final String path) {
        return Optional.ofNullable(getClass()
                .getClassLoader()
                .getResource(path))
                .map(this::getFile);
    }

    /**
     * Get the image file.
     *
     * @param url The image file url.
     * @return The image from the file.
     */
    private Image getFile(final URL url) {
       try {
           File file =  new File(url.toURI().getPath());
           log.debug("Loaded image: {}", url.toURI().getPath());
           return new Image(file.toURI().toString());
       } catch (URISyntaxException ex) {
           log.error("Unable to get URI from URL.", ex);
           return null;
       }
    }

}
