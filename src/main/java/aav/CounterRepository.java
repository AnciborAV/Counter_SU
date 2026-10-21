package aav;

import java.io.IOException;

/** Контракт хранения: консольная сессия не зависит от устройства файловой системы. */
public interface CounterRepository {
    Counter load() throws IOException;

    void save(Counter counter) throws IOException;
}
