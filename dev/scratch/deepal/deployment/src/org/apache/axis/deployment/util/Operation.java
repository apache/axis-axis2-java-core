package org.apache.axis.deployment.util;

import org.apache.axis.deployment.module.Module;

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
 *         Oct 21, 2004
 *         11:47:20 AM
 *
 */

/**
 * Opeartion
 */
public class Operation {

    public static String ATNAME = "name";
    public static String ATQNAME = "name";
    public static String ATSTYLE = "name";
    public static String ATUSE = "name";

    private String name;
    private String qname;
    private String style;
    private String use;

    private Module module;
    private InFlow inFlow;
    private OutFlow outFlow;
    private FaultFlow faultFlow;

    public Operation() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQname() {
        return qname;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public InFlow getInFlow() {
        return inFlow;
    }

    public void setInFlow(InFlow inFlow) {
        this.inFlow = inFlow;
    }

    public OutFlow getOutFlow() {
        return outFlow;
    }

    public void setOutFlow(OutFlow outFlow) {
        this.outFlow = outFlow;
    }

    public FaultFlow getFaultFlow() {
        return faultFlow;
    }

    public void setFaultFlow(FaultFlow faultFlow) {
        this.faultFlow = faultFlow;
    }
}
