package aav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/** Одна консольная сессия. Переданные потоки принадлежат вызывающему коду. */
public final class CounterProcessor {
    private static final String HELP = """
            Команды:
              /inc   — увеличить на 1
              /reset — сбросить в 0
              /value — показать значение
              /help  — показать справку
              /stop  — сохранить и выйти
            """;

    private final CounterRepository repository;
    private final BufferedReader input;
    private final PrintWriter output;

    public CounterProcessor(CounterRepository repository, BufferedReader input, PrintWriter output) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
    }

    public void process() throws IOException {
        var counter = repository.load();
        printValue(counter);
        output.print(HELP);
        flushOutput();

        String line;
        while ((line = input.readLine()) != null) {
            switch (Command.parse(line)) {
                case INCREMENT -> {
                    if (counter.value() == Integer.MAX_VALUE) {
                        output.println("Достигнуто максимальное значение. Используйте /reset.");
                    } else {
                        counter = save(counter.increment());
                        printValue(counter);
                    }
                }
                case RESET -> {
                    counter = save(Counter.ZERO);
                    printValue(counter);
                }
                case VALUE -> printValue(counter);
                case HELP -> output.print(HELP);
                case STOP -> {
                    finish(counter);
                    return;
                }
                case UNKNOWN -> output.println("Неизвестная команда. Введите /help.");
            }
            flushOutput();
        }
        // Конец ввода (Ctrl+D на macOS) завершает сессию так же, как /stop.
        finish(counter);
    }

    private Counter save(Counter counter) throws IOException {
        // Сначала сохраняем: при ошибке записи новое значение не объявляется успешным.
        repository.save(counter);
        return counter;
    }

    private void finish(Counter counter) throws IOException {
        save(counter);
        output.printf("Сохранено. Итоговое значение: %d%n", counter.value());
        flushOutput();
    }

    private void printValue(Counter counter) {
        output.printf("Счётчик: %d%n", counter.value());
    }

    private void flushOutput() throws IOException {
        // PrintWriter сам подавляет IOException; checkError также сбрасывает буфер.
        if (output.checkError()) {
            throw new IOException("Не удалось вывести результат в консоль.");
        }
    }
}
