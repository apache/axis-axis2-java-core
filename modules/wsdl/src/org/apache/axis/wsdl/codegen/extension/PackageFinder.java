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

package org.apache.axis.wsdl.codegen.extension;

import java.util.Map;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.codegen.CommandLineOption;
import org.apache.axis.wsdl.codegen.CommandLineOptionConstants;
import org.apache.wsdl.WSDLBinding;

/**
 * @author chathura@opensource.lk
 *  
 */
public class PackageFinder extends AbstractCodeGenerationExtension implements
		CodeGenExtention {

	public static final String DEFAULT_PACKAGE = "axis2";
	
	private static final String HTTP_PRFIX ="http://";

	public void init(CodeGenConfiguration configuration) {
		this.configuration = configuration;

	}

	public void engage() {
		Map allOptions = this.configuration.getParser().getAllOptions();
		String packageName = ((CommandLineOption)(allOptions.get(CommandLineOptionConstants.CLIENT_PACKAGE))).getOptionValue();
		if(null == packageName || "".equals(packageName))
			packageName = DEFAULT_PACKAGE;
		
		if (packageName == null) {
			WSDLBinding binding = configuration.getWom().getBinding(AxisBindingBuilder.AXIS_BINDING_QNAME);
			String temp = binding.getBoundInterface().getName().getNamespaceURI();
			//Striping off the http:// prefix
			String[] splitValues = temp.split(HTTP_PRFIX);			
			packageName = splitValues[1].trim();	
			if(null == packageName || "".equals(packageName))
				packageName = DEFAULT_PACKAGE;
			
			if(packageName.endsWith("/"))
				packageName = packageName.substring(0, packageName.length()-1 );
			
			packageName = packageName.replace('.', '#');
			String[] individualPackageNames = packageName.split("#");
			if(individualPackageNames.length>0){
				packageName = individualPackageNames[individualPackageNames.length -1];
				for(int i = individualPackageNames.length -2; i>=0; i--){
					packageName = packageName+ "." +individualPackageNames[i];
				}
			}
		}	
		
		if(null == packageName || "".equals(packageName))
			packageName = DEFAULT_PACKAGE;
		
	
		
		
		this.configuration.setPackageName(packageName.toLowerCase());

	}

	

}