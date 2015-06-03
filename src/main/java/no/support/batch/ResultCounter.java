package no.support.batch;

/**
 * Interface for telling.
 */
public interface ResultCounter {
    int incTotal(int steg);

    int incErr(int steg);

    void addWait(int steg);

    int getTotal(int step);

    int getErr(int step);

    int getWait(int step);
}
