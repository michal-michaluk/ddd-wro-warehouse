package tools;

import warehouse.Persistence;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by michal on 11.08.2016.
 */
public class FileStore {

    private final Path store = Paths.get("store");

    public FileStore() {
    }

    public void append(String index, Object entry) {
        try {
            Path path = store.resolve(index + ".json");
            if (Files.notExists(path)) {
                Files.createDirectories(store);
                Files.createFile(path);
            }
            try (FileOutputStream file = new FileOutputStream(path.toFile(), true)) {
                file.write('\n');
                Persistence.mapper.writeValue(file, entry);
                file.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> List<T> readAll(String index, Class<T> entryType) {
        Path path = store.resolve(index + ".json");
        if (Files.notExists(path)) {
            return Collections.emptyList();
        }
        try (Stream<String> stream = Files.lines(path)) {
            return stream.filter((s) -> !s.isEmpty())
                    .map((line) -> retrieveEvent(line, entryType))
                    .filter(Objects::nonNull)
                    .collect(Collectors.<T>toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T retrieveEvent(String line, Class<T> entryType) {
        try {
            return Persistence.mapper.readValue(line, entryType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
