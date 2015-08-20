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

    public static class DocumentHeaderFooterBuilder {
        private boolean useHeader = false;
        private boolean useFooter = false;
        private String headerLeft = "";
        private String headerCenter = "";
        private String headerRight = "";
        private String footerLeft = "";
        private String footerCenter = "";
        private String footerRight = "";

        public DocumentHeaderFooterBuilder useHeader(final boolean useHeader) {
            this.useHeader = useHeader;
            return this;
        }

        public DocumentHeaderFooterBuilder useFooter(final boolean useFooter) {
            this.useFooter = useFooter;
            return this;
        }

        public DocumentHeaderFooterBuilder headerLeft(final String headerLeft) {
            this.headerLeft = headerLeft;
            return this;
        }

        public DocumentHeaderFooterBuilder headerCenter(final String headerCenter) {
            this.headerCenter = headerCenter;
            return this;
        }

        public DocumentHeaderFooterBuilder headerRight(final String headerRight) {
            this.headerRight = headerRight;
            return this;
        }

        public DocumentHeaderFooterBuilder footerLeft(final String footerLeft) {
            this.footerLeft = footerLeft;
            return this;
        }

        public DocumentHeaderFooterBuilder footerCenter(final String footerCenter) {
            this.footerCenter = footerCenter;
            return this;
        }

        public DocumentHeaderFooterBuilder footerRight(final String footerRight) {
            this.footerRight = footerRight;
            return this;
        }

        public DocumentHeaderFooter build() {
            return new DocumentHeaderFooter(useHeader, useFooter, headerLeft, headerCenter, headerRight,
                                            footerLeft, footerCenter, footerRight);
        }
    }
}
