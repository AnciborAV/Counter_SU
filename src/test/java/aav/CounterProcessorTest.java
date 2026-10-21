package aav;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CounterProcessorTest {
    private final StringWriter output = new StringWriter();

    private void process(CounterRepository repository, String commands) throws IOException {
        new CounterProcessor(repository, new BufferedReader(new StringReader(commands)),
                new PrintWriter(output)).process();
    }

    @Test
    void savesEveryChangeAndStopsImmediately() throws IOException {
        var repository = new MemoryRepository();
        process(repository, "/inc\n/inc\n/reset\n/stop\n/inc\n");
        assertEquals(List.of(new Counter(1), new Counter(2), Counter.ZERO, Counter.ZERO), repository.saved);
    }

    @Test
    void readOnlyCommandsDoNotWriteUntilSessionEnds() throws IOException {
        var repository = new MemoryRepository();
        process(repository, "/help\n/value\n???\n");
        assertEquals(List.of(Counter.ZERO), repository.saved);
    }

    @Test
    void failedSaveDoesNotReportSuccessOrContinue() {
        var failure = new IOException("disk full");
        var repository = new CounterRepository() {
            public Counter load() { return Counter.ZERO; }
            public void save(Counter counter) throws IOException { throw failure; }
        };
        assertSame(failure, assertThrows(IOException.class,
                () -> process(repository, "/inc\n/reset\n")));
        assertFalse(output.toString().contains("Счётчик: 1"));
        assertFalse(output.toString().contains("Сохранено."));
    }

    @Test
    void failedLoadDoesNotOverwriteStorage() {
        var repository = new CounterRepository() {
            public Counter load() throws IOException { throw new IOException("cannot read"); }
            public void save(Counter counter) { fail("Load failure must not cause a write"); }
        };
        assertThrows(IOException.class, () -> process(repository, "/reset\n"));
        assertEquals("", output.toString());
    }

    @Test
    void outputFailureIsNotSilentlyIgnored() {
        var writer = new Writer() {
            public void write(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("broken output");
            }
            public void flush() { }
            public void close() { }
        };
        var repository = new MemoryRepository();
        var processor = new CounterProcessor(repository, new BufferedReader(new StringReader("/inc\n")),
                new PrintWriter(writer));
        assertThrows(IOException.class, processor::process);
        assertTrue(repository.saved.isEmpty());
    }

    @Test
    void readFailureDoesNotClaimSuccessfulCompletion() {
        var reader = new Reader() {
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("broken input");
            }
            public void close() { }
        };
        var repository = new MemoryRepository();
        var processor = new CounterProcessor(repository, new BufferedReader(reader), new PrintWriter(output));
        assertThrows(IOException.class, processor::process);
        assertTrue(repository.saved.isEmpty());
        assertFalse(output.toString().contains("Сохранено."));
    }

    private static final class MemoryRepository implements CounterRepository {
        private final List<Counter> saved = new ArrayList<>();

        public Counter load() { return Counter.ZERO; }
        public void save(Counter counter) { saved.add(counter); }
    }
}
