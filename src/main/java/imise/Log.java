package imise;

import org.apache.poi.ss.usermodel.Row;

/**
 * Logging class with following log levels:
 * 4 = everything, 3 = info = 2 = warn 1 = error
 */
public class Log {

	/**
	 * Log level
	 */
	static int level = 4;

	/**
	 * Number of errors
	 */
	int numError = 0;

	/**
	 * Number of Warnings
	 */
	int numWarning = 0;

	/**
	 * True if there is a warning
	 */
	boolean warning = true;

	public  int getNumError() {
		return numError;
	}

	public  int getNumWarning() {
		return numWarning;
	}

	void log(String msg) {
		System.out.println(msg);
	}

	private void log(String msg, String type) {
		log(msg, type, null);
	}

	private void log(String msg, String type, Exception exc) {
		System.out.println(type + " " + msg);
		if (exc != null) {
			exc.printStackTrace(System.out);
		}
	}

	public void log(String msg, Row row, String type) {
		log(msg, row, type, null);
	}

	/**
	 * Prints details of row to System.out
	 * @param msg Log message
	 * @param row Row of sheet
	 * @param type Type
	 * @param e Exception
	 */
	public void log(String msg, Row row, String type, Exception e) {
		System.out.println(type + " in \"" + row.getSheet().getSheetName()
				+ "\" line " + (row.getRowNum() + 1) + ": " + msg);
		if (e != null)
			e.printStackTrace(System.out);
	}

	public void error(String msg) {
		log(msg, "error");
		numError++;
	}

	public void error(String msg, Exception e) {
		log(msg, "error", e);
		numError++;
	}

	public void error(String msg, Row row) {
		log(msg, row, "error");
		numError++;
	}

	public void fatal(String msg) throws Exception {
		log(msg, "fatal");
		numError++;
		throw new Exception("fatal - aborted");
	}

	public void fatal(String msg, Exception e) throws Exception {
		log(msg, "fatal", e);
		numError++;
		throw new Exception("fatal - aborted");
	}

	public void warning(String msg) {
		if (warning)
			log(msg, "warning");
		numWarning++;
	}

	public void warning(String msg, Exception e) {
		if (warning)
			log(msg, "warning", e);
		numWarning++;
	}

	public void warning(String msg, Row row) {
		if (warning)
			log(msg, row, "warning");
		numWarning++;
	}

	public void warning(String msg, Row row, Exception e) {
		if (warning)
			log(msg, row, "warning", e);
		numWarning++;
	}

}
