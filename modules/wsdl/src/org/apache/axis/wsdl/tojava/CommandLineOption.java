package org.apache.axis.wsdl.tojava;

/**
 * @author chathura@opensource.lk
 *  
 */
public class CommandLineOption implements CommandLineOptionConstants {

	private String type;

	private String optionValue;

	private boolean invalid = false;

	/**
	 * @param type
	 * @param optionValue
	 */
	public CommandLineOption(String type, String optionValue) {

		if (("-" + WSDL_LOCATION_URI_OPTION).equalsIgnoreCase(type)) {
			this.type = WSDL_LOCATION_URI_OPTION;
		} else if (("-" + OUTPUT_LOCATION_OPTION).equalsIgnoreCase(type)) {
			this.type = OUTPUT_LOCATION_OPTION;
		}else if (("-" + ADVANCED_CODEGEN_OPTION).equalsIgnoreCase(type)) {
			this.type = ADVANCED_CODEGEN_OPTION;
		} else {
			this.invalid = true;
		}

		if (optionValue == null) {
			this.invalid = true;
		} else {
			this.optionValue = optionValue;
		}
	}

	/**
	 * @return Returns the type.
	 * @see <code>CommandLineOptionConstans</code>
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Returns the optionValue.
	 */
	public String getOptionValue() {
		return optionValue;
	}

	/**
	 * @return Returns the invalid.
	 */
	public boolean isInvalid() {
		return invalid;
	}
}