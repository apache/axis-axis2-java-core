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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
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
    protected final HashMap parmeters;

    /**
     * Constructor ParameterIncludeImpl
     */
    public ParameterIncludeImpl() {
        parmeters = new HashMap();
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param)  {
        if (param != null) {
            parmeters.put(param.getName(), param);
        }
    }

    /**
     * Method getParameter
     *
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return (Parameter) parmeters.get(name);
    }

    public ArrayList getParameters() {
        Collection col =  parmeters.values();
        ArrayList para_list = new ArrayList();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Parameter parameter = (Parameter) iterator.next();
            para_list.add(parameter);
        }
        return para_list;
    }

    //to check whether the paramter is locked at any levle
    public boolean isParameterLocked(String paramterName) {
        return false;
    }

    /**
     * At the run time it can be able to change paramters , and system can save at any time and
     * to get the original state this method can be used
     * @param paramters <code>OMElement</code>
     * @throws org.apache.axis2.AxisFault
     */
    public void  deserializeParameters(OMElement paramters) throws AxisFault {
        Iterator parameters = paramters.getChildrenWithName(new QName(DeploymentConstants.PARAMETER));
        while (parameters.hasNext()) {
            //this is to check whether some one has locked the parmter at the top level
            OMElement parameterElement = (OMElement) parameters.next();

            Parameter parameter = new ParameterImpl();
            //setting parameterElement
            parameter.setParameterElement(parameterElement);

            //setting parameter Name
            OMAttribute paraName = parameterElement.getAttribute(
                    new QName(DeploymentConstants.ATTNAME));
            parameter.setName(paraName.getAttributeValue());

            //setting paramter Value (the chiled elemnt of the paramter)
            OMElement paraValue = parameterElement.getFirstElement();
            if(paraValue !=null){
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.OM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();
                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }
            //setting locking attribute
            OMAttribute paraLocked = parameterElement.getAttribute(
                    new QName(DeploymentConstants.ATTLOCKED));
            if (paraLocked !=null) {
                String lockedValue = paraLocked.getAttributeValue();
                if("true".equals(lockedValue)){
                    parameter.setLocked(true);
                } else {
                    parameter.setLocked(false);
                }
            }
            addParameter(parameter);
        }
    }
}
