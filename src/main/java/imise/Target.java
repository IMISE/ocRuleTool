package imise;

import imise.Rule.RuleType;
import imise.encoding.AnsiEncoder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  -  targetInSubgroup && !compInSubgroup dann Target[1], Comp, Regel "_1"
 *  -  targetInSubgroup &&  compInSubgroup dann Target, Comp, Regel
 *  - !targetInSubgroup && !compInSubgroup dann Target, Comp, Regel
 *  - !targetInSubgroup &&  compInSubgroup dann Target, Comp[1], Regel
 */
class Target {
	final private HashMap<String, Item> items;
	final Item targetItem;
	private boolean targetInSubgroup = false;
	private boolean compInSubgroup = false;
	private Log log;
	private String encoding;
	List<Rule> ruleList = new LinkedList<>();

	Target(HashMap<String, Item> items, Item targetItem, Log log, String encoding) {
		this.log = log;
		this.encoding = encoding;
		this.items = items;
		this.targetItem = targetItem;
		targetInSubgroup = !targetItem.getGroup().equals("UNGROUPED");
	}

	/**
	 *
	 * @param ruleImport Document
	 * @return Size of rule list
	 * @throws DOMException
	 * @throws Exception
	 */
	int appendRuleDefs(Element ruleImport) throws DOMException, Exception {
		Document doc = ruleImport.getOwnerDocument();
		int i = 1;
		for (Rule rule : ruleList) {
			String ruleName = OcRuleTool.item_prefix + targetItem.getName().toUpperCase() + i;
			String ruleOID = targetItem.getOID().toUpperCase() + "_" + i;
			i++;
			if (targetInSubgroup && !compInSubgroup) {
				ruleOID += "_1";
				ruleName += "_1";
			}
			Element ruleDef = doc.createElement("RuleDef");
			ruleDef.setAttribute("Name", ruleName);
			ruleDef.setAttribute("OID", ruleOID);

			Element description = doc.createElement("Description");
			//description.appendChild(doc.createTextNode((isAnsi()) ? AnsiEncoder.replaceAllAnsi(rule.msg) : rule.msg));
			description.appendChild(doc.createTextNode(rule.msg));

			Element expression = doc.createElement("Expression");
			expression.appendChild(doc.createTextNode(rule.expr(targetItem.getOID())));
			ruleDef.appendChild(description);
			ruleDef.appendChild(expression);
			ruleImport.appendChild(ruleDef);
		}
		return ruleList.size();
	}

	/**
	 * for every rule in a rule list
	 * @param ruleImport Document
	 */
	void appendRuleAssignment(Element ruleImport) {
		Document doc = ruleImport.getOwnerDocument();
		Element ruleAssignment = doc.createElement("RuleAssignment");
		Element tg = doc.createElement("Target");
		tg.setAttribute("Context", "OC_RULES_V1");
		tg.appendChild(doc.createTextNode(targetItem
				.getFullOID(targetInSubgroup && !compInSubgroup)));
		ruleAssignment.appendChild(tg);
		int i = 1;

		for (Rule rule : ruleList) {
			String ruleOID = targetItem.getOID().toUpperCase() + "_" + i++;
			if (targetInSubgroup && !compInSubgroup)
				ruleOID += "_1";

			Element ruleRef = doc.createElement("RuleRef");
			ruleRef.setAttribute("OID", ruleOID);

			Element dna = doc.createElement("DiscrepancyNoteAction");
			dna.setAttribute("IfExpressionEvaluates", "true");

			Element message = doc.createElement("Message");
			//message.appendChild(doc.createTextNode((isAnsi()) ? AnsiEncoder.replaceAllAnsi(rule.msg) : rule.msg));
			message.appendChild(doc.createTextNode(rule.msg));
			ruleRef.appendChild(dna).appendChild(message);

			if (!rule.email.equals("")) {
				Element ea = doc.createElement("EmailAction");
				ea.setAttribute("IfExpressionEvaluates", "true");
				Element to = doc.createElement("To");
				to.appendChild(doc.createTextNode(rule.email));
				Element message2 = doc.createElement("Message");
				message2.appendChild(doc.createTextNode(rule.msg));
				ea.appendChild(message2);
				ruleRef.appendChild(ea).appendChild(to);
			}
			ruleAssignment.appendChild(ruleRef);
		}
		ruleImport.appendChild(ruleAssignment);
	}

	public String getCellAsString(Row row, int col) {
		Cell cell = row.getCell(col);
		cell.setCellType(CellType.STRING);
		return cell.getStringCellValue().trim();
	}

	public void add(Row row, HashMap<String, Integer> header_rules, String email)
			throws Exception {
		String ruleTypeString = getCellAsString(row,
				header_rules.get("RULE_TYPE"));
		if (ruleTypeString.trim().length() == 0) {
			log.warning("rule_type is empty", row);
			return;
		}
		RuleType ruleType;
		try {
			ruleType = Rule.RuleType.valueOf(ruleTypeString.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("invalid rule_type " + ruleTypeString, row);
			return;
		}

		String val = getCellAsString(row, header_rules.get("VAL"));
		String min = getCellAsString(row, header_rules.get("MIN"));
		String max = getCellAsString(row, header_rules.get("MAX"));
		final String comp = getCellAsString(row, header_rules.get("COMP"));
		String compOID = "";
		String expression = "";

		if (ruleType == RuleType.EXPRESSION) {
			// expression = comp;
			String[] arr = comp.split(" ");
			for (int i = 0; i < arr.length; i++) {
				String tok = arr[i];
				Item item = items.get(tok);
				if ((tok.length() > 4 && tok.matches("[a-zA-Z_0-9]*"))
						|| item != null) {
					if (item != null) {
						if (!item.getGroupOID().equals("UNGROUPED")) {
							compInSubgroup = true;
						}
						if (targetItem.getGroupOID().equals(item.getGroupOID()))
							arr[i] = item.getOID();
						else {
							arr[i] = item.getGroupOID() + "." + item.getOID();
						}
					} else {
						log.warning("potential unknown item in expression:"
								+ tok, row);
					}
				}
			}
			expression = arr[0];
			for (int i = 1; i < arr.length; i++)
				expression += " " + arr[i];

		} else {
			if (!comp.equals("")) {
				Item compItem = items.get(comp);
				if (comp.contains("."))
					compOID = comp.toUpperCase();
				else if (compItem == null) {
					log.error(comp + " found in COMP but not in Items", row);
					return;
				} else {
					compInSubgroup = !compItem.getGroup().equals("UNGROUPED");
					if (compItem.getGroupOID().equals(targetItem.getGroupOID())) {
						compOID = compItem.getOID();
					} else { // verschiedene Gruppen
						compOID = compItem.getGroupOID()
								+ ((!targetInSubgroup && compInSubgroup) ? "[1]"
										: "") + "." + compItem.getOID();
					}
				}
			}
		}
		String msg = getCellAsString(row,
				header_rules.get("RULE_ERROR_MESSAGE"));
		msg = msg.replaceAll("\\$n", "\"" + targetItem.getDescription() + "\"");
		Item compItem = items.get(comp);
		if (compItem != null) {
			msg = msg.replaceAll("\\$c", "\"" + compItem.getDescription() + "\"");
		}
		msg = msg.replaceAll("<", "&lt;");

		if (!(msg.endsWith(".") || msg.endsWith("!"))) {
			log.warning("missing . or ! at end of error message", row);
		}
		if (!msg.equals(new String(msg.getBytes(encoding)))) {
			log.warning("non ISO char in error message" + msg, row);
		}
		ruleList.add(new Rule(ruleType, compOID, expression, val, min, max,
				msg, row.getRowNum() + 1, email));
	}

	private boolean isAnsi() {
		return encoding.equals(OcRuleTool.ENC_ISO_8859_1);
	}
}