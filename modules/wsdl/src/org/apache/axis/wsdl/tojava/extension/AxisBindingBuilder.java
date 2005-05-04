package org.apache.axis.wsdl.tojava.extension;

import javax.xml.namespace.QName;

import org.apache.axis.wsdl.tojava.CodeGenConfiguration;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLInterface;

/**
 * @author chathura@opensource.lk
 *
 */
public class AxisBindingBuilder extends AbstractCodeGenerationExtension implements CodeGenExtention {
	
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
