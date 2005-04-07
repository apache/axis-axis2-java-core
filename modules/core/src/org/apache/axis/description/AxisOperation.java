package org.apache.axis.description;

import javax.xml.namespace.QName;

import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.impl.WSDLOperationImpl;

/**
 * @author chathura@opensource.lk
 *
 */
public class AxisOperation extends WSDLOperationImpl implements
		ParameterInclude, WSDLOperation,DescriptionConstants {
	
	
	public AxisOperation(){
		this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
	}
	
	public AxisOperation(QName name){
		this();
		this.setName(name);
	}

	/**
     * Method addParameter
     *
     * @param param Parameter that will be added
     */
    public void addParameter(Parameter param) {
        if (param == null) {
            return;
        }
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        paramInclude.addParameter(param);
    }

   
    /**
     * Method getParameter
     *
     * @param name Name of the parameter
     * @return 
     */
    public Parameter getParameter(String name) {
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        return (Parameter) paramInclude.getParameter(name);
    }
}
