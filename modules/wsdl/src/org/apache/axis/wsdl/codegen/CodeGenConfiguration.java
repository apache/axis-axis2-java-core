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

package org.apache.axis.wsdl.codegen;

import java.io.File;

import org.apache.wsdl.WSDLDescription;

/**
 * @author chathura@opensource.lk
 *  
 */
public class CodeGenConfiguration implements CommandLineOptionConstants {

	private WSDLDescription wom;
	private CommandLineOptionParser parser;
	private File outputLocation;
    private int outputLanguage = XSLTConstants.LanguageTypes.JAVA;
    private boolean advancedCodeGenEnabled=false;

	/**
	 * @param wom
	 * @param parser
	 */
	public CodeGenConfiguration(WSDLDescription wom,
			CommandLineOptionParser parser) {
		this.wom = wom;
		this.parser = parser;
		String outputLocation = ((CommandLineOption) parser.getAllOptions().get(
				OUTPUT_LOCATION_OPTION)).getOptionValue();
        advancedCodeGenEnabled = (parser.getAllOptions().get(ADVANCED_CODEGEN_OPTION)!=null);

		this.outputLocation = new File(outputLocation);

	}

	/**
	 * @return Returns the parser.
	 */
	public CommandLineOptionParser getParser() {
		return parser;
	}

	/**
	 * @return Returns the wom.
	 */
	public WSDLDescription getWom() {
		return wom;
	}
	
	
	/**
	 * @return Returns the outputLocation.
	 */
	public File getOutputLocation() {
		return outputLocation;
	}

    public int getOutputLanguage() {
        return outputLanguage;
    }

    public boolean isAdvancedCodeGenEnabled() {
        return advancedCodeGenEnabled;
    }
}