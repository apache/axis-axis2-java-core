package org.apache.axis.deployment.metadata;


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
public class OperationMetaData {

    public static String ATNAME = "name";
    public static String ATQNAME = "name";
    public static String ATSTYLE = "name";
    public static String ATUSE = "name";

    private String name;
    private String qname;
    private String style;
    private String use;

    private ModuleMetaData module;
    private InFlowMetaData inFlow;
    private OutFlowMetaData outFlow;
    private FaultFlowMetaData faultFlow;

    public OperationMetaData() {
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

    public ModuleMetaData getModule() {
        return module;
    }

    public void setModule(ModuleMetaData module) {
        this.module = module;
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

    public int getHandlerCount() {
        return inFlow.getHandlercount() + outFlow.getHandlercount() + faultFlow.getHandlercount();
    }

    public HandlerMetaData[] getHandlers() {
        int noofhandrs = inFlow.getHandlercount() + outFlow.getHandlercount() + faultFlow.getHandlercount();
        HandlerMetaData[] temphandler = new HandlerMetaData[noofhandrs];
        int count = 0;
        for (int i = 0; i < inFlow.getHandlercount(); i++) {
            temphandler[count] = inFlow.getHandler(i);
            count++;
        }
        for (int i = 0; i < outFlow.getHandlercount(); i++) {
            temphandler[count] = outFlow.getHandler(i);
            count++;
        }
        for (int i = 0; i < faultFlow.getHandlercount(); i++) {
            temphandler[count] = faultFlow.getHandler(i);
            count++;
        }
        return temphandler;
    }
}
