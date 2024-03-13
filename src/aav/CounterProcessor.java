package aav;

import aav.CounterHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static aav.Constants.*;

public class CounterProcessor {
    private final CounterHolder counterHolder;
    private final SerializeService serializeService;

    public CounterProcessor() {
        serializeService = new SerializeService();
        counterHolder = new CounterHolder();

        var savedValue = serializeService.deserialize();
        counterHolder.setValue(savedValue);
    }

    public void process() {
        var reader = new BufferedReader(new InputStreamReader(System.in));

        theBank:
        while (true) {
            var line = readLine(reader);
            switch (line) {
                case INCREMENT -> {
                    var value = counterHolder.incrementAndGet();
                    System.out.println("New counter value is: " + value);
                }
                case STOP -> {
                    var value = counterHolder.getValue();
                    serializeService.serialize(value);
                    System.out.println("I'm finishing work! Current value is: " +
                                              value);
                    break theBank;
                }
                case RESET -> {
                    var value = counterHolder.reset();
                    System.out.println("The counter has been reset! Value is: " +
                                              value);
                }
                default -> System.out.println("Can't parse input, try again");
            }
        }
    }

    private static String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
