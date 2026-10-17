package aav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class CounterApp {
    private CounterApp() {
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java -jar counter-su.jar [counter-file]");
            System.exit(2);
        }
        var path = Path.of(args.length == 0 ? "out.txt" : args[0]);
        var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        var output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        try {
            new CounterProcessor(new CounterStore(path), input, output).process();
        } catch (IOException exception) {
            System.err.println("Counter could not complete: " + exception.getMessage());
            System.exit(1);
        }
    }
}
