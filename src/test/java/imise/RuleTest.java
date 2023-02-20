package imise;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest {

    @Test
    void testExpr() throws Exception {

        Rule testRuleBefore = new Rule(Rule.RuleType.BEFORE, "I_DEMOR_UNITMEDIA", "", "", "50", "250", "", 0, "");
        String testBefore = testRuleBefore.expr("targetOID");
        assertEquals("I_DEMOR_UNITMEDIA lt targetOID or I_DEMOR_UNITMEDIA - targetOID lt 50 and I_DEMOR_UNITMEDIA - targetOID gt 250", testBefore);

        Rule testRuleAfter = new Rule(Rule.RuleType.AFTER, "I_DEMOR_UNITMEDIA", "", "", "50", "250", "", 0, "");
        String testAfter = testRuleAfter.expr("targetOID");
        assertEquals("targetOID lt I_DEMOR_UNITMEDIA or targetOID - I_DEMOR_UNITMEDIA lt 50 and targetOID - I_DEMOR_UNITMEDIA gt 250", testAfter);

        Rule testRuleAssert = new Rule(Rule.RuleType.ASSERT, "I_DEMOR_UNITMEDIA", "", "6", "", "", "", 6, "");
        String testAssert = testRuleAssert.expr("targetOID");
        assertEquals("I_DEMOR_UNITMEDIA 6", testAssert);

        Rule testRuleRequired = new Rule(Rule.RuleType.REQUIRED, "I_DEMOR_UNITMEDIA", "", "654", "50", "250", "", 6, "");
        String testRequired = testRuleRequired.expr("targetOID");
        assertEquals("targetOID lt 50 and targetOID gt 250 and I_DEMOR_UNITMEDIA 654", testRequired);

        Rule testRuleExpression = new Rule(Rule.RuleType.EXPRESSION, "", "not example", "", "", "", "", 0, "");
        String testExpression = testRuleExpression.expr("targetOID");
        assertEquals("example", testExpression);

        Rule testRuleEmpty = new Rule(Rule.RuleType.EMPTY, "I_DEMOR_UNITMEDIA", "", "low", "", "", "", 0, "");
        String testEmpty = testRuleEmpty.expr("targetOID");
        assertEquals("targetOID ne \"\" and I_DEMOR_UNITMEDIA low", testEmpty);

        Rule testRuleRange = new Rule(Rule.RuleType.RANGE, "I_DEMOR_UNITMEDIA", "", "45", "30", "60", "", 0, "");
        String testRange = testRuleRange.expr("targetOID");
        assertEquals("I_DEMOR_UNITMEDIA eq \"45\" and ( targetOID lt 30 or targetOID gt 60 )", testRange);
    }
}