package aav;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandTest {
    @ParameterizedTest
    @CsvSource({"/inc, INCREMENT", "/reset, RESET", "/value, VALUE", "/help, HELP", "/stop, STOP"})
    void parsesCommandsAndSurroundingWhitespace(String input, Command expected) {
        assertEquals(expected, Command.parse("  " + input + "\t"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "/INC", "/inc 2", "unknown"})
    void rejectsUnsupportedInput(String input) {
        assertEquals(Command.UNKNOWN, Command.parse(input));
    }
}
