package imise;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TargetTest {

    @Test
    void appendRuleDefs() throws Exception {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element ruleImport = doc.createElement("RuleImport");
        doc.appendChild(ruleImport);

        List<Rule> rules = new LinkedList<>();
        Rule testRuleBefore = new Rule(Rule.RuleType.BEFORE, "I_DEMOR_UNITMEDIA", "", "7", "", "", "Bitte mindestens ein Medikament angeben.", 0, "");
        Rule testRuleAfter = new Rule(Rule.RuleType.AFTER, "I_DEMOR_UNITMEDIA", "", "8", "", "", "muss zwischen 10-100 liegen.", 0, "");
        Rule testRuleAssert = new Rule(Rule.RuleType.ASSERT, "I_DEMOR_UNITMEDIA", "", "9", "", "", "OP Termin ist Pflicht (wie Required).", 0, "");
        rules.add(testRuleBefore);
        rules.add(testRuleAfter);
        rules.add(testRuleAssert);

        HashMap<String, Item> items = new HashMap<>();

        Item item = new Item("operation", "SE_EXCLUSION", "F_DEMORULECRF", "UNGROUPED",
                "OP", "Ungrouped", "Demo exclusion criteria", 4);
        Log log = new Log();
        String encoding="ISO-8859-1";

        Target target = new Target(items, item, log, encoding);
        target.ruleList.add(testRuleBefore);
        target.ruleList.add(testRuleAfter);
        target.ruleList.add(testRuleAssert);

        assertEquals(3, target.appendRuleDefs(ruleImport));
    }

    @Test
    void appendRuleAssignment() throws ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element ruleImport = doc.createElement("RuleImport");
        doc.appendChild(ruleImport);

        List<Rule> rules = new LinkedList<>();
        Rule testRuleBefore = new Rule(Rule.RuleType.BEFORE, "I_DEMOR_UNITMEDIA", "", "7", "", "", "Bitte mindestens ein Medikament angeben.", 0, "");
        Rule testRuleAfter = new Rule(Rule.RuleType.AFTER, "I_DEMOR_UNITMEDIA", "", "8", "", "", "muss zwischen 10-100 liegen.", 0, "");
        Rule testRuleAssert = new Rule(Rule.RuleType.ASSERT, "I_DEMOR_UNITMEDIA", "", "9", "", "", "OP Termin ist Pflicht (wie Required).", 0, "");
        rules.add(testRuleBefore);
        rules.add(testRuleAfter);
        rules.add(testRuleAssert);

        HashMap<String, Item> items = new HashMap<>();

        Item item = new Item("operation", "SE_EXCLUSION", "F_DEMORULECRF", "UNGROUPED",
                "OP", "Ungrouped", "Demo exclusion criteria", 4);
        Log log = new Log();
        String encoding="ISO-8859-1";

        Target target = new Target(items, item, log, encoding);
        target.ruleList.add(testRuleBefore);
        target.ruleList.add(testRuleAfter);
        target.ruleList.add(testRuleAssert);

        target.appendRuleAssignment(ruleImport);
        System.out.println(target);

    }
}