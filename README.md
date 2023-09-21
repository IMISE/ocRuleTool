# ocRuleTool
The ocRuleTool generates XML rules for OpenClinica/LibreClinica CDMS based on Excel templates.

## Purpose ##
This repository contains the ocRuleCompiler, a small Java app that facilitates the generation of rules for OpenClinica/LibreClinica CDISC-ODM management. Specifically, it generates rules.xml from CRF descriptions.

## Setup ##
To set up the ocRuleTool, follow these steps:

1. Download the ocRuleTool.jar for easy setup  (or build yourself!)
3. Navigate to the directory with the .jar file using your terminal.
4. Run the command `java -jar ocRuleTool.jar /path/to/your/CRF.xls` to generate rules.xml.

## Workflow ##
The following steps outline the workflow for generating rules.xml with the ocRuleCompiler:

1. Create a CRF and add items and rules to the respective sheets.
2. For more information on specifying rules, refer to the OpenClinica Docs at https://docs.openclinica.com/3-1/rules/.
3. Upload the CRF to your OpenClinica/LibreClinica platform by navigating to Tasks -> Build Study -> in column Create CRF click the "+" -> upload the .xls file.
4. Retrieve the generated OIDs and add them to the Excel file.
5. Generate rules.xml with the ocRuleTool.
6. Upload the rules.xml to OpenClinica/LibreClinica by navigating to Tasks -> Build Study -> in column Create Rules click the "+" -> upload the rules.xml file.

## Explanation ##
Explanation of the different Excel cells.

- **EVENT_OID**
If the CRF is assigned to an Event in Openclinica, the EVENT_OID must be entered here. If the Event has already been created: -> Build Study -> in column Create Event Definitions click the "view" -> look for **OID**.

- **CRF_OID**
The CRF_OID is created when uploading the CRF to the Openclinica platform and can be found under: Tasks -> CRFs -> *look for the CRF in question and then in the column **CFR_OID**.*

- **UNGROUPED_OID**
The UNGROUPED_OID is created when uploading the CRF to the Openclinica platform: Tasks -> CRFs -> *look for the CRF in question* -> "view" -> "<...>" -> to be found in row *Ungrouped* and column **OID**.

- **ITEM_NAME**
The item to which the rule should be applied. If left empty, the rule will be applied to the item above.

- **RULE_TYPE** Type of Rule. Currently supported Types include:

    - **After** *Use to compare dates.* Similar to "less than". Tests if **COMP** is less than **ITEM_NAME**.

    - **Before** *Use to compare dates.* Similar to "greater than". Tests if **COMP** is greater than **ITEM_NAME**.

    - **Expression** Use for a custom statment in **COMP**.

    - **Range** Defines a range of values for the item. Use in combination with **MIN** and **MAX**.

    - **Required** *There must be data within the item*.

    - **Empty** *The item must be empty.*

- **COMP**
Here the comparison data point for all dependent Statments (after,before...) is needed, normally in the form of an item. If the **RULE_TYPE** expression is selected you can also define your own statments in the form of `[ITEM] [operator] [value]`.
In addition, it can be treated as an if statement where the item named in **COMP** must first satisfy the condition given by an operator in **VAL** to validate the item named in **ITEM_NAME**.

- **VAL**
Used to validate the COMP statement. Requires a value or a logical operator + value. Can also be used to create a statement if there is only a item given in **COMP**. Currently supported opreators include:

    - greater than - gt(int) or gt(real) 

    - less than - lt(int) or lt(real)

    - range - range(int1, int2) or range(real1, real2)

    - gte(int) or gte(real)

    - lte(int) or lte(real)

    - ne(int) or ne(real)

    - eq(int) or eq(real)

- **MIN**
Minimum value expected from the item.

- **MAX**
Maximum value expected from the item.

- **RULE_ERROR_MESSAGE**
Error message if the given rule is violated. "$n" is used to indicate the item whose rule was violated.

- **GROUP**
The group of the item must be named here. Additionally it can be used to limit a rule to a single row `**GROUP**[*row*]` if the **GROUP** is defined as GRID.

## Compiling from Source ##
If you want to compile the ocRuleCompiler yourself, you'll need git, maven, and java >= 1.8. Follow these steps:

1. Clone the repository by running `git clone https://fmeineke@github.com/fmeineke/ocRuleCompiler.git`.
2. Change your working directory to `ocRuleCompiler`.
3. Run `mvn clean package -Dmaven.test.skip=true` to build target/ocRuleTool.jar and copy dependencies to target/lib.
4. Start the batch shell with `java -jar target/ocRuleTool.jar`.

## Notes ##
Here are some additional notes to keep in mind when using the ocRuleCompiler:

- Standard encoding is ISO-8859-1, which supports Ä,Ö,Ü, and other special characters.
- If you want to encode the rules.xml in UTF-8, add -utf at the end of the command.
- With UTF-8 encoding, umlauts may not be displayed properly in OpenClinica/LibreClinica.

