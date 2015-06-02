package no.support.batch;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementasjon av tabell med data.
 * @param <T> Datatype
 */
public class DocumentTable<T> {

    /** Navnet på tabellen. */
    private final String tableName;

    /** Antall kolonner. */
    private final int cols;

    /** Holder data. */
    private final List<T[]> list= new LinkedList<>();

    /** Holder data. */
    private String[] titles;

    /**
     * Default constructor.
     *
     * @param tabName Navn på tabellen
     * @param noOfCols Antall kolonner i tabellen
     */
    public DocumentTable(final String tabName, final int noOfCols) {
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

    /**
     * Formater data for utskrift innenfor en eksisterende kontekst.
     * @param contents Buffer vi skriver til
     */
    public void write(final StringBuilder contents) {
        writeTableHead(this.tableName, this.cols, this.list.size(),
                       contents, this.titles);
        this.list.forEach(data -> writeTableRow(contents, data));
    }


    /**
     * Skriv starten av en tabell.
     *
     * @param name Feltnavn
     * @param cols Antall kolonner i tabellen
     * @param rows Antall rader i tabellen
     * @param buffer Buffer vi skriver til
     * @param pTitles Overskriftene
     */
    private void writeTableHead(final String name,
                                final int cols, final int rows, final StringBuilder buffer,
                                final String... pTitles) {
        buffer.append("\n");
        for (final String title : pTitles) {
            buffer.append(title)
                  .append("\t");
        }
        buffer.append("\n");
    }

    /**
     * Skriv rad i en tabell.
     * @param buffer Buffer vi skriver til
     * @param data Kolonnene
     */
    private void writeTableRow(final StringBuilder buffer, final Object... data) {
        if ( data==null ) return;
        for (final Object s : data) {
            buffer.append(s)
                  .append('\t');
        }
        buffer.append('\n');
    }
}
