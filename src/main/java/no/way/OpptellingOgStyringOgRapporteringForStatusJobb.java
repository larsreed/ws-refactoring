package no.way;

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
 * Holder orden på tellere, trådstyring og rapportering av endelig resultat
 * for kjøring av statusjobben.
 */
public class OpptellingOgStyringOgRapporteringForStatusJobb {
    // Tellerne benytter AtomicInteger for å slippe synkronisering.


    /** Tom streng. */
    private static final String EMPTY_STRING= "";

    /** Antall steg totalt. */
    public static final int ANTALL_STEG= 2+1;

    /** Antall saker berørt. */
    private final AtomicInteger antallSaker= new AtomicInteger(0);
    /** Tom streng. */
    private static final String TOM_STRENG= EMPTY_STRING;

    // Tellerne benytter AtomicInteger for å slippe synkronisering.

    /** 1 vente-teller for hvert steg. */
    private final AtomicInteger[] waitCounter;
    /** 1 OK-teller for hvert steg. */
    private final AtomicInteger[] counter;
    /** 1 feilteller for hvert steg. */
    private final AtomicInteger[] errCounter;
    /** 1 trådteller for hvert steg. */
    private final AtomicInteger[] threadCounter;
    /** 1 trådflagg for hvert steg. */
    private final boolean[] threadStarted;
    /** Meldingslager. */
    private final LinkedList<String> messages= new LinkedList<>();

    /** Holder på feltverdier. */
    private final List<String[]> reportData= new LinkedList<>();

    /** Referanser til tabeller. */
    private final List<DocumentTable<String>> tables= new LinkedList<>();

    /** Array med overskriftene til resultattabellen. */
    private final String[] resultatArr;

    /** Debugmodus? */
    private final boolean debug;

    /** Holder data under skriving. */
    private final StringBuilder contents;

    /** Holder på data som en ganske vanlig streng. */
    private String data;

    /**
     * Implementasjon av tabell med data.
     * @param <T> Datatype
     */
    class DocumentTable<T>  {

        /** Navnet på tabellen. */
        private final String tableName;

        /** Antall kolonner. */
        private final int cols;

        /** Holder data. */
        private final List<T[]> list= new LinkedList<>();

        /** Holder data. */
        private String[] titles;

        /**
         * Default constructor for DataGroup.
         *
         * @param tabName Navn på tabellen
         * @param noOfCols Antall kolonner i tabellen
         */
        public DocumentTable(final String tabName,
                             final int noOfCols) {
            super();
            this.tableName= tabName;
            this.cols= noOfCols;
            this.titles= new String[noOfCols];
        }

        /**
         * Legg til 1 datarad.
         *
         * @param data Forekomstene
         * @return this
         */
        public DocumentTable<T> addLine(final T... data) {
            if ( data==null ) return this;
            if ( data.length != this.cols ) throw new IllegalArgumentException(data.length + "!=" +this.cols);
            this.list.add(data);
            return this;
        }

        /**
         * Legg til mange datarader.
         *
         * @param data En liste med 1 rad påtre nivå, feltene på indre nivå
         * @return this
         */
        public DocumentTable<T> addLines(final List<T[]> data) {
            if ( data== null ) return this;
            for (final T[] row : data) {
                if ( row==null ) return this;
                if ( row.length != this.cols ) {
                    throw new IllegalArgumentException(row.length + "!=" +this.cols);
                }
                this.list.add(row);
            }
            return this;
        }

        /**
         * Legg til overskrifter.
         *
         * @param data Overskriftene
         * @return this
         */
        public DocumentTable<T> addHeadings(final String... data) {
            if ( data==null ) return this;
            if ( data.length != this.cols) {
                throw new IllegalArgumentException(data.length + "!=" +this.cols);
            }
            this.titles = data;
            return this;
        }

        /** Formater data for utskrift innenfor en eksisterende kontekst.  */
        public void write() {
            writeTableHead(this.tableName, this.cols, this.list.size(), this.titles);
            this.list.stream().forEach(this::writeTableRow);
        }


        /**
         * Skriv starten av en tabell.
         *
         * @param name Feltnavn
         * @param cols Antall kolonner i tabellen
         * @param rows Antall rader i tabellen
         * @param pTitles Overskriftene
         */
        private void writeTableHead(final String name,
                                    final int cols, final int rows,
                                    final String... pTitles) {
            final StringBuilder buffer= OpptellingOgStyringOgRapporteringForStatusJobb.this.contents;
            buffer.append("\n");
            for (final String title : pTitles) {
                buffer.append(title)
                      .append("\t");
            }
            buffer.append("\n");
        }

        /**
         * Skriv rad i en tabell.
         * @param data Kolonnene
         */
        private void writeTableRow(final Object... data) {
            if ( data==null ) return;
            final StringBuilder buffer= OpptellingOgStyringOgRapporteringForStatusJobb.this.contents;
            for (final Object s : data) {
                buffer.append(s)
                      .append('\t');
            }
            buffer.append('\n');
        }
    }

    /**
     * Default constructor.
     * @param debug Debugmodus?
     */
    public OpptellingOgStyringOgRapporteringForStatusJobb(final boolean debug) {
        super();
        this.debug= debug;
        final int step= ANTALL_STEG;
        this.waitCounter= new AtomicInteger[step];
        this.counter= new AtomicInteger[step];
        this.errCounter= new AtomicInteger[step];
        this.threadCounter= new AtomicInteger[step];
        this.threadStarted= new boolean[step];
        this.contents= new StringBuilder();
        for (int i=0; i< step; i++) {
            this.waitCounter[i]= new AtomicInteger();
            this.counter[i]= new AtomicInteger();
            this.errCounter[i]= new AtomicInteger();
            this.threadCounter[i]= new AtomicInteger();
        }
        final String xtra= debug? "Ventet" : TOM_STRENG;
        this.resultatArr= new String[] { "Steg", "Antall", "Feil", xtra  };
    }


    /**
     * Lag rapport.
     * @param overskrift1 Topptekst
     * @param overskrift2 Topptekst
     * @param overskrift3 Topptekst
     * @param underskrift1 Bunntekst
     * @param underskrift2 Bunntekst
     * @param underskrift3 Bunntekst
     * @throws IOException Uff
     */
    public void report(final String overskrift1,
                       final String overskrift2,
                       final String overskrift3,
                       final String underskrift1,
                       final String underskrift2,
                       final String underskrift3) throws IOException {
        final String dtForm=
            new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
        addIfDefined(overskrift1);
        addIfDefined(overskrift2);
        addIfDefined(overskrift3);
        addData(); // Legger inn tom linje
        addData("Statusfix", dtForm);
        addData(); // Legger inn tom linje
        addData(); // Legger inn tom linje
        final DocumentTable<String> resultatTabell = new DocumentTable<>("Resultat",
                                                                         this.resultatArr.length);

        resultatTabell.addHeadings(this.resultatArr);
        for (int i=1; i< ANTALL_STEG; i++) { // Steg 0 rapporteres ikke
            final String steg= i + ". "
                               + "Steg "
                               + i;
            final String[] arr= new String[] {
                steg,
                TOM_STRENG+this.counter[i].get(),
                TOM_STRENG+this.errCounter[i].get(),
                this.debug? TOM_STRENG+this.waitCounter[i].get() : TOM_STRENG
            };
            resultatTabell.addLine(arr);
        }
        resultatTabell.addLine(TOM_STRENG, TOM_STRENG, TOM_STRENG, TOM_STRENG);
        resultatTabell.addLine("Antall saker",
                               TOM_STRENG + this.antallSaker,
                               TOM_STRENG,
                               TOM_STRENG);
        addTable(resultatTabell);
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
        addTable(meldingsTabell);
        addData(); // Legger inn tom linje
        addIfDefined(underskrift1);
        addIfDefined(underskrift2);
        addIfDefined(underskrift3);
        this.reportData.forEach(this::writeData);
        this.tables.forEach(DocumentTable<String>::write);
        this.data= this.contents.toString().replaceAll("[ \t]+\n", "\n");
        save(new File(("statusfix."
                + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())
                + ".log")));
    }

    /**
     * Oppdater antall saker berørt.
     *
     * @param antSaker Antall nye saker
     * @return Antall saker totalt
     */
    public int addSaker(final int antSaker) {
        return this.antallSaker.addAndGet(antSaker);
    }

    /**
     * Tell opp antall OK.
     *
     * @param steg Hvilket steg
     * @param pAntall Antall nye
     * @return Antall totalt etter oppdatering
     */
    public int addOk(final int steg, final int pAntall) {
        return this.counter[steg].addAndGet(pAntall);
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
    public int threadsDown(final int step, final Object caller) {
        this.threadCounter[step].decrementAndGet();
        return getThreadCount();
    }

    /**
     * Returnerer antall aktive trår totalt.
     *
     * @return Barnetråder
     */
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
    public int getOk(final int step) {
        return this.counter[step].get();
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
     * Hent antall saker.
     *
     * @return Antall
     */
    public int getSaker() {
        return this.antallSaker.get();
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
