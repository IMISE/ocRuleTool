package imise;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void update() {
        Item item = new Item("Dose Drug A", "SE_EXCLUSION", "DEMO_CRF", "TREAT",
                "MED_A", "Treatment", "Dose of drug A", 0);
        assertTrue(item.update("NEW_ITEM_OID", "TREAT"));
        assertTrue(item.update("MED_A", "CONTR"));
        assertFalse(item.update("MED_A", "CONTR"));
    }

    @Test
    void getGroupOID() {
        Item item = new Item("", "", "", "TREAT", "", "", "", 0);
        String testTrue = item.getGroupOID(true);
        String testFalse = item.getGroupOID(false);
        assertEquals("TREAT[1]", testTrue);
        assertEquals("TREAT", testFalse);
    }

    @Test
    void getFullOID() {
        Item item = new Item("Dose Drug A", "SE_EXCLUSION", "DEMO_CRF", "TREAT",
                "MED_A", "Treatment", "Dose of drug A", 0);
        String test = item.getFullOID(true);
        assertEquals("SE_EXCLUSION.DEMO_CRF.TREAT[1].MED_A", test);
    }
}