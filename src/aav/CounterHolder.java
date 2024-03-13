package aav;

public class CounterHolder {
    private static int COUNTER = 0;

    public int incrementAndGet() {
        return ++COUNTER;
    }

    public int getValue() {
        return COUNTER;
    }

    public int setValue(int newValue) {
        COUNTER = newValue;
        return newValue;
    }

    public int reset() {
        return setValue(0);
    }
}
