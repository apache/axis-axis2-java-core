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

package org.apache.axis.wsdl.codegen.emitter;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.codegen.CodeGenerationException;
import org.apache.axis.wsdl.codegen.extension.AxisBindingBuilder;
import org.apache.axis.wsdl.databinding.DefaultTypeMapper;
import org.apache.wsdl.WSDLBinding;

import java.io.IOException;

/**
 * @author chathura@opensource.lk
 *
 */
public class ClientJavaEmitter implements Emitter {


	private CodeGenConfiguration configuration;
		
	/**
	 * @param configuration
	 */
	public ClientJavaEmitter(CodeGenConfiguration configuration) {		
		this.configuration = configuration;
	}
	
	public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
		this.configuration = configuration;

	}

	
	public void emitStub() throws CodeGenerationException{
		WSDLBinding axisBinding = this.configuration.getWom().getBinding(AxisBindingBuilder.AXIS_BINDING_QNAME);		
		ClientInterfaceWriter clientInterfaceWriter = new ClientInterfaceWriter(axisBinding.getBoundInterface(), this.configuration.getOutputLocation(), new DefaultTypeMapper());
		ClientStubWriter clientStubWriter = new ClientStubWriter(axisBinding.getBoundInterface(), this.configuration.getOutputLocation(), new DefaultTypeMapper());
		try {
			clientInterfaceWriter.emit();
			clientStubWriter.emit();
		} catch (IOException e) {
			throw new CodeGenerationException(e);
		}
	}

	
	public void emitSkeleton() {
		throw new UnsupportedOperationException("To be implemented");

	}

}
