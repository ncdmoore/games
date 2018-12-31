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
public class ProperyWrapper {

    private Properties properties = new Properties();

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
            log.warn("Unable to open resource file {}", name);
        }
    }

    /**
     * Get a property string health.
     *
     * @param key the property to getShipData.
     * @return The health of the property.
     */
    public String getString(final String key) {
        return properties.getProperty(key);
    }

    /**
     * Get the property int health.
     *
     * @param key the property to getShipData.
     * @return The health of the property.
     */
    public int getInt(final String key) {
        return Integer.parseInt(properties.getProperty(key));
    }


    /**
     * Get the property health.
     * @param key the property to getShipData.
     * @return The health of the property.
     */
    public double getDouble(final String key) {
        return Double.parseDouble(properties.getProperty(key));
    }
}
