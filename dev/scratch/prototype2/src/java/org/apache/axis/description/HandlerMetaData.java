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
import org.apache.axis.impl.description.ParameterIncludeImpl;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class HandlerMetaData implements ParameterInclude{
    private  ParameterInclude parameterInclude;
    private QName name;
    private PhaseRule rules;
    private Handler handler;
    private String className;


    public HandlerMetaData(){
       this.parameterInclude = new ParameterIncludeImpl();
       this.rules = new PhaseRule();
    }
    /**
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    public String getBefore() {
        return rules.getBefore();
    }

    public void setBefore(String before) {
        rules.setBefore(before);
    }

    public String getAfter() {
        return rules.getAfter();
    }

    public void setAfter(String after) {
        rules.setAfter(after);
    }

    public String getPhaseName() {
        return rules.getPhaseName();
    }

    public void setPhaseName(String phaseName) {
        rules.setPhaseName(phaseName);
    }

    public boolean isPhaseFirst() {
        return rules.isPhaseFirst();
    }

    public void setPhaseFirst(boolean phaseFirst) {
        rules.setPhaseFirst(phaseFirst);
    }

    public boolean isPhaseLast() {
        return rules.isPhaseLast();
    }

    public void setPhaseLast(boolean phaseLast) {
        rules.setPhaseLast(phaseLast);
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
