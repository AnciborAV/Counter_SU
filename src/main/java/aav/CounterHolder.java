package aav;

/** Mutable state owned by one console session. */
public final class CounterHolder {
    private int value;

    public CounterHolder(int initialValue) {
        value = initialValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
