package no.support.batch;

import java.util.List;

/**
 * Logginterface.
 */
interface SimpleLog {
    /** Skriv debuginfo hvis debug er p�. */
    void debug(Object... info);

    /** Lagre logginfo. */
    void log(Object... info);

    /** Hent ut loggmeldinger. */
    List<String> getMessages();

    /** Sjekk om debug er p�. */
    boolean isDebug();
}
