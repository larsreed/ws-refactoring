package no.support.batch;

/**
 * Topp- og bunntekst i rapporter.
 */
public class DocumentHeaderFooter {

    private final boolean useHeader;
    private final boolean useFooter;

    private final String overskrift1;
    private final String overskrift2;
    private final String overskrift3;
    private final String underskrift1;
    private final String underskrift2;
    private final String underskrift3;

    public DocumentHeaderFooter(final boolean useHeader, final boolean useFooter,
                                final String overskrift1, final String overskrift2,
                                final String overskrift3, final String underskrift1,
                                final String underskrift2, final String underskrift3) {
        this.useHeader = useHeader;
        this.useFooter = useFooter;
        this.overskrift1 = overskrift1;
        this.overskrift2 = overskrift2;
        this.overskrift3 = overskrift3;
        this.underskrift1 = underskrift1;
        this.underskrift2 = underskrift2;
        this.underskrift3 = underskrift3;
    }

    public String getOverskrift1() {
        return overskrift1;
    }

    public String getOverskrift2() {
        return overskrift2;
    }

    public String getOverskrift3() {
        return overskrift3;
    }

    public String getUnderskrift1() {
        return underskrift1;
    }

    public String getUnderskrift2() {
        return underskrift2;
    }

    public String getUnderskrift3() {
        return underskrift3;
    }

    /** Bruke topptekst? */
    public boolean isUseHeader() {
        return useHeader;
    }

    /** Bruke bunntekst? */
    public boolean isUseFooter() {
        return useFooter;
    }
}
