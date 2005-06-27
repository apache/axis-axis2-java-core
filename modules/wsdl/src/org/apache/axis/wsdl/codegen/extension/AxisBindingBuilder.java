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

import javax.xml.namespace.QName;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLInterface;

/**
 * @author chathura@opensource.lk
 *
 */
public class AxisBindingBuilder extends AbstractCodeGenerationExtension implements CodeGenExtension {
	
	public static final String AXIS_NAMESPACE = "http://ws.apache.org/axis2/";
	
	public static final QName AXIS_BINDING_QNAME = new QName(AXIS_NAMESPACE, "codeGenerationBinding", "axis");
	
	
	
	public AxisBindingBuilder() {		
	}
	
	public void init(CodeGenConfiguration configuration){
		this.configuration = configuration;
	}
	
	public void engage(){
		WSDLDescription  wom = this.configuration.getWom();
		WSDLBinding binding = wom.getFirstBinding();
		
		WSDLBinding newBinding = wom.createBinding();
		newBinding.setName(AXIS_BINDING_QNAME);
		
		WSDLInterface boundInterface = binding.getBoundInterface();
		newBinding.setBoundInterface(boundInterface);
		
		newBinding.setBindingFaults(binding.getBindingFaults());
		newBinding.setBindingOperations(binding.getBindingOperations());
		wom.addBinding(newBinding);						
	}
}
