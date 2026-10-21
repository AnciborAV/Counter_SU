package aav;

import java.util.Objects;

/** Разбор команды отделён от её выполнения и не меняет состояние счётчика. */
enum Command {
    INCREMENT, RESET, VALUE, HELP, STOP, UNKNOWN;

    static Command parse(String input) {
        return switch (Objects.requireNonNull(input, "input").strip()) {
            case "/inc" -> INCREMENT;
            case "/reset" -> RESET;
            case "/value" -> VALUE;
            case "/help" -> HELP;
            case "/stop" -> STOP;
            default -> UNKNOWN;
        };
    }
}
