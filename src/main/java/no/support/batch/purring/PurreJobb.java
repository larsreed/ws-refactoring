package no.support.batch.purring;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * Jobb som på magisk vis på purrer på saker som viser manglende fremdrift.
 * Forretningslogikken er utelatt her (den kan sikkert legges på med run time
 * weaving av aspekter hvis vi trenger den :-).
 *
 * Jobben har en instans av {@link OpptellingOgStyringOgRapporteringForStatusJobb}
 * som benyttes for synkronisering mellom tråder, rapportering av resultat osv.
 * Gisp.
 */
public class PurreJobb extends Thread {

    /** Antall tråder. */
    private static final int THREADS= 24;
    /** Starter saksnr. */
    private static final int FIRST_NO= 99999;
    /** Køstørrelse. */
    private static final int QSIZE= 5;
    /** Hvor lenge skal vi sove? */
    private static final int SLEEP= 100;
    /** Ventetid på køoperasjoner (ms). */
    private static final int Q_WAIT_READ= 10*SLEEP;
    /** Ventetid på køoperasjoner (ms). */
    private static final int Q_WAIT_WRITE= 6*Q_WAIT_READ;
    /** Hvor ofte skal vi feile? */
    private static final int ERR_FREQ= 3;
    /** Antall produsert av hver produsent. */
    private static final int MAXPROD= 100;

    /** Kommunikasjon. */
    static BlockingQueue<Sak> q= new LinkedBlockingQueue<>(QSIZE);
    /** Teller. */
    static AtomicInteger neste= new AtomicInteger(FIRST_NO);

    /** Det er denne vi jobber med... */
    OpptellingOgStyringOgRapporteringForStatusJobb resultat;
    /** Det er denne vi jobber med... */
    OpptellingOgStyringOgRapporteringForStatusJobb jobCtrl;

    /** Ha med debug? */
    private final boolean withDebug;

    /** En sak...*/
    class Sak {

        /** ID. */
        private final int sakNr;

        /**
         * Default constructor for PurreJobb.Sak.
         * @param sakNr Saksnummer
         */
        Sak(final int sakNr) {
            this.sakNr= sakNr;
        }

        /**
         * Returnerer saksnr.
         *
         * @return sakNr
         * @see #sakNr
         */
        int getSakNr() {
            return this.sakNr;
        }

        @Override
        public String toString() {
            return "Sak#" + this.sakNr;
        }
    }

    /** Henter saker. */
    class Producer implements Runnable {

        /** Jobbstyring. */
        private final OpptellingOgStyringOgRapporteringForStatusJobb jobControl;
        /** Rapportering. */
        private final OpptellingOgStyringOgRapporteringForStatusJobb result;
        /** Hvilket steg er dette? */
        private final int stepNo;

        /**
         * Default constructor.
         *
         * @param stepNo Steg nummer
         * @param jobCtrl Jobbkontroll
         * @param result Tellere
         */
        Producer(final int stepNo,
                 final OpptellingOgStyringOgRapporteringForStatusJobb jobCtrl,
                 final OpptellingOgStyringOgRapporteringForStatusJobb result) {
            this.stepNo= stepNo;
            this.jobControl= jobCtrl;
            this.result= result;
        }

        @Override
        public void run() {
            int no= MAXPROD;
            this.jobControl.threadsUp(this.stepNo, this);
            while (no-->0) {
                this.result.incTotal(this.stepNo);
                try {
                    final Sak sak= new Sak(neste.incrementAndGet());
                    if ( !q.offer(sak, Q_WAIT_WRITE, TimeUnit.MILLISECONDS) ) {
                        this.result.incErr(this.stepNo);
                        this.result.log("Ville ikke inn i køen", sak);
                        if ( this.jobControl.stepDone(this.stepNo+1) ) {
                            // Ingen konsumenter :O
                            break;
                        }
                    }
                }
                catch (final InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            this.jobControl.threadsDown(this.stepNo, this);
        }
    }

    /** Behandler saker. */
    class Consumer implements Runnable {

        /** Jobbstyring. */
        private final OpptellingOgStyringOgRapporteringForStatusJobb jobControl;
        /** Tellere. */
        private final OpptellingOgStyringOgRapporteringForStatusJobb result;
        /** Hvilket steg er dette? */
        private final int stepNo;

        /**
         * Default constructor.
         *
         * @param stepNo Steg nummer
         * @param jobCtrl Jobbkontroll
         * @param result Tellere
         */
        Consumer(final int stepNo,
                 final OpptellingOgStyringOgRapporteringForStatusJobb jobCtrl,
                 final OpptellingOgStyringOgRapporteringForStatusJobb result) {
            this.stepNo= stepNo;
            this.jobControl= jobCtrl;
            this.result= result;
        }

        @Override
        public void run() {
            this.jobControl.threadsUp(this.stepNo, this);
            while (true) {
                try {
                    final Sak sak= q.poll(Q_WAIT_READ, TimeUnit.MILLISECONDS);
                    if ( sak!=null ) {
                        this.result.incTotal(this.stepNo);
                        if (( sak.getSakNr() % ERR_FREQ) ==0) {
                            this.result.incErr(this.stepNo);
                            this.result.log("Feilet stygt", sak);
                        }
                        else {
                            this.result.addSaker(1);
                        }
                    }
                    else {
                        if ( this.jobControl.stepDone(this.stepNo-1) ) {
                            break; // Produksjon ferdig
                        }
                        this.result.addWait(this.stepNo);
                    }
                    sleep(SLEEP);
                }
                catch (final InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            this.jobControl.threadsDown(this.stepNo, this);
        }
    }

    /**
     * Default constructor for PurreJobb.
     *
     * @param withDebug Kjøre med debug
     */
    public PurreJobb(final boolean withDebug) {
        this.withDebug= withDebug;
    }

    /**
     * Hovedprogrammet.
     *
     * @param args Ignorert kommandolinje
     */
    public static void main(final String[] args) {
        new PurreJobb(true).run();
    }

    /**
     * Selve hjertet.
     */
    @Override
    public void run() {
        final OpptellingOgStyringOgRapporteringForStatusJobb ctrl=
            new OpptellingOgStyringOgRapporteringForStatusJobb(this.withDebug);
        this.jobCtrl= ctrl;
        this.resultat= ctrl;
        this.jobCtrl.threadsUp(0, this);
        for (int i=0; i<THREADS/3; i++) {
            new Thread(new Producer(1, this.jobCtrl, this.resultat)).start();
            new Thread(new Consumer(2, this.jobCtrl, this.resultat)).start();
            new Thread(new Consumer(2, this.jobCtrl, this.resultat)).start();
            try {
                sleep(SLEEP/10);
            }
            catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.jobCtrl.threadsDown(0, this);
        final int barn= this.jobCtrl.getThreadCount();
        while ( barn> 0 ) {
            if ( this.jobCtrl.stepDone(2) ) {
                break;
            }
            this.resultat.debug("Venter på", barn, "barn...");
            try {
                sleep(2*SLEEP);
            }
            catch (final InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            this.resultat.report(this.toString(), new Date().toString(),
                        "lre", "", "", "");
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        this.resultat.debug("Ferdig!");
    }
}
