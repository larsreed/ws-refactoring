package no.support.batch;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Klassen har ansvaret for å holde orden på trådene som kjører.
 */
public class BatchControl implements ThreadControl {

    /** 1 trådteller for hvert steg. */
    private final AtomicIntegerArray threadCounter;
    /** 1 trådflagg for hvert steg. */
    private final boolean[] threadStarted;

    /**
     * Standard constructor.
     * @param steps Antall steg vi skal holde styr på
     */
    public BatchControl(final int steps) {
        this.threadStarted = new boolean[steps];
        this.threadCounter = new AtomicIntegerArray(steps);
    }

    /**
     * Øk antall tråder med 1.
     *
     * @param step   Steg nummer
     * @param caller Hvem ringer
     * @return Antall tråder nå
     */
    @Override
    public int threadsUp(final int step, final Object caller) {
        this.threadCounter.incrementAndGet(step);
        this.threadStarted[step] = true;
        return getThreadCount();
    }

    /**
     * Reduser antall tråder med 1.
     *
     * @param step   Steg nummer
     * @param caller Hvem ringer
     * @return Antall trår nå
     */
    @Override
    public int threadsDown(final int step, final Object caller) {
        this.threadCounter.decrementAndGet(step);
        return getThreadCount();
    }

    /**
     * Returnerer antall aktive tråder totalt.
     *
     * @return Barnetråder
     */
    @Override
    public int getThreadCount() {
        int sum= 0;
        for (int i = 0; i < this.threadCounter.length(); i++) {
            sum += this.threadCounter.get(i);
        }
        return sum;
    }

    /**
     * Sjekk om forrige steg er ferdig.
     *
     * @param lastStep Forrige steg
     * @return <code>true</code> hvis forrige steg er ferdig
     */
    @Override
    public synchronized boolean stepDone(final int lastStep) {
        return this.threadStarted[lastStep]
                       &&
                       (0 >= this.threadCounter.get(lastStep));
    }
}