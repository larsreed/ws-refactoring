package no.support.batch.factory;

import no.support.batch.BatchResult;
import no.support.batch.purring.PurreJobbResultat;

/**
 * Factory for resultatklasser.
 */
public class ResultFactory {
    public static <T extends BatchResult> T  createResult(final Class<? extends T> clazz, final boolean debug) {
        if ( clazz.isAssignableFrom(PurreJobbResultat.class)) {
            return (T) new PurreJobbResultat(debug);
        }
        return null;
    }
}
