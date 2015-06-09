package no.support.batch;

/**
 * Interface for telling.
 */
interface ResultCounter {
    /** Øk antall totalt for steg N. */
    int incTotal(int step);

    /** Øk antall feil for steg N. */
    int incErr(int step);

    /** Øk antall ventet for steg N. */
    void incWait(int step);

    /** Hent antall totalt for steg N. */
    int getTotal(int step);

    /** Hent antall feil for steg N. */
    int getErr(int step);

    /** Hent antall ventet for steg N. */
    int getWait(int step);
}
