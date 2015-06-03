package no.support.batch;

import java.util.concurrent.atomic.AtomicInteger;

public class BatchCounter implements ResultCounter {
    /** 1 vente-teller for hvert steg. */
    private final AtomicInteger[] waitCounter;
    /** 1 OK-teller for hvert steg. */
    private final AtomicInteger[] total;
    /** 1 feilteller for hvert steg. */
    private final AtomicInteger[] errCounter;

    public BatchCounter(final int steps) {
        this.waitCounter = new AtomicInteger[steps];
        this.total = new AtomicInteger[steps];
        this.errCounter = new AtomicInteger[steps];
        for (int i=0; i< steps; i++) {
            this.waitCounter[i]= new AtomicInteger();
            this.total[i]= new AtomicInteger();
            this.errCounter[i]= new AtomicInteger();
        }
    }

    /**
     * Tell opp antall totalt.
     *
     * @param steg Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    @Override
    public int incTotal(final int steg) {
        return this.total[steg].incrementAndGet();
    }

    /**
     * Tell opp antall feil.
     *
     * @param steg Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    @Override
    public int incErr(final int steg) {
        return this.errCounter[steg].incrementAndGet();
    }

    /**
     * Tell opp ventetid.
     *
     * @param steg Hvilket steg
     */
    @Override
    public void addWait(final int steg) {
        this.waitCounter[steg].incrementAndGet();
    }

    /**
     * Hent teller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    @Override
    public int getTotal(final int step) {
        return this.total[step].get();
    }

    /**
     * Hent feilteller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    @Override
    public int getErr(final int step) {
        return this.errCounter[step].get();
    }

    @Override
    public int getWait(final int step) {
        return this.waitCounter[step].get();
    }
}