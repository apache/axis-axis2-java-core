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
*/


package org.apache.axis2.description;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class ParameterIncludeImpl
 */
public class ParameterIncludeImpl implements ParameterInclude {

    /**
     * Field parmeters
     */
    protected final HashMap parameters;

    /**
     * Constructor ParameterIncludeImpl.
     */
    public ParameterIncludeImpl() {
        parameters = new HashMap();
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) {
        if (param != null) {
            parameters.put(param.getName(), param);
        }
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    /**
     * Since at runtime it parameters may be modified
     * to get the original state this method can be used
     *
     * @param parameters <code>OMElement</code>
     * @throws AxisFault
     */
    public void deserializeParameters(OMElement parameters) throws AxisFault {
        Iterator iterator =
                parameters.getChildrenWithName(new QName(DeploymentConstants.TAG_PARAMETER));

        while (iterator.hasNext()) {

            // this is to check whether some one has locked the parmeter at the top level
            OMElement parameterElement = (OMElement) iterator.next();
            Parameter parameter = new Parameter();

            // setting parameterElement
            parameter.setParameterElement(parameterElement);

            // setting parameter Name
            OMAttribute paraName =
                    parameterElement.getAttribute(new QName(DeploymentConstants.ATTRIBUTE_NAME));

            parameter.setName(paraName.getAttributeValue());

            // setting parameter Value (the child element of the parameter)
            OMElement paraValue = parameterElement.getFirstElement();

            if (paraValue != null) {
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.OM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();

                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }

            // setting locking attribute
            OMAttribute paraLocked =
                    parameterElement.getAttribute(new QName(DeploymentConstants.ATTRIBUTE_LOCKED));

            if (paraLocked != null) {
                String lockedValue = paraLocked.getAttributeValue();

                if ("true".equals(lockedValue)) {
                    parameter.setLocked(true);
                } else {
                    parameter.setLocked(false);
                }
            }

            addParameter(parameter);
        }
    }

    /**
     * Method getParameter.
     *
     * @param name
     * @return Returns parameter.
     */
    public Parameter getParameter(String name) {
        return (Parameter) parameters.get(name);
    }

    public ArrayList getParameters() {
        Collection col = parameters.values();
        ArrayList para_list = new ArrayList();

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Parameter parameter = (Parameter) iterator.next();

            para_list.add(parameter);
        }

        return para_list;
    }

    // to check whether the parameter is locked at any level
    public boolean isParameterLocked(String parameterName) {
        return false;
    }
}
