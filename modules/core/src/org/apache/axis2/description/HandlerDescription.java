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

import org.apache.axis2.AxisFault;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.engine.Handler;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * represent the deployment information about the handler
 */
public class HandlerDescription implements ParameterInclude {
    /**
     * Field parameterInclude
     */
    private final ParameterInclude parameterInclude;

    /**
     * Field name
     */
    private QName name;

    /**
     * Field rules
     */
    private PhaseRule rules;

    /**
     * Field handler
     */
    private Handler handler;

    /**
     * Field className
     */
    private String className;

    private ParameterInclude parent;

    /**
     * Constructor HandlerDescription
     */
    public HandlerDescription() {
        this.parameterInclude = new ParameterIncludeImpl();
        this.rules = new PhaseRule();
    }

    /**
     * Constructor HandlerDescription
     *
     * @param name
     */
    public HandlerDescription(QName name) {
        this();
        this.name = name;
    }

    /**
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * Method getRules
     *
     * @return
     */
    public PhaseRule getRules() {
        return rules;
    }

    /**
     * Method setRules
     *
     * @param rules
     */
    public void setRules(PhaseRule rules) {
        this.rules = rules;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault{
        if(isParameterLocked(param.getName())){
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else{
            parameterInclude.addParameter(param);
        }
    }

    /**
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return parameterInclude.getParameter(name);
    }

    public ArrayList getParameters() {
        return parameterInclude.getParameters();
    }

    //to check whether the parameter is locked at any levle
    public boolean isParameterLocked(String parameterName) {
        if(parent != null){
            if(parent.isParameterLocked(parameterName)){
                return true;
            }
        }
        return parameterInclude.isParameterLocked(parameterName);
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameterInclude.deserializeParameters(parameterElement);
    }

    /**
     * @return
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Method getClassName
     *
     * @return
     */
    public String getClassName() {
        return className;
    }

    /**
     * Method setClassName
     *
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    public ParameterInclude getParent() {
        return parent;
    }

    public void setParent(ParameterInclude parent) {
        this.parent = parent;
    }
}
