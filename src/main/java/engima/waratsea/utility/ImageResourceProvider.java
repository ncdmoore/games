package engima.waratsea.utility;

import com.google.inject.Inject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.game.Game;

import javax.inject.Singleton;
import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 * Utility class to getImageView image view objects.
 */
@Singleton
@Slf4j
public class ImageResourceProvider {

    private static final String IMAGES_DIR = "images/";
    private final Game game;

    /**
     * Construct a image resource provider.
     *
     * @param game The game object.
     */
    @Inject
    public ImageResourceProvider(final Game game) {
        this.game = game;
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
     * Get an image. First, attempt to get a specific game image for a the current game. If no specific image is found
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
        String path = game.getName() + "/scenarios/" + scenario + "/" + resourceName;
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
        String path = game.getName() + "/" + IMAGES_DIR + resourceName;
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
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));

        return url.map(u -> {
            File file = new File(u.getPath());
            log.debug("Loaded image: {}", path);
            return new Image(file.toURI().toString());
        });


    }

}
