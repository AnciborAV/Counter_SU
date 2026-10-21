package aav;

/** Неизменяемое значение счётчика: операция возвращает новый объект. */
public record Counter(int value) {
    public static final Counter ZERO = new Counter(0);

    /** Переполнение вызывает исключение, а не превращает максимум в отрицательное число. */
    public Counter increment() {
        return new Counter(Math.incrementExact(value));
    }
}
