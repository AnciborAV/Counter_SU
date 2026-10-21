package aav;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Файловое хранилище UTF-8; сохраняет совместимость с прежним out.txt. */
public final class CounterStore implements CounterRepository {
    private final Path path;

    public CounterStore(Path path) {
        this.path = Objects.requireNonNull(path, "path").toAbsolutePath();
        if (this.path.getFileName() == null) {
            throw new IllegalArgumentException("Укажите путь к файлу, а не корень диска.");
        }
    }

    @Override
    public Counter load() throws IOException {
        final String content;
        try {
            content = Files.readString(path).strip();
        } catch (NoSuchFileException ignored) {
            return Counter.ZERO;
        }
        if (content.isEmpty()) {
            return Counter.ZERO;
        }
        try {
            return new Counter(Integer.parseInt(content));
        } catch (NumberFormatException exception) {
            throw new IOException("Файл должен содержать целое число типа int: " + path, exception);
        }
    }

    @Override
    public void save(Counter counter) throws IOException {
        Objects.requireNonNull(counter, "counter");
        Files.createDirectories(path.getParent());
        var temporary = Files.createTempFile(path.getParent(), ".counter-", ".tmp");
        try {
            Files.writeString(temporary, Integer.toString(counter.value()));
            // Временный файл рядом с основным позволяет выполнить атомарную замену.
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
