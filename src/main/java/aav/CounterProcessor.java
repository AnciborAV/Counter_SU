package aav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/** Input/output are injected so commands can be tested without System.in. */
public final class CounterProcessor {
    private final CounterStore store;
    private final BufferedReader input;
    private final PrintWriter output;

    public CounterProcessor(CounterStore store, BufferedReader input, PrintWriter output) {
        this.store = Objects.requireNonNull(store);
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
    }

    public void process() throws IOException {
        var counter = new CounterHolder(store.load());
        output.printf("Counter: %d. Commands: /inc, /reset, /stop%n", counter.getValue());
        output.flush();
        String line;
        while ((line = input.readLine()) != null) {
            switch (line.strip()) {
                case "/inc" -> {
                    try {
                        update(counter, Math.incrementExact(counter.getValue()));
                    } catch (ArithmeticException ignored) {
                        output.println("Maximum counter value reached. Use /reset.");
                    }
                }
                case "/reset" -> update(counter, 0);
                case "/stop" -> {
                    finish(counter);
                    return;
                }
                default -> output.println("Unknown command. Use /inc, /reset or /stop.");
            }
            output.flush();
        }
        finish(counter);
    }

    private void update(CounterHolder counter, int newValue) throws IOException {
        // Publish the new in-memory value only after persistence succeeds.
        store.save(newValue);
        counter.setValue(newValue);
        output.printf("Counter: %d%n", newValue);
    }

    private void finish(CounterHolder counter) throws IOException {
        store.save(counter.getValue());
        output.printf("Saved. Final counter: %d%n", counter.getValue());
        output.flush();
    }
}
