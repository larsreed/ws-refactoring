package no.support.batch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Superklasse for batchresultater.
 */
public class BatchResult implements ThreadControl {
    /** Tom streng. */
    protected static final String TOM_STRENG= BatchResult.EMPTY_STRING;
    /** Tom streng. */
    private static final String EMPTY_STRING= "";
    /** 1 vente-teller for hvert steg. */
    protected final AtomicInteger[] waitCounter;
    /** 1 OK-teller for hvert steg. */
    protected final AtomicInteger[] total;
    /** 1 feilteller for hvert steg. */
    protected final AtomicInteger[] errCounter;
    /** 1 trådteller for hvert steg. */
    protected final AtomicInteger[] threadCounter;
    /** 1 trådflagg for hvert steg. */
    protected final boolean[] threadStarted;
    /** Array med overskriftene til resultattabellen. */
    protected final String[] resultatArr;
    /** Debugmodus? */
    protected final boolean debug;
    /** Holder data under skriving. */
    protected final StringBuilder contents;
    /** Meldingslager. */
    private final LinkedList<String> messages= new LinkedList<>();
    /** Holder på feltverdier. */
    private final List<String[]> reportData= new LinkedList<>();
    /** Referanser til tabeller. */
    private final List<DocumentTable<String>> tables= new LinkedList<>();
    /** Antall steg. */
    private final int steps;
    /** Holder på data som en ganske vanlig streng. */
    private String data;

    /**
     * Standard constructor.
     * @param step Antall steg i prosessen
     * @param debug Debug på?
     */
    public BatchResult(final int step, final boolean debug) {
        super();
        this.steps = step;
        this.debug= debug;
        this.contents= new StringBuilder();
        this.waitCounter= new AtomicInteger[step];
        this.total = new AtomicInteger[step];
        this.errCounter= new AtomicInteger[step];
        this.threadCounter= new AtomicInteger[step];
        this.threadStarted= new boolean[step];
        for (int i=0; i< step; i++) {
            this.waitCounter[i]= new AtomicInteger();
            this.total[i]= new AtomicInteger();
            this.errCounter[i]= new AtomicInteger();
            this.threadCounter[i]= new AtomicInteger();
        }
        final String xtra= debug? "Ventet" : TOM_STRENG;
        this.resultatArr= new String[] { "Steg", "Antall", "Feil", xtra  };
    }

    /**
     * Lag rapport.
     *
     * @param documentHeaderFooter@throws IOException Uff
     */
    public void report(final DocumentHeaderFooter documentHeaderFooter) throws IOException {
        final String dtForm=
            new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
        addIfDefined(documentHeaderFooter.getOverskrift1());
        addIfDefined(documentHeaderFooter.getOverskrift2());
        addIfDefined(documentHeaderFooter.getOverskrift3());
        addData(); // Legger inn tom linje
        addData("Statusfix", dtForm);
        addData(); // Legger inn tom linje
        addData(); // Legger inn tom linje
        addTable(addResultTable());
        addTable(addMessageTable());
        addData(); // Legger inn tom linje
        addIfDefined(documentHeaderFooter.getUnderskrift1());
        addIfDefined(documentHeaderFooter.getUnderskrift2());
        addIfDefined(documentHeaderFooter.getUnderskrift3());
        this.reportData.forEach(this::writeData);
        this.tables.forEach(table -> table.write(this.contents));
        this.data= this.contents.toString().replaceAll("[ \t]+\n", "\n");
        save(new File(("statusfix."
                + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())
                + ".log")));
    }

    protected DocumentTable<String> addMessageTable() {
        final DocumentTable<String> meldingsTabell =
            new DocumentTable<>("Meldinger", 2);
        int i= 0;
        meldingsTabell.addHeadings(TOM_STRENG, TOM_STRENG);
        for (final String s : this.messages) {
            i++;
            final String[] arr= new String[] {
                TOM_STRENG + i,
                s
            };
            meldingsTabell.addLine(arr);
        }
        return meldingsTabell;
    }

    protected DocumentTable<String> addResultTable() {
        final DocumentTable<String> resultatTabell = new DocumentTable<>("Resultat",
                                                                         this.resultatArr.length);

        resultatTabell.addHeadings(this.resultatArr);
        for (int i=1; i< this.steps; i++) { // Steg 0 rapporteres ikke
            final String steg= i + ". "
                               + "Steg "
                               + i;
            final String[] arr= new String[] {
                steg,
                TOM_STRENG+this.total[i].get(),
                TOM_STRENG+this.errCounter[i].get(),
                this.debug? TOM_STRENG+this.waitCounter[i].get() : TOM_STRENG
            };
            resultatTabell.addLine(arr);
        }
        resultatTabell.addLine(TOM_STRENG, TOM_STRENG, TOM_STRENG, TOM_STRENG);
        addLocalResult(resultatTabell);
        return resultatTabell;
    }

    protected void addLocalResult(final DocumentTable<String> resultatTabell) {
        return;
    }

    /**
     * Tell opp antall totalt.
     *
     * @param steg Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    public int incTotal(final int steg) {
        return this.total[steg].incrementAndGet();
    }

    /**
     * Tell opp antall feil.
     *
     * @param steg Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    public int incErr(final int steg) {
        return this.errCounter[steg].incrementAndGet();
    }

    /**
     * Øk antall tråder med 1.
     *
     * @param step Steg nummer
     * @param caller Hvem ringer
     * @return Antall tråder nå
     */
    @Override
    public int threadsUp(final int step, final Object caller) {
        this.threadCounter[step].incrementAndGet();
        this.threadStarted[step]= true;
        return getThreadCount();
    }

    /**
     * Reduser antall tråder med 1.
     *
     * @param step Steg nummer
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
        int sum=0;
        for (final AtomicInteger i : this.threadCounter) {
            sum+= i.get();
        }
        return sum;
    }

    /**
     * Debugmeldinger.
     *
     * @param info Alt som skal logges
     */
    public synchronized void debug(final Object... info) {
        final String s= concat(" ", info);
        if (!((s == null || s.length()==0 || s.trim().length() == 0))) {
            final Object[] s1= { s };
            if ( this.debug ) {
                System.err.println(concat(" ", s1));
            }
        }
    }

    /**
     * Faktiske meldinger.
     *
     * @param info Alt som skal logges
     */
    public synchronized void log(final Object... info) {
        final String s= concat(" ", info);
        if (!((s == null || s.length()==0 || s.trim().length() == 0))) {
            this.messages.add(s);
        }
        debug(info);
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

    /**
     * Tell opp ventetid.
     *
     * @param steg Hvilket steg
     */
    public void addWait(final int steg) {
        this.waitCounter[steg].incrementAndGet();
    }

    /**
     * Legg til vanlig felt KUN hvis innholdet er non-null/blank.
     *
     * @param data Data
     */
    private void addIfDefined(final String data) {
        if ( data == null || data.length()==0 || data.trim().length() == 0 ) {
            return;
        }
        addData(data);
    }

    /**
     * Legg til et vanlig felt.
     *
     * @param p1 Feltinnhold
     */
    private void addData(final String p1) {
        addData(new String[] { p1 });
    }

    /**
     * Legg til linje med mange felt.
     *
     * @param data Data
     */
    private void addData(final String... data) {
        this.reportData.add(data);
    }

    /**
     * Legg til en tabell.
     *
     * @param table Tabellen
     */
    private void addTable(final DocumentTable<String> table) {
        this.tables.add(table);
    }

    /**
     * Skriv en linje.
     *
     * @param data Innhold
     */
    private void writeData(final String... data) {
        if ( data!=null ) {
            for (final String string : data) {
                this.contents.append(string==null? TOM_STRENG : string)
                             .append('\t');
            }
        }
        this.contents.append('\n');
    }

    /**
     * Skriv dokumentet til gitt fil.
     *
     * @param f Fil vi skal skrive til
     * @throws IOException Ved teknisk leif
     */
    private void save(final File f) throws IOException {
        final OutputStream printStream= new PrintStream(f);
        printStream.write(this.data.getBytes());
        printStream.close();
    }

    /**
     * Slå sammen strenger til én lang streng.
     *
     * @param separator Hvordan delene skal skilles av
     * @param args Alt som skal skjøtes
     * @return Herlig røre (<code>null</code> hvis ingen input)
     */
    private String concat(final String separator, final Object... args) {
        if ( args==null ) return null;
        final StringBuilder buf= new StringBuilder();
        int rest= args.length;
        for (final Object object : args) {
            buf.append(object);
            rest--;
            if ( rest> 0 ) {
                buf.append(separator);
            }
        }
        return buf.toString();
    }

    /**
     * Hent teller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    public int getTotal(final int step) {
        return this.total[step].get();
    }

    /**
     * Hent feilteller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    public int getErr(final int step) {
        return this.errCounter[step].get();
    }

    /**
     * Hent meldinger.
     *
     * @return messages
     * @see #messages
     */
    public synchronized List<String> getMessages() {
        return this.messages;
    }
}
