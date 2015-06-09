package no.support.batch;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class BatchCounter implements ResultCounter {

    /** 1 vente-teller for hvert steg. */
    private final AtomicIntegerArray waitCounter;
    /** 1 OK-teller for hvert steg. */
    private final AtomicIntegerArray total;
    /** 1 feilteller for hvert steg. */
    private final AtomicIntegerArray errCounter;

    public BatchCounter(final int steps) {
        this.waitCounter = new AtomicIntegerArray(steps);
        this.total = new AtomicIntegerArray(steps);
        this.errCounter = new AtomicIntegerArray(steps);
    }

    /**
     * Tell opp antall totalt.
     *
     * @param step Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    @Override
    public int incTotal(final int step) {
        return this.total.incrementAndGet(step);
    }

    /**
     * Tell opp antall feil.
     *
     * @param step Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    @Override
    public int incErr(final int step) {
        return this.errCounter.incrementAndGet(step);
    }

    /**
     * Tell opp ventetid.
     *
     * @param step Hvilket steg
     */
    @Override
    public void incWait(final int step) {
        this.waitCounter.incrementAndGet(step);
    }

    /**
     * Hent teller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    @Override
    public int getTotal(final int step) {
        return this.total.get(step);
    }

    /**
     * Hent feilteller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    @Override
    public int getErr(final int step) {
        return this.errCounter.get(step);
    }

    @Override
    public int getWait(final int step) {
        return this.waitCounter.get(step);
    }
}