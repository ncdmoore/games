package engima.waratsea.utility;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class that wraps properties.
 *
 */
@Slf4j
public class PropertyWrapper {

    private final Properties properties = new Properties();

    /**
     * Initialize the property wrapper by reading the  property resource file.
     *
     * @param name The name of the property file.
     */
    public void init(final String name) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name)) {
            properties.load(inputStream);
            log.info("Loaded properties: {}", name);
        } catch (IOException | RuntimeException ex) {
            log.warn("Unable to load properties file {}", name);
        }
    }

    /**
     * Get a property string value.
     *
     * @param key the property key.
     * @return The value of the property.
     */
    public String getString(final String key) {
        return properties.getProperty(key);
    }

    /**
     * Get the property string value if the given key is found; otherwise, the default is returned.
     *
     * @param key the property key
     * @param defaultValue The property default value.
     * @return The value of the property or the default if no value found.
     */
    public String getString(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get the property int value.
     *
     * @param key the property key.
     * @return The value of the property.
     */
    public int getInt(final String key) {
        return Integer.parseInt(properties.getProperty(key));
    }


    /**
     * Get the property value.
     * @param key the property key.
     * @return The value of the property.
     */
    public double getDouble(final String key) {
        return Double.parseDouble(properties.getProperty(key));
    }
}
