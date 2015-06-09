package no.support.batch;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Klassen har ansvaret for � holde orden p� tr�dene som kj�rer.
 */
public class BatchControl implements ThreadControl {

    /** 1 tr�dteller for hvert steg. */
    private final AtomicIntegerArray threadCounter;
    /** 1 tr�dflagg for hvert steg. */
    private final boolean[] threadStarted;

    /**
     * Standard constructor.
     * @param steps Antall steg vi skal holde styr p�
     */
    public BatchControl(final int steps) {
        this.threadStarted = new boolean[steps];
        this.threadCounter = new AtomicIntegerArray(steps);
    }

    /**
     * �k antall tr�der med 1.
     *
     * @param step   Steg nummer
     * @param caller Hvem ringer
     * @return Antall tr�der n�
     */
    @Override
    public int threadsUp(final int step, final Object caller) {
        this.threadCounter.incrementAndGet(step);
        this.threadStarted[step] = true;
        return getThreadCount();
    }

    /**
     * Reduser antall tr�der med 1.
     *
     * @param step   Steg nummer
     * @param caller Hvem ringer
     * @return Antall tr�r n�
     */
    @Override
    public int threadsDown(final int step, final Object caller) {
        this.threadCounter.decrementAndGet(step);
        return getThreadCount();
    }

    /**
     * Returnerer antall aktive tr�der totalt.
     *
     * @return Barnetr�der
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