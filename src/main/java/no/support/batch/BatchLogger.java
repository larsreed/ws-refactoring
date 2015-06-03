package no.support.batch;

import java.util.LinkedList;
import java.util.List;

public class BatchLogger implements SimpleLog {
    /** Debugmodus? */
    private final boolean debug;
    /** Meldingslager. */
    private final LinkedList<String> messages = new LinkedList<>();

    public BatchLogger(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Debugmeldinger.
     *
     * @param info Alt som skal logges
     */
    @Override
    public synchronized void debug(final Object... info) {
        final String s = concat(" ", info);
        if (!((s == null || s.length() == 0 || s.trim().length() == 0))) {
            final Object[] s1 = {s};
            if (this.debug) {
                System.err.println(concat(" ", s1));
            }
        }
    }

    /**
     * Faktiske meldinger.
     *
     * @param info Alt som skal logges
     */
    @Override
    public synchronized void log(final Object... info) {
        final String s = concat(" ", info);
        if (!((s == null || s.length() == 0 || s.trim().length() == 0))) {
            this.messages.add(s);
        }
        debug(info);
    }

    /**
     * Hent meldinger.
     *
     * @return messages
     * @see #messages
     */
    @Override
    public synchronized List<String> getMessages() {
        return this.messages;
    }

    @Override
    public boolean isDebug() {
        return this.debug;
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
}