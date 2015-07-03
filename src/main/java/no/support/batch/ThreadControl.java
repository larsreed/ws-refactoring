package no.support.batch;

/**
 * Interface for trådkontroll.
 */
public interface ThreadControl {
    /** Øk antall tråder. */
    int threadsUp(int step, Object caller);

    /** Reduser antall tråder. */
    int threadsDown(int step, Object caller);

    /** Hent antall tråder. */
    int getThreadCount();

    /** Sjekk om forrige steg er ferdig. */
    boolean stepDone(int lastStep);
}
