package no.way;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Full test...
 */
public class PurreJobbSystemTest {

    /** Testobjekt. */
    private static OpptellingOgStyringOgRapporteringForStatusJobb res;

    /**
     * Kjør jobben.
     */
    @BeforeClass
    public static void setUpClass() {
        final PurreJobb purreJobb= new PurreJobb(false);
        purreJobb.run();
        res= purreJobb.resultat;
    }

    @Test
    public void antall()  {
        assertEquals(800, res.getOk(1));
        assertEquals(800, res.getOk(2));
        assertEquals(0, res.getErr(1));
        assertEquals(266, res.getErr(2));
        assertEquals(800-266, res.getSaker());
    }

    @Test
    public void saker()  {
        final List<String> list= res.getMessages();
        assertEquals(266, list.size());
    }

    @Test
    public void traader()  {
        assertEquals(0, res.getThreadCount());
    }
}
