package engima.waratsea.utility;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import engima.waratsea.model.game.Game;

import java.net.URL;
import java.util.Optional;

/**
 * Utility class to getImageView CSS file paths.
 */
@Singleton
@Slf4j
public class CssResourceProvider {

    private static final String CSS_DIR = "/css/";
    private final Game game;

    /**
     * Construct a css resource provider. The game may override the default css styles for a given view.
     *
     * @param game The game.
     */
    @Inject
    public CssResourceProvider(final Game game) {
        this.game = game;
    }

    /**
     * Get the css file path that corresponds to the given name.
     *
     * @param name name of the css file.
     * @return path of the css file.
     */
    public String get(final String name) {
        String cssPath = game.getName() + CSS_DIR + name;

        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(cssPath));
        return url.isPresent() ? useGameSpecificCss(cssPath) : useGameDefaultCss(name);
    }

    /**
     * Use the game specific defined css file. This is just a utility file
     * to aid logging and provide readable code.
     *
     * @param cssPath  path to the game specific css file.
     * @return The path to the game specific css file.
     */
    private String useGameSpecificCss(final String cssPath) {
        log.debug("Using game css file: {}", cssPath);
        return cssPath;
    }

    /**
     * Use the game default css file.
     *
     * @param name name of the css file.
     * @return The path to the game default css file.
     */
    private String useGameDefaultCss(final String name) {
        String cssPath = CSS_DIR + name;
        log.debug("Using default css file: {}", cssPath);
        return cssPath;
    }

}
