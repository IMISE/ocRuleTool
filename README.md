# ocRuleTool
The ocRuleTool generate XML rules for OpenClinica/LibreClinica CDMS based on Excel templates.

## Purpose ##
This repository contains the ocRuleCompiler, a small Java app that facilitates the generation of rules for OpenClinica/LibreClinica CDISC-ODM management. Specifically, it generates rules.xml from CRF descriptions.

## Setup ##
To set up the ocRuleCompiler, follow these steps:

1. Download the latest release (.jar) for easy setup.
2. Navigate to the directory with the .jar file using your terminal.
3. Run the command `java -jar ocRuleCompiler2-2-shaded.jar /path/to/your/CRF.xls` to generate rules.xml.

## Workflow ##
The following steps outline the workflow for generating rules.xml with the ocRuleCompiler:

1. Create a CRF and add items and rules to the respective sheets.
2. For more information on specifying rules, refer to the OpenClinica Docs at https://docs.openclinica.com/3-1/rules/.
3. Upload the CRF to your OpenClinica/LibreClinica platform by navigating to Tasks -> Build Study -> in column Create CRF click the "+" -> upload the .xls file.
4. Retrieve the generated OIDs and add them to the Excel file.
5. Generate rules.xml with the ocRuleCompiler.
6. Upload the rules.xml to OpenClinica/LibreClinica by navigating to Tasks -> Build Study -> in column Create Rules click the "+" -> upload the rules.xml file.

## Usage ##
Instructions for using the ocRuleCompiler.


### Compiling from Source ###
If you want to compile the ocRuleCompiler yourself, you'll need git, maven, and java >= 1.8. Follow these steps:

1. Clone the repository by running `git clone https://fmeineke@github.com/fmeineke/ocRuleCompiler.git`.
2. Change your working directory to `ocRuleCompiler`.
3. Run `mvn clean package -Dmaven.test.skip=true` to build target/ocRuleCompiler.jar and copy dependencies to target/lib.
4. Start the batch shell with `java -jar target/ocRuleCompiler-1.0.jar`.

### Notes ###
Here are some additional notes to keep in mind when using the ocRuleCompiler:

- Standard encoding is ISO-8859-1, which supports Ä,Ö,Ü, and other special characters.
- If you want to encode the rules.xml in UTF-8, add -utf at the end of the command.
- With UTF-8 encoding, umlauts may not be displayed properly in OpenClinica/LibreClinica.
- If you need help with the CRF.xls, you can find an example under src/test/resources.
