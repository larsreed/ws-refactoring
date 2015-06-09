package no.support.batch;

/**
 * Topp- og bunntekst i rapporter (immutable).
 */
public class DocumentHeaderFooter {

    private final boolean useHeader;
    private final boolean useFooter;

    private final String headerLeft;
    private final String headerCenter;
    private final String headerRight;
    private final String footerLeft;
    private final String footerCenter;
    private final String footerRight;

    public DocumentHeaderFooter(final boolean useHeader, final boolean useFooter,
                                final String headerLeft, final String headerCenter,
                                final String headerRight, final String footerLeft,
                                final String footerCenter, final String footerRight) {
        this.useHeader = useHeader;
        this.useFooter = useFooter;
        this.headerLeft = headerLeft;
        this.headerCenter = headerCenter;
        this.headerRight = headerRight;
        this.footerLeft = footerLeft;
        this.footerCenter = footerCenter;
        this.footerRight = footerRight;
    }

    public String getHeaderLeft() {
        return this.headerLeft;
    }

    public String getHeaderCenter() {
        return this.headerCenter;
    }

    public String getHeaderRight() {
        return this.headerRight;
    }

    public String getFooterLeft() {
        return this.footerLeft;
    }

    public String getFooterCenter() {
        return this.footerCenter;
    }

    public String getFooterRight() {
        return this.footerRight;
    }

    /** Bruke topptekst? */
    public boolean useHeader() {
        return this.useHeader;
    }

    /** Bruke bunntekst? */
    public boolean useFooter() {
        return this.useFooter;
    }
}
