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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.i18n.Messages;

import java.util.ArrayList;

/**
 * Represents the deployment information about the handler
 */
public class HandlerDescription implements ParameterInclude {

    /**
     * Field className
     */
    private String className;

    /**
     * Field handler
     */
    private Handler handler;

    /**
     * Field name
     */
    private String name;

    /**
     * Field parameterInclude
     */
    private final ParameterInclude parameterInclude;
    private ParameterInclude parent;

    /**
     * Field rules
     */
    private PhaseRule rules;

    /**
     * Constructor HandlerDescription.
     */
    public HandlerDescription() {
        this.parameterInclude = new ParameterIncludeImpl();
        this.rules = new PhaseRule();
    }

    /**
     * Constructor HandlerDescription.
     *
     * @param name
     */
    public HandlerDescription(String name) {
        this();
        this.name = name;
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameterInclude.addParameter(param);
        }
    }

    public void removeParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameterInclude.removeParameter(param);
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameterInclude.deserializeParameters(parameterElement);
    }

    /**
     * Method getClassName.
     *
     * @return Returns String.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return Returns Handler.
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * @return Returns QName.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name) {
        Parameter parameter = parameterInclude.getParameter(name);
        if (parameter == null && parent != null) {
            return parent.getParameter(name);
        } else {
            return parameter;
        }
    }

    public ArrayList getParameters() {
        return parameterInclude.getParameters();
    }

    public ParameterInclude getParent() {
        return parent;
    }

    /**
     * Method getRules.
     *
     * @return Returns PhaseRule.
     */
    public PhaseRule getRules() {
        return rules;
    }

    // to check whether the parameter is locked at any level
    public boolean isParameterLocked(String parameterName) {
        if (parent != null) {
            if (parent.isParameterLocked(parameterName)) {
                return true;
            }
        }

        return parameterInclude.isParameterLocked(parameterName);
    }

    /**
     * Method setClassName.
     *
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
        this.className = handler.getClass().getName();
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setParent(ParameterInclude parent) {
        this.parent = parent;
    }

    /**
     * Method setRules.
     *
     * @param rules
     */
    public void setRules(PhaseRule rules) {
        this.rules = rules;
    }
}
