package no.support.batch.factory;

import java.util.Optional;

import no.support.batch.BatchResult;
import no.support.batch.purring.PurreJobbResultat;

/**
 * Factory for resultatklasser.
 */
public class ResultFactory {
    public static <T extends BatchResult> Optional<T>  createResult(final Class<? extends T> clazz, final boolean debug) {
        if ( clazz.isAssignableFrom(PurreJobbResultat.class)) {
            //noinspection unchecked
            return Optional.of((T) new PurreJobbResultat(debug));
        }
        return Optional.empty();
    }
}
