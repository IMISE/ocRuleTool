package imise;

/**
 * Rules provide a flexible and powerful way for doing advanced types of validation within the OpenClinica EDC system.
 * OpenClinica’s Rules module has been designed to be flexible, and makes it easy to define, run, and reuse Rules.
 * See more at: https://docs.openclinica.com/3-1/rules/rules-creating-rules/
 */
class Rule {

	/**
	 * used to point at current line of the Excel sheet
	 */
	int line;

	/**
	 * Comparison OID in accordance with OpenClinica
	 */
	String compOID;

	/**
	 * Comparison expression, e.g "eq 0" or "ne 3"
	 */
	private String val;

	/**
	 * Minimal value, numerical
	 */
	String min;

	/**
	 * Maximum value, numerical
	 */
	String max;

	/**
	 * Column RULE_ERROR_MESSAGE, $n is replaced by DESCRIPTION_LABEL of ITEM_NAME from the sheet ITEMS
	 */
	String msg;

	/**
	 * false if expression starts with not
	 */
	String eval;

	/**
	 * Free expression in OpenClinica syntax
	 * Note: put group prefix before ITEM and in parenthesis to set values
	 */
	String expression;

	/**
	 * AFTER: Date has to be at least "min" after or at the most "max" (days) after "comp"
	 * BEFORE: Date has to be at least "min" before ar at the most "max" (days) before "comp"
	 * Note: Calculating dates is not officially supported with OpenClinica 3.0
	 * ASSERT:
	 * REQUIRED: Value must not be empty if comp has val, without comp/val not much use
	 * EMPTY:
	 * EXPRESSION:
	 * RANGE:
	 */
	enum RuleType { AFTER, BEFORE, ASSERT, REQUIRED, EMPTY, EXPRESSION, RANGE };

	RuleType ruleType;

	String email;

	/**
	 * Generic Constructor
	 * @param ruleType Rule type
	 * @param compOID Comparison OID
	 * @param expression Expression
	 * @param val Value
	 * @param min Minimum
	 * @param max Maximum
	 * @param msg Message
	 * @param rowNum Row Number
	 * @param email Email
	 */
	Rule(RuleType ruleType,
			String compOID,
			String expression,
			String val, String min, String max,String msg,int rowNum,String email) {
		this.ruleType = ruleType;
		this.compOID = compOID;
		this.expression = expression;
		this.val = val;
		this.min = min;
		this.max = max;
		this.msg = msg;
		this.email = email;
		line=rowNum;
		eval = "true";
	}

	/**
	 * @param target Target
	 * @return Rule as a string, form depending on input
	 * @throws Exception
	 */
	String expr(String target) throws Exception {
		String r = "";
		switch(ruleType) {
			case BEFORE:
				if (!min.isEmpty() && !max.isEmpty()) {
					r  = compOID + " lt " + target + " or " + compOID + " - " + target + " lt " + min + " and " + compOID + " - " + target + " gt " + max;
				} else if (!min.isEmpty()) {
					r =  compOID + " lt " + target + " or " + compOID + " - " + target + " lt " + min;
				} else if (!max.isEmpty()) {
					r =  compOID + " lt " + target + " or " + compOID + " - " + target + " gt " + max;
				} else {
					r = compOID + " lt " + target;
				}
				break;
			case AFTER:
				/*
				 * for a-b OC 3.0.4.1 calculates abs(a-b)
				 * therefore when you calculate the difference between days you additionally need to check a lt b
				 */
				if (!min.isEmpty() && !max.isEmpty()) {
					r  = target + " lt " + compOID + " or " + target + " - " + compOID + " lt " + min + " and " + target + " - " + compOID + " gt " + max;
				} else if (!min.isEmpty()) {
					r =  target + " lt " + compOID + " or " + target + " - " + compOID + " lt " + min;
				} else if (!max.isEmpty()) {
					r =  target + " lt " + compOID + " or " + target + " - " + compOID + " gt " + max;
				} else {
					r = target + " lt " + compOID;
				}
				break;
			case ASSERT:
				if (compOID.isEmpty()) compOID=target;
				compOID = compOID.toUpperCase();
				if (val.isEmpty()) throw new Exception(line + ": val is empty");

				// zahlen pattern wird gesucht und soll benutzt werden, aber in anführungszeichen setzen regextreffer ist $1
				val = val.replaceFirst(" ([0-9]+)"," \"$1\"");
				r = compOID + " " + val;
				break;
			case REQUIRED:
				if (!min.isEmpty() && !max.isEmpty()) {
					r = target + " lt " + min + " and " + target + " gt " + max;
				} else if (!min.isEmpty()) {
					r = target + " lt " + min;
				} else if (!max.isEmpty()) {
					r = target + " gt " + max;
				} else {
					r = target + " eq \"\"";
				}
				if (!compOID.isEmpty()) {
					compOID = compOID.toUpperCase();
					if (val.isEmpty()) throw new Exception(line + ": val is empty");
					val = val.replaceFirst(" ([0-9]+)"," \"$1\"");
					r += " and " + compOID + " " + val;
				}
				break;
			case EXPRESSION:
				if (expression.startsWith("not ")) { eval="false"; expression = expression.substring(4); }
				// $iItem -->itemprefix+upperCase
				r = expression;
				break;
			case EMPTY:
				compOID = compOID.toUpperCase();
				// write numbers as strings
				val = val.replaceFirst(" ([0-9]+)"," \"$1\"");
				r = target + " ne " + "\"\"" + " and " + compOID + " " + val;
				break;
			case RANGE:
				if (!compOID.isEmpty()) {
					r = compOID + " eq \"" + val + "\" and ( ";
				}
				if (!min.isEmpty() && !max.isEmpty()) {
					r += target + " lt " + min + " or "  + target + " gt " + max;
				} else if (!min.isEmpty()) {
					r += target + " lt " + min;
				} else if (!max.isEmpty()) {
					r += target + " gt " + max;
				} else assert(false);
				if (!compOID.isEmpty()) r += " )";
			}
		return r;
	}
}