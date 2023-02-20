package imise;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Main class that reads an xls file and generates an xml file for the rules and items ready to upload to Open Clinica
 */
public class OCRead {

	static final String newline = System.getProperty("line.separator");
	static final String ENC_ISO_8859_1 = "ISO-8859-1";
	static final String ENC_UTF_8 = "UTF-8";

	/**
	 * 2013-04-09 (C) F. Meineke, 2013-09-25 F. Rissner
	 * 2016-06-02 (C) F. Meineke
	 */
	static final String version = "ocRuleCompiler version 2023-02-10 (C) F. Meineke, F. Ulbrich";

	/**
	 * Path of input file (.xls)
	 */
	String infile = null;

	/**
	 * Path of output file (.xml)
	 */
	String outfile = null;

	HashMap<String, Integer> header_rules = new HashMap<String, Integer>();

	/**
	 * Memorize numerical row for a row headline
	 */
	HashMap<String, Integer> header_items = new HashMap<String, Integer>();

	/**
	 * HashMap of all items
	 */
	HashMap<String, Item> items = new HashMap<String, Item>();

	Log log = new Log();
	private List<Target> targetList = new LinkedList<Target>();

	/**
	 * Study event OID
	 */
	private String eventOID;

	/**
	 * Case report form OID
	 */
	private String crfOID;

	/**
	 * OID of ungrouped items
	 */
	private String ungroupedOID;

	private String groupPrefix;
	private String itemPrefix;

	//	String encoding = ENC_UTF_8;
	private String encoding="ISO-8859-1";

	private String sourceSystem = "";

	/**
	 * true: rulePrefix = itemPrefix
	 * false: rulePrefix = ""
	 */
	public static boolean useRulePrefix = false;
	public static String rulePrefix = "";
	public static String item_prefix = "";


    public static void main(String[] args) {
		new OCRead().start(args);
	}

	/**
	 * Basically main class
	 * Reads input parameters from commandline and tries to connect to a given db if necessary.
	 * Then, transforms input xls to workbook and reads OIds, rules and items.
	 * Then, writes results to xml
	 * @param args Parameter given with the specified flags via command line
	 */
	public void start(String[] args) {

		log.log(version);

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-utf" -> encoding = ENC_UTF_8;
				case "-latin" -> encoding = ENC_ISO_8859_1;
				case "-source" -> sourceSystem = args[++i];
				case "-o" -> outfile = args[++i];
				case "-prefix" -> useRulePrefix = true;
				case "-nowarnings" -> log.warning = false;
			}
			if(infile == null) infile = args[i];
		}

		String now = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

		try {
			if (infile == null) printCommandLineOptionsToConsole();
			if (outfile == null) outfile = infile.replace(".xls", sourceSystem + "-rules.xml");
			log.log("read " + infile);
			InputStream inp = new FileInputStream(infile);
			Workbook wb = WorkbookFactory.create(inp);
			inp.close();
			wb.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
			readRulesOID(wb);
			log.log("readRulesOID done");
			// read "Items" and internally add OID columns
			readItems(wb);
			log.log("readItems done");
			readRules(wb);
			log.log("readRules done");
			PrintStream out = outfile.equals("-") ? System.out : new PrintStream(outfile, encoding);
			log.log("writing " + outfile);
			writeRulesXML(out, sourceSystem, now);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		String[] cleaned = stripArgs(args);
		log.log("'" + this.getClass().getSimpleName() + "' called with params: " + Arrays.toString(cleaned));
		log.log(" returnes with " + log.getNumError() + " error"
				+ (log.getNumError() == 1 ? "" : "s") + " "
				+ log.getNumWarning() + " warning"
				+ (log.getNumWarning() == 1 ? "" : "s"));
		if (log.getNumWarning() > 0 && !log.warning)
			log.log("RECOMMENDATION: retry without switch '-nowarnings'");
	}

	/**
	 * @param headerRow Top row of the workbook
	 * @return Hashmap with column names and their position
	 * @throws Exception
	 */
	HashMap<String, Integer> getHeaderColumns(Row headerRow) throws Exception {
		if (headerRow == null) {
			log.fatal("expected Header Row is empty");
		}
		HashMap<String, Integer> header = new HashMap<>();
		for (int j = 0; j < headerRow.getLastCellNum(); j++) {
			int num = headerRow.getLastCellNum();
			String h = headerRow.getCell(j).toString().trim().toUpperCase();
			// remove "*" at the end
			if (h.endsWith("*")) h = h.substring(0, h.length()-1);
			header.put(h, j);
		}
		return header;
	}

	void readItems(Workbook wb) throws Exception {
		Sheet sheet = wb.getSheet("Items");
		if (sheet == null) {
			log.fatal("no Items sheet");
		}
		header_items = getHeaderColumns(sheet.getRow(0));

		if (header_items.get("ITEM_OID") == null) {
			int col = header_items.size();
			Cell cell = sheet.getRow(0).createCell(col);
			header_items.put("ITEM_OID", col);
			cell.setCellValue("ITEM_OID");
		}
		if (header_items.get("GROUP_OID") == null) {
			int col = header_items.size();
			Cell cell = sheet.getRow(0).createCell(col);
			header_items.put("GROUP_OID", col);
			cell.setCellValue("GROUP_OID");
		}
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row r = sheet.getRow(i);
			if (r == null)
				break;

			String itemName = r.getCell(header_items.get("ITEM_NAME")).toString();
			String responseValues = r.getCell(header_items.get("RESPONSE_VALUES_OR_CALCULATIONS")).toString();
			String dataType = r.getCell(header_items.get("DATA_TYPE")).toString();
			String groupLabel = r.getCell(header_items.get("GROUP_LABEL")).toString();
			if (groupLabel.isEmpty()) groupLabel = "UNGROUPED";
			String description = r.getCell(header_items.get("DESCRIPTION_LABEL")).toString();

			if (responseValues.matches(".*_[0-9]*"))
				log.warning("Unnecessary versioning: " + responseValues, r);
			if (responseValues.matches("([0-9][0-9]*[, ]*)+")
					&& !dataType.equals("INT"))
				log.warning("data type could be INT " + responseValues, r);
			if (!responseValues.matches("([0-9][0-9]*[, ]*)*")
					&& dataType.equals("INT"))
				log.warning(itemName + " is invalid INT " + responseValues, r);
			/*
			 * if (data_type.equals("DATE") && !item_name.endsWith("_D")) {
			 * warning(item_name +
			 * " is of type DATE but does not end with without _D",r); } if
			 * (!data_type.equals("DATE") && item_name.endsWith("_D")) {
			 * warning(item_name + " ends with _D but is not of type DATE ",r);
			 * }
			 */
			String itemOID = r.getCell(header_items.get("ITEM_OID")).toString();
			if (itemOID.isEmpty())
				itemOID = itemPrefix + itemName.toUpperCase();
			String groupOID = r.getCell(header_items.get("GROUP_OID"))
					.toString();
			if (groupOID.isEmpty())
				groupOID = groupPrefix + groupLabel.toUpperCase();
			items.put(itemName, new Item(itemName, eventOID, crfOID, groupOID,
					itemOID, groupLabel, description, i));
		}
	}

	/**
	 * Reads Event-OID, CRF-OID an Ungrouped-OID from the workbook an sets group prefix, item prefix and rule prefix
	 * @param wb Excel workbook with OpenClinica rules
	 * @throws Exception
	 */
	void readRulesOID(Workbook wb) throws Exception {
		Sheet sheet = wb.getSheet("Rules");
		if (sheet == null){
			log.fatal("o rules sheet");
		} else {
			HashMap<String, Integer> header_oid = getHeaderColumns(sheet.getRow(0));
			if (!header_oid.containsKey("EVENT_OID"))
				log.fatal("EVENT_OID missing in Rules");
			if (!header_oid.containsKey("CRF_OID"))
				log.fatal("CRF_OID missing in Rules");
			if (!header_oid.containsKey("UNGROUPED_OID"))
				log.fatal("UNGROUPED_OID missing in Rules");

			eventOID = sheet.getRow(1).getCell(header_oid.get("EVENT_OID")).toString();
			crfOID = sheet.getRow(1).getCell(header_oid.get("CRF_OID")).toString();
			ungroupedOID = sheet.getRow(1).getCell(header_oid.get("UNGROUPED_OID")).toString();

			if (!ungroupedOID.endsWith("_UNGROUPED"))
				log.warning("UNGROUPED does not end with \"_UNGROUPED\" in Rules");

			if (!ungroupedOID.startsWith("IG_"))
				log.warning("UNGROUPED does not start with \"IG_\" in Rules");

			int l = "UNGROUPED".length();
			groupPrefix = ungroupedOID.substring(0, ungroupedOID.length() - l);
			itemPrefix = "I" + ungroupedOID.substring(2, ungroupedOID.length() - l);

			if (useRulePrefix) rulePrefix = itemPrefix; else rulePrefix = "";
			item_prefix = itemPrefix;
		}
	}

	/**
	 * Iterates over all rows
	 * @param wb Excel workbook with OpenClinica rules
	 * @throws Exception
	 */
	void readRules(Workbook wb) throws Exception {

		String target;
		String prevTarget = null;
		Target targetElement = null;

		Sheet sheet = wb.getSheet("Rules");
		if (sheet == null) log.fatal("no \"Rules\" sheet");

		header_rules = getHeaderColumns(sheet.getRow(3));

		int firstRow = 4;

		for (int i = firstRow; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) continue;
			String itemName = row.getCell(header_rules.get("ITEM_NAME")).toString();

			if (itemName.isEmpty()) {
				target = prevTarget;
			} else {
				target = itemName;
			}

			if (prevTarget != target) {
				Item targetItem = items.get(target);
				if (targetItem == null) {
					log.warning("didn't find '" + target + "'; checking whether it has unique id added", row);
					for (String type : items.keySet()) {
						if (target.startsWith(type)) {
							targetItem = items.get(type);
						}
					}
				}
				if (targetItem == null) {
					log.error("unknown rule target " + target);
					continue;
				}
				targetElement = new Target(items, targetItem, log, encoding);
				targetList.add(targetElement);
			}

			prevTarget = target;

			if (!items.containsKey(target)) {
				log.error(target + " found in Rules but not in Items", row);
				continue;
			}

			String email = "";
			if (header_rules.get("EMAIL") != null) email = row.getCell(header_rules.get("EMAIL")).toString();
			targetElement.add(row, header_rules, email);
		}
	}

	void writeRulesXML(PrintStream out, String sourceSystem, String now) throws DOMException, Exception {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		// doc.setXmlStandalone(true);
		Element ruleImport = doc.createElement("RuleImport");
		doc.appendChild(ruleImport);

		Comment comment = doc.createComment("Generator: "
				+ version
				+ newline
				+ "    Inputfile: "
				+ infile
				+ newline
				+ ((!"".equals(sourceSystem)) ? ("    SourceSystem: "
						+ sourceSystem + newline) : "") + "         User: "
						+ System.getProperty("user.name") + newline + "         Date: "
						+ now + newline);
		doc.insertBefore(comment, ruleImport);

		for (Target target : targetList) {
			target.appendRuleAssignment(ruleImport);
		}

		for (Target target : targetList) {
			target.appendRuleDefs(ruleImport);
		}

		log.log("write "
				+ ruleImport.getElementsByTagName("RuleAssignment").getLength()
				+ " targets with "
				+ ruleImport.getElementsByTagName("RuleDef").getLength()
				+ " rules.");

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);

	}

	private String[] stripArgs(String[] args) {
		List<String> list = new ArrayList<String>();
		String placeholder = "xxxxxx";
		boolean next = false;
		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if (next) {
				list.add(placeholder);
				next = false;
			} else
				list.add(string);
			if (string.toUpperCase().equals("-USER")
					|| string.toUpperCase().equals("-PASSWORD")) {
				next = true;
			}
		}
		String[] a = new String[0];
		return list.toArray(a);
	}

	/**
	 * Command prompt to show available options to the user
	 */
	void printCommandLineOptionsToConsole() {
		log.log("usage: ocread "
				+ "\n  [-utf|latin]   : output xml enconding, default is "
				+ encoding
				+ "\n  [-nowarnings]  : suppresses warnings"
				+ "\n  [-o <name>.xml : output, default is <name>-rules.xml, '-' is stdout"
				+ "\n  [-prefix]      : prefix rule OID with group prefix"
				+ "\n  [-source]      : name of SourceSystem; used in outputfiles"
				+ "\n  <name>.xls     : OpenClinica Excel Sheet");
		System.exit(1);
	}
}
