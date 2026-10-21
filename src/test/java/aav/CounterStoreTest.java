package aav;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CounterStoreTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"2147483648", "-2147483649", "1\n2", "1.5", "abc"})
    void invalidContentsAreNotChanged(String content) throws IOException {
        var path = directory.resolve("state.txt");
        Files.writeString(path, content);
        assertThrows(IOException.class, () -> new CounterStore(path).load());
        assertEquals(content, Files.readString(path));
    }

    @Test
    void failedReplacementCleansUpTemporaryFile() throws IOException {
        var target = Files.createDirectory(directory.resolve("occupied"));
        Files.writeString(target.resolve("keep.txt"), "keep");
        assertThrows(IOException.class, () -> new CounterStore(target).save(new Counter(7)));
        assertEquals("keep", Files.readString(target.resolve("keep.txt")));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void parentFileCannotBeUsedAsDirectory() throws IOException {
        var parent = directory.resolve("parent");
        Files.writeString(parent, "keep");
        assertThrows(IOException.class, () -> new CounterStore(parent.resolve("state.txt")).save(Counter.ZERO));
        assertEquals("keep", Files.readString(parent));
    }

    @Test
    void rootIsRejectedBeforeAnyIo() {
        assertThrows(IllegalArgumentException.class, () -> new CounterStore(directory.getRoot()));
    }
}
