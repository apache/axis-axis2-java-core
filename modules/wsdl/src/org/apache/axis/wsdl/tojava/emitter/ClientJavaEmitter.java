package org.apache.axis.wsdl.tojava.emitter;

import java.io.IOException;

import org.apache.axis.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis.wsdl.tojava.CodeGenConfiguration;
import org.apache.axis.wsdl.tojava.CodeGenerationException;
import org.apache.axis.wsdl.tojava.extension.AxisBindingBuilder;
import org.apache.wsdl.WSDLBinding;

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
//		ClientStubWriter clientStubWriter = new ClientStubWriter(axisBinding.getBoundInterface(), this.configuration.getOutputLocation(), new DefaultTypeMapper());
		try {
			clientInterfaceWriter.emit();
//			clientStubWriter.emit();
		} catch (IOException e) {
			throw new CodeGenerationException(e);
		}
	}

	
	public void emitSkeleton() {
		throw new UnsupportedOperationException("To be implemented");

	}

}
