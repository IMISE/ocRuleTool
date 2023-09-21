package imise;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OCReadTest {

    /**
     * Generates a rules.xml from provided Demo_v3.9.xls
     * Test is successful if the generated file matches the solution.xml
     * Displayed metadata at the top is ignored
     */
    @Test
    public void completeTest() throws IOException {

        OcRuleTool ocRead = new OcRuleTool();
        ocRead.infile = "src/test/resources/Demo_v3.9.xls";
        String[] args = {};
        ocRead.start(args);

        File rulesXml = new File("src/test/resources/Demo_v3.9-rules.xml");
        assertTrue(rulesXml.exists());
        String rules = Files.readString(rulesXml.toPath(), Charset.forName("windows-1252"));
        rules = rules.replaceAll("(?s)<!--.*?-->", "");

        File solutionXml = new File("src/test/resources/Demo_v3.9-Test-solution.xml");
        String solution = Files.readString(solutionXml.toPath(), Charset.forName("windows-1252"));
        solution = solution.replaceAll("(?s)<!--.*?-->", "");

        assertEquals(solution, rules);
    }
}
