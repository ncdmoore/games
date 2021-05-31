package engima.waratsea.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engima.waratsea.model.PersistentData;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A persistent utility class to aid in persisting data.
 */
@Slf4j
public final class PersistentUtility {

    /**
     * This is a utility method to get a given list of objects corresponding data objects.
     *
     * @param input The given list of objects.
     * @param <T> The type of data object.
     * @param <R> The type of given object.
     * @return A list of data objects of type R that correspond to the input type T.
     */
    public static <T, R extends PersistentData<T>> List<T> getData(final List<R> input) {
        return Optional.ofNullable(input)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(R::getData)
                .collect(Collectors.toList());
    }

    /**
     * Save a list of objects.
     *
     * @param fileName The file to save the data to.
     * @param input The given list of objects.
     * @param <T> The type of data object. The persistent part of the object that is saved.
     * @param <R> The type of given object. The object that is saved.
     */
    public static <T, R extends PersistentData<T>> void save(final String fileName, final List<R> input) {
        List<T> data = getData(input);
        saveList(fileName, data);

        // Save the object's children if it has any.
        input.forEach(PersistentData::saveChildrenData);
    }

    /**
     * Save a single data object.
     *
     * @param fileName The file to save the data to.
     * @param data The data that is saved to the given file.
     * @param <T> The type of data object. The persistent part of the object that is saved.
     * @param <R> The type of given object. The object that is saved.
     */
    public static <T, R extends PersistentData<T>> void save(final String fileName, final R data) {
        T t = data.getData();
        saveObject(fileName, t);
    }

    /**
     * Save a single data object.
     *
     * @param fileName The file to save the data to.
     * @param data The data that is saved to the given file.
     * @param <T> The type of object saved.
     */
    private static <T> void saveObject(final String fileName, final T data) {
        Path path = Paths.get(fileName);

        try {
            Files.createDirectories(Optional.ofNullable(path.getParent()).orElseThrow(IOException::new));
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            FileOutputStream out = new FileOutputStream(path.toString());

            try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(data);
                writer.write(json);
            }

        } catch (IOException ex) {
            log.error("Unable to save  '{}' file not found.", fileName, ex);
        }
    }

    /**
     * Save a list of data objects.
     *
     * @param fileName The file to save the data to.
     * @param data A list of objects that are saved to the given file.
     *
     * @param <T> The type of objects saved.
     */
    private static <T> void saveList(final String fileName, final List<T> data) {
        Path path = Paths.get(fileName);

        try {

            Files.createDirectories(Optional.ofNullable(path.getParent()).orElseThrow(IOException::new));
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            FileOutputStream out = new FileOutputStream(path.toString());

            try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(data);
                writer.write(json);
            }
        } catch (IOException ex) {
            log.error("Unable to save  '{}' file not found.", fileName, ex);
        }
    }

    /**
     * This object is never constructed.
     */
    private PersistentUtility() {
    }
}
