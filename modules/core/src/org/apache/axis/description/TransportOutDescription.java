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
package org.apache.axis.description;

import org.apache.axis.engine.Phase;
import org.apache.axis.phaseresolver.PhaseMetadata;
import org.apache.axis.transport.TransportSender;

import javax.xml.namespace.QName;

/**
 * Represents a transport deployed in AXis2
 */
public class TransportOutDescription
        implements ParameterInclude {
    /**
     * Field paramInclude
     */
    protected final ParameterInclude paramInclude;


    /**
     * Field flowInclude
     */
    private Flow outFlow;

    /**
     * Field flowInclude
     */
    private Flow faultFlow;

   private Phase outPhase;
   private  Phase faultPhase;

    /**
     * Field name
     */
    protected QName name;
    
    
    protected TransportSender sender;
    
    /**
     * Constructor AxisTransport
     *
     * @param name
     */
    public TransportOutDescription(QName name) {
        paramInclude = new ParameterIncludeImpl();
        this.name = name;
        outPhase = new Phase(PhaseMetadata.TRANSPORT_PHASE);
        faultPhase = new Phase(PhaseMetadata.TRANSPORT_PHASE);
    }

    /**
     * Method getParameter
     *
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) {
        paramInclude.addParameter(param);
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


    /**
     * @return
     */
    public TransportSender getSender() {
        return sender;
    }


    /**
     * @param sender
     */
    public void setSender(TransportSender sender) {
        this.sender = sender;
    }

    public Flow getOutFlow() {
        return outFlow;
    }

    public void setOutFlow(Flow outFlow) {
        this.outFlow = outFlow;
    }

    public Flow getFaultFlow() {
        return faultFlow;
    }

    public void setFaultFlow(Flow faultFlow) {
        this.faultFlow = faultFlow;
    }

    public Phase getOutPhase() {
        return outPhase;
    }

    public void setOutPhase(Phase outPhase) {
        this.outPhase = outPhase;
    }

    public Phase getFaultPhase() {
        return faultPhase;
    }

    public void setFaultPhase(Phase faultPhase) {
        this.faultPhase = faultPhase;
    }

}
