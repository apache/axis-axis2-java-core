package org.apache.axis.deployment.metadata;

import java.util.Vector;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 18, 2004
 *         3:14:40 PM
 *
 */

/**
 * Either Desirialization of module.xml or <module>...</module> element
 * in service.xml
 */
public class ModuleMetaData {

    public static final String REF = "ref";
    public static final String CLASSNAME = "class";
    public static final String NAME = "name";

    private InFlowMetaData inFlow;
    private OutFlowMetaData outFlow;
    private FaultFlowMetaData faultFlow;

    private String ref;
    private String className;
    private String name;

    private Vector parameters = new Vector();

    public ModuleMetaData() {
        //just to clear the vector
        parameters.removeAllElements();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void addParameter(ParameterMetaData parameter) {
        parameters.add(parameter);
    }

    public ParameterMetaData getParameter(int index) {
        return (ParameterMetaData) parameters.get(index);
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public InFlowMetaData getInFlow() {
        return inFlow;
    }

    public void setInFlow(InFlowMetaData inFlow) {
        this.inFlow = inFlow;
    }

    public OutFlowMetaData getOutFlow() {
        return outFlow;
    }

    public void setOutFlow(OutFlowMetaData outFlow) {
        this.outFlow = outFlow;
    }

    public FaultFlowMetaData getFaultFlow() {
        return faultFlow;
    }

    public void setFaultFlow(FaultFlowMetaData faultFlow) {
        this.faultFlow = faultFlow;
    }





}
