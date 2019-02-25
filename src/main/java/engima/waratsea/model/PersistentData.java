package engima.waratsea.model;

/**
 * Implement this interface to indicate the class contains persistent data.
 *
 * @param <T> The type of data that is persisted.
 */
public interface PersistentData<T> {
    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    T getData();
}
