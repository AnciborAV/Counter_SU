package aav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Точка входа связывает консоль и хранилище; вся логика доступна для тестирования. */
public final class CounterApp {
    private static final String USAGE = """
            Использование: java -jar target/counter-su-1.0.0-SNAPSHOT.jar [файл]
            По умолчанию: out.txt в текущем рабочем каталоге.
            --help — показать эту справку.
            """;

    // Java 25 позволяет запускать обычный класс через нестатический main.
    void main(String[] args) {
        var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        var output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        var error = new PrintWriter(System.err, true, StandardCharsets.UTF_8);
        int status = run(args, input, output, error);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Возвращает код завершения, не закрывая общие потоки и не останавливая JVM. */
    static int run(String[] args, BufferedReader input, PrintWriter output, PrintWriter error) {
        if (args.length == 1 && "--help".equals(args[0])) {
            output.print(USAGE);
            return output.checkError() ? 1 : 0;
        }
        if (args.length > 1) {
            error.print(USAGE);
            error.flush();
            return 2;
        }
        try {
            var filename = args.length == 0 ? "out.txt" : args[0];
            if (filename.isBlank()) {
                throw new IllegalArgumentException("Путь к файлу не должен быть пустым.");
            }
            var store = new CounterStore(Path.of(filename));
            new CounterProcessor(store, input, output).process();
            return 0;
        } catch (IllegalArgumentException exception) {
            error.println("Некорректный путь: " + exception.getMessage());
            error.flush();
            return 2;
        } catch (IOException exception) {
            error.println("Ошибка счётчика: " + exception.getMessage());
            error.flush();
            return 1;
        }
    }
}
