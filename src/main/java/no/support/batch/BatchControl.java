package no.support.batch;

import java.util.concurrent.atomic.AtomicInteger;

public class BatchControl implements ThreadControl {
    /** 1 trådteller for hvert steg. */
    private final AtomicInteger[] threadCounter;
    /** 1 trådflagg for hvert steg. */
    private final boolean[] threadStarted;

    public BatchControl(final int steps) {
        this.threadStarted = new boolean[steps];
        this.threadCounter = new AtomicInteger[steps];
        for (int i=0; i< steps; i++) {
            this.threadCounter[i]= new AtomicInteger();
        }
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
        this.threadCounter[step].incrementAndGet();
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
        this.threadCounter[step].decrementAndGet();
        return getThreadCount();
    }

    /**
     * Returnerer antall aktive trår totalt.
     *
     * @return Barnetråder
     */
    @Override
    public int getThreadCount() {
        int sum = 0;
        for (final AtomicInteger i : this.threadCounter) {
            sum += i.get();
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
                       (0 >= this.threadCounter[lastStep].get());
    }
}