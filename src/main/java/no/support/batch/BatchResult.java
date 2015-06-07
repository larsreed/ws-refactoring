package no.support.batch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Superklasse for batchresultater.
 */
public class BatchResult implements ThreadControl, ResultCounter, SimpleLog {
    /** Tom streng. */
    public static final String EMPTY_STRING= "";
    public static final char FIELD_SEP = '\t';
    public static final char LINE_SEP = '\n';
    /** Array med overskriftene til resultattabellen. */
    protected final String[] resultatArr;
    /** Holder data under skriving. */
    protected final StringBuilder contents;
    protected final ThreadControl batchControl;
    protected final ResultCounter batchCounter;
    protected final SimpleLog batchLogger;
    /** Holder på feltverdier. */
    private final List<String[]> reportData= new LinkedList<>();
    /** Referanser til tabeller. */
    private final List<DocumentTable<String>> tables= new LinkedList<>();
    /** Antall steg. */
    private final int steps;
    /** Holder på data som en ganske vanlig streng. */
    private String data;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /** Resultatfil. */
    private String fileName = "statusfix."
                              + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())
                              + ".log";

    /**
     * Standard constructor.
     * @param step Antall steg i prosessen
     * @param debug Debug på?
     * @param batchLogger Logger
     * @param batchCounter Teller
     * @param batchControl Kontrollmekanisme
     */
    public BatchResult(final int step, final boolean debug, final BatchLogger batchLogger,
                       final BatchCounter batchCounter, final BatchControl batchControl) {
        super();
        this.steps = step;
        this.batchLogger = batchLogger;
        this.contents= new StringBuilder();
        this.batchCounter = batchCounter;
        this.batchControl = batchControl;
        final String xtra= debug? "Ventet" : BatchResult.EMPTY_STRING;
        this.resultatArr= new String[] { "Steg", "Antall", "Feil", xtra  };
    }

    /**
     * Lag rapport.
     *
     * @param documentHeaderFooter
     * @throws IOException Uff
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
        final File resultFile = new File(fileName);
        save(resultFile);
    }

    protected DocumentTable<String> addMessageTable() {
        final DocumentTable<String> meldingsTabell =
            new DocumentTable<>("Meldinger", 2);
        int i= 0;
        meldingsTabell.addHeadings(BatchResult.EMPTY_STRING, BatchResult.EMPTY_STRING);
        for (final String s : this.batchLogger.getMessages()) {
            i++;
            final String[] arr= new String[] {
                BatchResult.EMPTY_STRING + i,
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
                BatchResult.EMPTY_STRING + this.batchCounter.getTotal(i),
                BatchResult.EMPTY_STRING + this.batchCounter.getErr(i),
                this.batchLogger.isDebug() ? BatchResult.EMPTY_STRING + this.batchCounter.getWait(i) : BatchResult.EMPTY_STRING
            };
            resultatTabell.addLine(arr);
        }
        resultatTabell.addLine(BatchResult.EMPTY_STRING, BatchResult.EMPTY_STRING,
                               BatchResult.EMPTY_STRING, BatchResult.EMPTY_STRING);
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
    @Override
    public int incTotal(final int steg) {
        return batchCounter.incTotal(steg);
    }

    /**
     * Tell opp antall feil.
     *
     * @param steg Hvilket steg
     * @return Antall totalt etter oppdatering
     */
    @Override
    public int incErr(final int steg) {
        return batchCounter.incErr(steg);
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
        return batchControl.threadsUp(step, caller);
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
        return batchControl.threadsDown(step, caller);
    }

    /**
     * Returnerer antall aktive trår totalt.
     *
     * @return Barnetråder
     */
    @Override
    public int getThreadCount() {
        return batchControl.getThreadCount();
    }

    /**
     * Debugmeldinger.
     *
     * @param info Alt som skal logges
     */
    @Override
    public synchronized void debug(final Object... info) {
        batchLogger.debug(info);
    }

    /**
     * Faktiske meldinger.
     *
     * @param info Alt som skal logges
     */
    @Override
    public synchronized void log(final Object... info) {
        batchLogger.log(info);
    }

    /**
     * Sjekk om forrige steg er ferdig.
     *
     * @param lastStep Forrige steg
     * @return <code>true</code> hvis forrige steg er ferdig
     */
    @Override
    public synchronized boolean stepDone(final int lastStep) {
        return batchControl.stepDone(lastStep);
    }

    /**
     * Tell opp ventetid.
     *
     * @param steg Hvilket steg
     */
    @Override
    public void addWait(final int steg) {
        batchCounter.addWait(steg);
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
                this.contents.append(string==null? BatchResult.EMPTY_STRING : string)
                             .append(FIELD_SEP);
            }
        }
        this.contents.append(LINE_SEP);
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
     * Hent teller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    @Override
    public int getTotal(final int step) {
        return batchCounter.getTotal(step);
    }

    /**
     * Hent feilteller.
     *
     * @param step Steg nummer
     * @return Antall
     */
    @Override
    public int getErr(final int step) {
        return batchCounter.getErr(step);
    }

    @Override
    public int getWait(final int step) {
        return batchCounter.getWait(step);
    }

    /**
     * Hent meldinger.
     *
     * @return messages
     */
    @Override
    public synchronized List<String> getMessages() {
        return batchLogger.getMessages();
    }

    @Override
    public boolean isDebug() {
        return this.batchLogger.isDebug();
    }
}
