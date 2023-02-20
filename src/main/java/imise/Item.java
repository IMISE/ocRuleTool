package imise;

/**
 * OpenClinica supports user-defined CRF Item Data values according to Item Data properties defined during CRF and
 * Study design. OpenClinica enforces strict data typing corresponding with the Item Datatype
 * Item Data Types: https://docs.openclinica.com/3-1-technical-documents/openclinica-and-cdisc-odm-specifications/openclinica-and-cdisc-odm-specifications-cdisc-odm-representation-opencli-14/
 */
public class Item {

	/**
	 * Item name
	 */
	private String name;

	/**
	 * Event OID
	 */
	private String eventOID;

	/**
	 * CRF OID
	 */
	private String crfOID;

	/**
	 * Group OID
	 */
	private String groupOID;

	/**
	 * Item OID
	 */
	private String itemOID;

	/**
	 * Group containing the item
	 */
	private String group;

	/**
	 * Description of the item
	 */
	private String description;

	/**
	 * Row of the sheet in which the item is
	 */
	private int rowNum;

	/**
	 * Plain item constructor
	 * @param name Item name
	 * @param eventOID Event OID
	 * @param crfOID CRF OID
	 * @param groupOID Group OID
	 * @param itemOID Item OID
	 * @param group Group
	 * @param description Description
	 * @param rowNum Row number
	 */
	public Item(String name,
				String eventOID,
				String crfOID,
				String groupOID,
				String itemOID,
				String group,
				String description,
				int rowNum) {
		this.name = name;
		this.eventOID = eventOID;
		this.crfOID = crfOID;
		this.groupOID = groupOID;
		this.itemOID = itemOID;
		this.group = group;
		this.description = description;
		this.rowNum = rowNum;
	}

	/**
	 * @param itemOID Item OID
	 * @param groupOID Group OID
	 * @return True if supplied itemOID and groupOID are not the current OIDs
	 */
	public boolean update(String itemOID,String groupOID) {
		if (!this.itemOID.equals(itemOID)) System.out.println(itemOID + " " + this.itemOID);
		if (!this.groupOID.equals(groupOID)) System.out.println(groupOID + " " + this.groupOID);
		if (this.itemOID.equals(itemOID) && this.groupOID.equals(groupOID)) return false;   
		this.itemOID = itemOID;
		this.groupOID = groupOID;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getOID() {
		return itemOID;
	}

	/**
	 * @param first True if first OID
	 * @return "[1]" if true, empty String if false
	 */
	public String getGroupOID(boolean first) {
		return groupOID + (first?"[1]":"");
	}

	public String getGroupOID() {
		return groupOID;
	}

	/**
	 * @param first True if first OID
	 * @return String consisting of all OIDs
	 */
	public String getFullOID(boolean first) {
		return eventOID + "." + crfOID + "." + getGroupOID(first) + "." + itemOID;
	}

	public String getGroup() {
		return group;
	}

	public String getDescription() {
		return description;
	}

	public int getRowNum() {
		return rowNum;
	}
}
