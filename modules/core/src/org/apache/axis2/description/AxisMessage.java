package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.om.OMElement;

import java.util.ArrayList;

/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*/

/**
 * This class represent the messages in WSDL , and there can be message element in services.xml
 * those will be representd by this class
 */
public class AxisMessage implements ParameterInclude {
    private ArrayList handlerChain;
    private ParameterInclude parameterinclude;
    private AxisOperation parent;

    public AxisMessage() {
        parameterinclude = new ParameterIncludeImpl();
        handlerChain = new ArrayList();
    }

    public void addParameter(Parameter param) throws AxisFault {
        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else {
            parameterinclude.addParameter(param);
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        parameterinclude.deserializeParameters(parameterElement);
    }

    public ArrayList getMessageFlow() {
        return handlerChain;
    }

    public Parameter getParameter(String name) {
        return parameterinclude.getParameter(name);
    }

    public ArrayList getParameters() {
        return parameterinclude.getParameters();
    }

    public AxisOperation getParent() {
        return parent;
    }

    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean loscked = false;

        if (getParent() != null) {
            loscked = getParent().isParameterLocked(parameterName);
        }

        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    public void setMessageFlow(ArrayList operationFlow) {
        this.handlerChain = operationFlow;
    }

    public void setParent(AxisOperation parent) {
        this.parent = parent;
    }
}
