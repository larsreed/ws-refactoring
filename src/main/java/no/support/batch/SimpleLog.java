package no.support.batch;

import java.util.List;

/**
 * Created by larsr_000 on 03.06.2015.
 */
public interface SimpleLog {
    void debug(Object... info);

    void log(Object... info);

    List<String> getMessages();

    boolean isDebug();
}
