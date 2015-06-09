package no.support.batch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** Implementasjon av SimpleLog. */
public class BatchLogger implements SimpleLog {

    /** Debugmodus? */
    private final boolean debug;
    /** Meldingslager. */
    private final List<String> messages = new LinkedList<>();

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
        if (!this.debug) return;
        System.err.println(concat(" ", info));
    }

    /**
     * Faktiske meldinger.
     *
     * @param info Alt som skal logges
     */
    @Override
    public synchronized void log(final Object... info) {
        final String s = concat(" ", info);
        if (!s.isEmpty()) this.messages.add(s);
        debug(s);
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
     * @return Herlig røre
     */
    private String concat(final String separator, final Object... args) {
        return args==null? ""
                         : Arrays.stream(args).filter(e->e!=null)
                                   .map(Object::toString).collect(Collectors.joining(separator));
    }
}