package aav;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {
    @TempDir
    Path directory;

    private CounterStore store() {
        return new CounterStore(directory.resolve("out.txt"));
    }

    private String run(String commands) throws IOException {
        var output = new StringWriter();
        new CounterProcessor(store(), new BufferedReader(new StringReader(commands)),
                new PrintWriter(output)).process();
        return output.toString();
    }

    @Test
    void missingAndEmptyFilesStartAtZero() throws IOException {
        assertEquals(0, store().load().value());
        Files.writeString(directory.resolve("out.txt"), "  ");
        assertEquals(0, store().load().value());
    }

    @Test
    void commandsPersistAndStopIgnoresRemainingInput() throws IOException {
        run("/inc\n/inc\n/stop\n/inc\n");
        assertEquals(2, store().load().value());
    }

    @Test
    void eofSavesWithoutCrashing() throws IOException {
        run("/inc\n");
        assertEquals(1, store().load().value());
    }

    @Test
    void resetIsPersistedAcrossSessions() throws IOException {
        store().save(new Counter(42));
        run("/reset\n/stop\n");
        assertEquals(0, store().load().value());
        run("/inc\n");
        assertEquals(1, store().load().value());
    }

    @Test
    void unknownCommandLeavesValueUnchanged() throws IOException {
        store().save(new Counter(3));
        assertTrue(run("???\n").contains("Неизвестная команда"));
        assertEquals(3, store().load().value());
    }

    @Test
    void overflowDoesNotWrapOrOverwrite() throws IOException {
        store().save(new Counter(Integer.MAX_VALUE));
        assertTrue(run("/inc\n").contains("Достигнуто максимальное значение"));
        assertEquals(Integer.MAX_VALUE, store().load().value());
    }

    @Test
    void corruptFileIsNotSilentlyReset() throws IOException {
        var file = directory.resolve("out.txt");
        Files.writeString(file, "broken");
        assertThrows(IOException.class, () -> run("/reset\n"));
        assertEquals("broken", Files.readString(file));
    }

    @Test
    void whitespaceAndLegacyNegativeValuesAreSupported() throws IOException {
        Files.writeString(directory.resolve("out.txt"), " -2\n");
        run("  /inc  \n");
        assertEquals(-1, store().load().value());
    }

    @Test
    void incrementCreatesNewValue() {
        var original = new Counter(8);
        assertEquals(new Counter(9), original.increment());
        assertEquals(8, original.value());
        assertThrows(ArithmeticException.class, () -> new Counter(Integer.MAX_VALUE).increment());
    }

    @Test
    void helpAndValueDoNotChangeCounter() throws IOException {
        store().save(new Counter(7));
        var output = run("/help\n/value\n/stop\n");
        assertTrue(output.contains("Счётчик: 7"));
        assertTrue(output.contains("/inc"));
        assertEquals(7, store().load().value());
    }

    @Test
    void nestedDirectoryIsCreatedAndTemporaryFilesAreRemoved() throws IOException {
        var nested = new CounterStore(directory.resolve("nested/value.txt"));
        nested.save(new Counter(7));
        nested.save(new Counter(8));
        assertEquals(8, nested.load().value());
        try (var files = Files.list(directory.resolve("nested"))) {
            assertEquals(1, files.count());
        }
    }
}
