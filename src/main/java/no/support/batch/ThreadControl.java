package no.support.batch;

/**
 * Interface for tr�dkontroll.
 */
public interface ThreadControl {
    /** �k antall tr�der. */
    int threadsUp(int step, Object caller);

    /** Reduser antall tr�der. */
    int threadsDown(int step, Object caller);

    /** Hent antall tr�der. */
    int getThreadCount();

    /** Sjekk om forrige steg er ferdig. */
    boolean stepDone(int lastStep);
}
