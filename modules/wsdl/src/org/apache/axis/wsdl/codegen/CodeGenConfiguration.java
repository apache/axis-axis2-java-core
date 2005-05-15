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

import org.apache.wsdl.WSDLDescription;

import java.io.File;
import java.util.Map;

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
    private boolean asyncOn=true;
    private boolean syncOn=true;
    private String packageName=XSLTConstants.DEFAULT_PACKAGE_NAME;

	/**
	 * @param wom
	 * @param parser
	 */
	public CodeGenConfiguration(WSDLDescription wom,
			CommandLineOptionParser parser) {
        this.wom = wom;
		this.parser = parser;

        Map optionMap = parser.getAllOptions();

		String outputLocation = ((CommandLineOption)optionMap.get(OUTPUT_LOCATION_OPTION)).getOptionValue();
        this.outputLocation = new File(outputLocation);

        advancedCodeGenEnabled = (optionMap.get(ADVANCED_CODEGEN_OPTION)!=null);
        boolean asyncFlagPresent = (optionMap.get(CODEGEN_ASYNC_ONLY_OPTION)!=null);
        boolean syncFlagPresent = (optionMap.get(CODEGEN_SYNC_ONLY_OPTION)!=null);
        if (asyncFlagPresent) {this.asyncOn=true;this.syncOn=false;}
        if (syncFlagPresent) {this.asyncOn=false;this.syncOn=true;}

        CommandLineOption packageOption = (CommandLineOption)optionMap.get(PACKAGE_OPTION);
        if(packageOption!=null) {this.packageName = packageOption.getOptionValue();}

        CommandLineOption langOption = (CommandLineOption)optionMap.get(STUB_LANGUAGE_OPTION);
        if (langOption!=null){
            loadLanguge(langOption.getOptionValue());
        }

	}

    private void loadLanguge(String langName) {
        if (LanguageNames.JAVA.equalsIgnoreCase(langName)){
            this.outputLanguage = XSLTConstants.LanguageTypes.JAVA;
        }else if (LanguageNames.C_SHARP.equalsIgnoreCase(langName)){
            this.outputLanguage = XSLTConstants.LanguageTypes.C_SHARP;
        }else if (LanguageNames.C_PLUS_PLUS.equalsIgnoreCase(langName)){
            this.outputLanguage = XSLTConstants.LanguageTypes.C_PLUS_PLUS;
        }else if (LanguageNames.VB_DOT_NET.equalsIgnoreCase(langName)){
            this.outputLanguage = XSLTConstants.LanguageTypes.VB_DOT_NET;
        }
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

    public boolean isAsyncOn() {
        return asyncOn;
    }


    public boolean isSyncOn() {
        return syncOn;
    }

    public String getPackageName() {
        return packageName;
    }
}