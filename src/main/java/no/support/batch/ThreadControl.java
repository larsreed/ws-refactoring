package no.support.batch;

/**
 * Interface for tr�dkontroll.
 */
public interface ThreadControl {
    int threadsUp(int step, Object caller);

    int threadsDown(int step, Object caller);

    int getThreadCount();

    boolean stepDone(int lastStep);
}
