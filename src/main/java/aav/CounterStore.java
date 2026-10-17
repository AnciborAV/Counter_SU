package aav;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** UTF-8 integer file, compatible with the original out.txt format. */
public final class CounterStore {
    private final Path path;

    public CounterStore(Path path) {
        this.path = Objects.requireNonNull(path).toAbsolutePath();
    }

    public int load() throws IOException {
        final String content;
        try {
            content = Files.readString(path).strip();
        } catch (NoSuchFileException ignored) {
            return 0;
        }
        if (content.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(content);
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid counter file: " + path, exception);
        }
    }

    public void save(int value) throws IOException {
        Files.createDirectories(path.getParent());
        var temporary = Files.createTempFile(path.getParent(), ".counter-", ".tmp");
        try {
            Files.writeString(temporary, Integer.toString(value));
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
