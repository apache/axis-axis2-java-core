/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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