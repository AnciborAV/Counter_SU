package aav;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CounterAppTest {
    @TempDir
    Path directory;
    private final StringWriter output = new StringWriter();
    private final StringWriter error = new StringWriter();

    private int run(String... args) {
        return CounterApp.run(args, new BufferedReader(new StringReader("/inc\n/stop\n")),
                new PrintWriter(output), new PrintWriter(error));
    }

    @Test
    void savesToCustomPath() throws IOException {
        var file = directory.resolve("state.txt");
        assertEquals(0, run(file.toString()));
        assertEquals("1", Files.readString(file));
        assertEquals("", error.toString());
    }

    @Test
    void helpDoesNotStartSession() {
        assertEquals(0, run("--help"));
        assertTrue(output.toString().contains("Использование:"));
        assertFalse(output.toString().contains("Счётчик:"));
    }

    @Test
    void tooManyArgumentsAreRejectedBeforeWriting() {
        var file = directory.resolve("state.txt");
        assertEquals(2, run(file.toString(), "extra"));
        assertFalse(Files.exists(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "bad\u0000path"})
    void invalidPathReturnsUsageError(String path) {
        assertEquals(2, run(path));
        assertTrue(error.toString().contains("Некорректный путь"));
    }

    @Test
    void corruptedStateIsPreserved() throws IOException {
        var file = directory.resolve("state.txt");
        Files.writeString(file, "broken");
        assertEquals(1, run(file.toString()));
        assertEquals("broken", Files.readString(file));
        assertTrue(error.toString().contains("Ошибка счётчика"));
    }

    @Test
    void directoryCannotBeUsedAsStateFile() {
        assertEquals(1, run(directory.toString()));
    }
}
