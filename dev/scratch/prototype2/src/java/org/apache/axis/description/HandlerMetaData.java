/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.description;

import javax.xml.namespace.QName;

import org.apache.axis.engine.Handler;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class HandlerMetaData implements ParameterInclude{
    private  ParameterInclude parameterInclude;
    private QName name;
    private PhaseRule rules;
    private Handler handler;
    
    /**
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * @return
     */
    public PhaseRule getRules() {
        return rules;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * @param rule
     */
    public void setRules(PhaseRule rule) {
        rules = rule;
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param) {
        parameterInclude.addParameter(param);
    }

    /**
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return parameterInclude.getParameter(name);
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
}
