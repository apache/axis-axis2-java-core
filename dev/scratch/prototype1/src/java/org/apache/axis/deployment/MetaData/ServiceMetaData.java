package org.apache.axis.deployment.MetaData;

import org.apache.axis.deployment.MetaData.*;
import org.apache.axis.deployment.DeployCons;
import org.apache.axis.deployment.MetaData.phaserule.HandlerChainMetaData;
import org.apache.axis.deployment.MetaData.phaserule.HandlerChainMetaDataImpl;
import org.apache.axis.deployment.MetaData.phaserule.PhaseException;

import java.io.InputStream;
import java.util.Vector;

//import com.thoughtworks.xstream.XStream;

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
 *         3:10:34 PM
 *
 */


/**
 * actual service class which is to deserilize ServiceMetaData.xml
 */
public class ServiceMetaData implements DeployCons {

    public static String PROVIDERNAME = "provider";
    public static String STYLENAME = "style";

    //TODO Complte this class
    private Vector parameters = new Vector();
    private String provider;
    private String style;
    private String contextPath;

    // flow objects
    private OutFlowMetaData outFlow;
    private InFlowMetaData inFlow;
    private FaultFlowMetaData faultFlow;

    private OperationMetaData operation;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ServiceMetaData() {
        //jsut to clear the vector
        parameters.removeAllElements();
    }

    public OperationMetaData getOperation() {
        return operation;
    }

    public void setOperation(OperationMetaData operation) {
        this.operation = operation;
    }

    public OutFlowMetaData getOutFlow() {
        return outFlow;
    }

    public void setOutFlow(OutFlowMetaData outFlow) {
        this.outFlow = outFlow;
    }

    public InFlowMetaData getInFlow() {
        return inFlow;
    }

    public void setInFlow(InFlowMetaData inFlow) {
        this.inFlow = inFlow;
    }

    public FaultFlowMetaData getFaultFlow() {
        return faultFlow;
    }

    public void setFaultFlow(FaultFlowMetaData faultFlow) {
        this.faultFlow = faultFlow;
    }

    public static String CONTEXTPATHNAME = "contextPath";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void appParameter(ParameterMetaData parameter) {
        parameters.add(parameter);
    }

    public ParameterMetaData getParameter(int index) {
        return (ParameterMetaData) parameters.get(index);
    }

    public int getParametercount() {
        return parameters.size();
    }

    public void prinData() throws PhaseException {
     //   XStream xstream = new XStream();
    //    String xml = xstream.toXML(this);
    //    System.out.println("Serialization out put \n" + xml);

        HandlerMetaData[] handler = getHandlers();
        for (int i = 0; i < handler.length; i++) {
            System.out.println("Hander No " + (i + 1));
            handler[i].printMe();
        }

    }

    /***
     * This method is use to get all the handlers which are needed to
     * run the service , but Handlers which are belong to Global and Transport
     * not included here if it is requird it can be done
     *
     */
    public HandlerMetaData[] getHandlers() throws PhaseException {
        int noofhandlers = operation.getHandlerCount() + outFlow.getHandlercount() + inFlow.getHandlercount() +
                faultFlow.getHandlercount();
        HandlerMetaData[] handler = new HandlerMetaData[noofhandlers];
        int count = 0;

        for (int i = 0; i < operation.getHandlerCount(); i++) {
            handler[count] = operation.getHandlers()[i];
            count++;
        }
        for (int i = 0; i < inFlow.getHandlercount(); i++) {
            handler[count] = inFlow.getHandler(i);
            count++;
        }
        for (int i = 0; i < outFlow.getHandlercount(); i++) {
            handler[count] = outFlow.getHandler(i);
            count++;
        }
        for (int i = 0; i < faultFlow.getHandlercount(); i++) {
            handler[count] = faultFlow.getHandler(i);
            count++;
        }

        HandlerChainMetaData handlerChain = new HandlerChainMetaDataImpl();
        for (int i = 0; i < handler.length; i++) {
            HandlerMetaData temphandler = handler[i];
            handlerChain.addHandler(temphandler);
        }
        handler = handlerChain.getOrderdHandlers();

        return handler;
    }


    /**
     * this method is used to get all the handers for a given phase
     * @return
     */
    public HandlerMetaData[] getFlowHandlers(String flowName) throws PhaseException {
        int count = 0;
        int hcount = 0;
        HandlerMetaData[] handlers = new HandlerMetaData[0];
        if (flowName.equals(INFLOWST)) {
            count = getInFlow().getHandlercount() + operation.getInFlow().getHandlercount();
            handlers = new HandlerMetaData[count];
            for (int k = 0; k < getInFlow().getHandlercount(); k++) {
                handlers[hcount] = getInFlow().getHandler(k);
                hcount++;
            }

            for (int k = 0; k < operation.getInFlow().getHandlercount(); k++) {
                handlers[hcount] = operation.getInFlow().getHandler(k);
                hcount++;
            }

        } else if (flowName.equals(OUTFLOWST)) {
            count = getOutFlow().getHandlercount() + operation.getOutFlow().getHandlercount();
            handlers = new HandlerMetaData[count];
            for (int k = 0; k < getOutFlow().getHandlercount(); k++) {
                handlers[hcount] = getOutFlow().getHandler(k);
                hcount++;
            }

            for (int k = 0; k < operation.getOutFlow().getHandlercount(); k++) {
                handlers[hcount] = operation.getOutFlow().getHandler(k);
                hcount++;
            }
        } else if (flowName.equals(FAILTFLOWST)) {
            count = getFaultFlow().getHandlercount() + operation.getFaultFlow().getHandlercount();
            handlers = new HandlerMetaData[count];
            for (int k = 0; k < getFaultFlow().getHandlercount(); k++) {
                handlers[hcount] = getFaultFlow().getHandler(k);
                hcount++;
            }

            for (int k = 0; k < operation.getFaultFlow().getHandlercount(); k++) {
                handlers[hcount] = operation.getFaultFlow().getHandler(k);
                hcount++;
            }
        }

        HandlerChainMetaData handlerChain = new HandlerChainMetaDataImpl();
        for (int i = 0; i < handlers.length; i++) {
            HandlerMetaData handler = handlers[i];
            handlerChain.addHandler(handler);
        }
        handlers = handlerChain.getOrderdHandlers();
        return handlers;
    }


}
