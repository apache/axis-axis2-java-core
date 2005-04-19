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

package org.apache.axis.deployment;

import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfigurationImpl;
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.transport.TransportSender;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * This class is used to parse the following xml douments
 * 1 server.xml
 * 2 service.xml
 * 3 module.xml
 * <p/>
 * this class implements DeployCons to get some constant values need to
 * parse a given document
 */
public class DeploymentParser implements DeploymentConstants {
    //module.xml strating tag
    private static final String MODULEXMLST = "module";
    // service.xml strating tag
    private static final String SERVICEXMLST = "service";

    private XMLStreamReader pullparser;

    /**
     * referebce to the deployment engine
     */
    private DeploymentEngine dpengine;

    /**
     * constructor to parce service.xml
     *
     * @param inputStream
     * @param engine
     */
    public DeploymentParser(InputStream inputStream, DeploymentEngine engine) throws XMLStreamException {
        this.dpengine = engine;
        pullparser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
    }

    public void parseServiceXML(AxisService axisService) throws DeploymentException {
        //To check whether document end tag has encountered
        boolean END_DOCUMENT = false;
        //   ServiceMetaData service = null;
        try {
            while (!END_DOCUMENT) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    END_DOCUMENT = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (ST.equals(SERVICEXMLST)) {
                        procesServiceXML(axisService);
                        axisService.setName(new QName(getAxisServiceName(dpengine.getCurrentFileItem().getServiceName())));
                    }
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
    }

    /**
     * To process server.xml
     */
    public void procesServerXML(AxisGlobal serverMetaData) throws DeploymentException {
        try {
            boolean END_DOCUMENT = false;
            while (!END_DOCUMENT) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                    break;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName(); //Staring tag name
                    if (SERVERST.equals(ST)) {
                        //todo complete this to fill the names
                    } else if (PARAMETERST.equals(ST)) {
                        Parameter parameter = processParameter();
                        serverMetaData.addParameter(parameter);
                    } else if (TRANSPORTSENDER.equals(ST)) {
                        AxisTransportOut transportout = proccessTrasnsportOUT();
                        dpengine.getEngineconfig().addTransportOut(transportout);
                    } else if (TRANSPORTRECEIVER.equals(ST)) {
                        AxisTransportIn transportin = proccessTrasnsportIN();
                        dpengine.getEngineconfig().addTransportIn(transportin);
                    } else if (TYPEMAPPINGST.equals(ST)) {
                        throw new UnsupportedOperationException("Type Mappings are not allowed in server.xml");
                    } else if (MODULEST.equals(ST)) {
                        int attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (REF.equals(attname)) {
                                    serverMetaData.addModule(new QName(attvalue));
                                }
                            }
                        }
                    } else if (PHASE_ORDER.equals(ST)) {
                        int attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (TYPE.equals(attname)) {
                                    if (INFLOWST.equals(attvalue)) {
                                        ((EngineConfigurationImpl) dpengine.getEngineconfig()).setInPhases(processPhaseOrder());
                                    } else if (OUTFLOWST.equals(attvalue)) {
                                        ((EngineConfigurationImpl) dpengine.getEngineconfig()).setOutPhases(processPhaseOrder());
                                    } else if (IN_FAILTFLOW.equals(attvalue)) {
                                        ((EngineConfigurationImpl) dpengine.getEngineconfig()).setInFaultPhases(processPhaseOrder());
                                    } else if (OUT_FAILTFLOW.equals(attvalue)) {
                                        ((EngineConfigurationImpl) dpengine.getEngineconfig()).setOutFaultPhases(processPhaseOrder());
                                    } else {
                                        throw new DeploymentException("un defined flow type  " + ST);
                                    }
                                }
                            }
                        } else {
                            throw new DeploymentException("Flow type is a required attribute in " + ST);
                        }
                    } else {
                        throw new UnsupportedOperationException(ST + " element is not allowed in the server.xml");
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (SERVERST.equals(endtagname)) {
                        END_DOCUMENT = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (AxisFault e) {
            throw new DeploymentException(e);
        }
    }

    public AxisTransportIn proccessTrasnsportIN() throws DeploymentException {
        AxisTransportIn transportin = null;
        String attname = pullparser.getAttributeLocalName(0);
        String attvalue = pullparser.getAttributeValue(0);

        int attribCount = pullparser.getAttributeCount();
        for (int i = 0; i < attribCount; i++) {
            attname = pullparser.getAttributeLocalName(i);
            attvalue = pullparser.getAttributeValue(i);
            if (ATTNAME.equals(attname)) {
                transportin = new AxisTransportIn(new QName(attvalue));
            } else if (transportin != null && CLASSNAME.equals(attname)) {
                Class reciever = null;
                try {
                    reciever = Class.forName(attvalue, true, Thread.currentThread().getContextClassLoader());
                    TransportReceiver trnsrecievr = (TransportReceiver) reciever.newInstance();
                    transportin.setReciver(trnsrecievr);
                } catch (ClassNotFoundException e) {
                    throw  new DeploymentException(e.getMessage());
                } catch (IllegalAccessException e) {
                    throw  new DeploymentException(e.getMessage());
                } catch (InstantiationException e) {
                    throw  new DeploymentException(e.getMessage());
                }
            }
        }
        boolean END_TRANSPORTS = false;
        try {
            while (!END_TRANSPORTS) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    END_TRANSPORTS = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (transportin != null && PARAMETERST.equals(tagnae)) {
                        Parameter parameter = processParameter();
                        transportin.addParameter(parameter);
                    } else if (transportin != null && INFLOWST.equals(tagnae)) {
                        Flow inFlow = processInFlow();
                        transportin.setInFlow(inFlow);
                    } else if (transportin != null && OUTFLOWST.equals(tagnae)) {
                        throw new DeploymentException("OUTFlow dose not support in AxisTransportIN " + tagnae);
                    } else if (transportin != null && IN_FAILTFLOW.equals(tagnae)) {
                        Flow faultFlow = processInFaultFlow();
                        transportin.setFaultFlow(faultFlow);
                    } else {
                        throw new DeploymentException("Unknown element " + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (TRANSPORTRECEIVER.equals(endtagname)) {
                        END_TRANSPORTS = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return transportin;
    }

    public AxisTransportOut proccessTrasnsportOUT() throws DeploymentException {
        AxisTransportOut transportout = null;
        String attname;
        String attvalue;
        int attribCount = pullparser.getAttributeCount();
        for (int i = 0; i < attribCount; i++) {
            attname = pullparser.getAttributeLocalName(i);
            attvalue = pullparser.getAttributeValue(i);
            if (ATTNAME.equals(attname)) {
                transportout = new AxisTransportOut(new QName(attvalue));
            } else if (transportout != null && CLASSNAME.equals(attname)) {
                Class sender = null;
                try {
                    sender = Class.forName(attvalue, true, Thread.currentThread().getContextClassLoader());
                    TransportSender transportSender = (TransportSender) sender.newInstance();
                    transportout.setSender(transportSender);
                } catch (ClassNotFoundException e) {
                    throw  new DeploymentException(e.getMessage());
                } catch (IllegalAccessException e) {
                    throw  new DeploymentException(e.getMessage());
                } catch (InstantiationException e) {
                    throw  new DeploymentException(e.getMessage());
                }
            }
        }
        boolean END_TRANSPORTS = false;
        try {
            while (!END_TRANSPORTS) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    END_TRANSPORTS = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (transportout != null && PARAMETERST.equals(tagnae)) {
                        Parameter parameter = processParameter();
                        transportout.addParameter(parameter);
                    } else if (transportout != null && INFLOWST.equals(tagnae)) {
                        throw new DeploymentException("InFlow dose not support in AxisTransportOut  " + tagnae);
                    } else if (transportout != null && OUTFLOWST.equals(tagnae)) {
                        Flow outFlow = processOutFlow();
                        transportout.setOutFlow(outFlow);
                    } else if (transportout != null && OUT_FAILTFLOW.equals(tagnae)) {
                        Flow faultFlow = processOutFaultFlow();
                        transportout.setFaultFlow(faultFlow);
                    } else {
                        throw new DeploymentException("Unknown element " + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (TRANSPORTSENDER.equals(endtagname)) {
                        END_TRANSPORTS = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return transportout;
    }

    /**
     * to process service.xml
     */
    private void procesServiceXML(AxisService axisService) throws DeploymentException {
        int attribCount = pullparser.getAttributeCount();
        boolean foundMR = false;
        if (attribCount >= 1) {
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (MESSAGERECEIVER.equals(attname)) {
                    dpengine.getCurrentFileItem().setMessgeReceiver(attvalue);
                    foundMR = true;
                } else if (STYLENAME.equals(attname)) {
                    axisService.setStyle(attvalue);
                } else if (CONTEXTPATHNAME.equals(attname)) {
                    axisService.setContextPath(getValue(attvalue));
                } else {
                    throw new DeploymentException("Bad arguments for the service" + axisService.getName());
                }
            }
        } else
            throw new DeploymentException("Bad arguments" + axisService.getName());
        if (!foundMR) {
            throw new DeploymentException("Message Receiver dose not specify " + axisService.getName());
        }

        //*********************************************************************************************//
        // This is to process remainng part of the document
        /**
         * to check whether the End tage of the document has met
         */
        boolean END_DOCUMENT = false;
        try {
            while (!END_DOCUMENT) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                    break;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName(); //Staring tag name
                    if (PARAMETERST.equals(ST)) {
                        Parameter parameter = processParameter();
                        axisService.addParameter(parameter);
                        //axisService. .appParameter(parameter);
                    } else if (TYPEMAPPINGST.equals(ST)) {
                        throw new UnsupportedOperationException();
                        // todo this should implemnt latter
                        //  processTypeMapping();
                    } else if (BEANMAPPINGST.equals(ST)) {
                        throw new UnsupportedOperationException();
                        // todo this should implemnt latter
                        // processBeanMapping();
                    } else if (OPRATIONST.equals(ST)) {
                        AxisOperation operation = processOperation();
                        axisService.addOperation(operation);
                    } else if (INFLOWST.equals(ST)) {
                        Flow inFlow = processInFlow();
                        axisService.setInFlow(inFlow);
                    } else if (OUTFLOWST.equals(ST)) {
                        Flow outFlow = processOutFlow();
                        axisService.setOutFlow(outFlow);
                    } else if (IN_FAILTFLOW.equals(ST)) {
                        Flow faultFlow = processInFaultFlow();
                        axisService.setFaultInFlow(faultFlow);
                    } else if (OUT_FAILTFLOW.equals(ST)) {
                        Flow faultFlow = processOutFaultFlow();
                        axisService.setFaultOutFlow(faultFlow);
                    } else if (MODULEST.equals(ST)) {
                        attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (REF.equals(attname)) {
                                    if (dpengine.getModule(new QName(attvalue)) == null) {
                                        throw new DeploymentException(ST + " module is invalid or dose not have bean deployed");
                                    } else
                                        axisService.addModule(new QName(attvalue));
                                }
                            }
                        }

                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + ST);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (AxisFault axisFault) {
            throw new DeploymentException("Module referece error , module dosenoot exist !!");
        }
    }


    private Parameter processParameter() throws DeploymentException {
        Parameter parameter = new ParameterImpl();
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 2) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (ATTNAME.equals(attname)) {
                    parameter.setName(attvalue);
                } else if (ATTLOCKED.equals(attname)) {
                    String boolval = getValue(attvalue);
                    if (boolval.equals("true")) {
                        parameter.setLocked(true);
                    } else if (boolval.equals("false")) {
                        parameter.setLocked(false);
                    }
                }

            }
        } else {
            throw new DeploymentException("bad parameter arguments");
        }

        boolean END_PARAMETER = false;
        String element = ""; // to store the paramater elemnt
        //todo this should change to support xsdany
        try {
            while (!END_PARAMETER) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_PARAMETER = true;
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (PARAMETERST.equals(endtagname)) {
                        END_PARAMETER = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    element += pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        // adding element to the parameter
        parameter.setValue(element);
        return parameter;
    }

    /**
     * this method is to process the HandlerMetaData tag in the either service.xml or server.xml
     *
     * @return HandlerMetaData object
     * @throws org.apache.axis.deployment.DeploymentException
     *
     */
    private HandlerMetadata processHandler() throws DeploymentException {
        //  String name = pullparser.getLocalName();
        boolean ref_name = false;
        HandlerMetadata handler = new HandlerMetadata();
        int attribCount = pullparser.getAttributeCount();

        for (int i = 0; i < attribCount; i++) {
            String attname = pullparser.getAttributeLocalName(i);
            String attvalue = pullparser.getAttributeValue(i);

            if (CLASSNAME.equals(attname)) {
                handler.setClassName(attvalue);
            } else if (ATTNAME.equals(attname)) {
                if (ref_name) {
                    throw new DeploymentException("Hanlder canot have both name and ref  " + attvalue);
                } else {
                    handler.setName(new QName(attvalue));
                    ref_name = true;
                }
            } else if (REF.equals(attname)) {
                if (ref_name) {
                    throw new DeploymentException("Hanlder canot have both name and ref  " + attvalue);
                } else {
                    ref_name = true;
                    throw new UnsupportedOperationException("This should be implmented");
                    //TODO implement this

                }
            }
        }

        boolean END_HANDLER = false;
        try {
            while (!END_HANDLER) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_HANDLER = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (ORDER.equals(tagnae)) {
                        attribCount = pullparser.getAttributeCount();
                        for (int i = 0; i < attribCount; i++) {
                            String attname = pullparser.getAttributeLocalName(i);
                            String attvalue = pullparser.getAttributeValue(i);

                            if (AFTER.equals(attname)) {
                                handler.getRules().setAfter(attvalue);
                            } else if (BEFORE.equals(attname)) {
                                handler.getRules().setBefore(attvalue);
                            } else if (PHASE.equals(attname)) {
                                handler.getRules().setPhaseName(attvalue);
                            } else if (PHASEFIRST.equals(attname)) {
                                String boolval = getValue(attvalue);
                                if (boolval.equals("true")) {
                                    handler.getRules().setPhaseFirst(true);
                                } else if (boolval.equals("false")) {
                                    handler.getRules().setPhaseFirst(false);
                                }
                            } else if (PHASELAST.equals(attname)) {
                                String boolval = getValue(attvalue);
                                if (boolval.equals("true")) {
                                    handler.getRules().setPhaseLast(true);
                                } else if (boolval.equals("false")) {
                                    handler.getRules().setPhaseLast(false);
                                }
                            }

                        }
                    } else if (tagnae.equals(PARAMETERST)) {
                        Parameter parameter = processParameter();
                        handler.addParameter(parameter);
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (HANDERST.equals(endtagname)) {
                        END_HANDLER = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        // adding element to the parameter
        return handler;
    }


    /**
     * This method used to process the <typeMapping>..</typeMapping> tag
     * in the service.xml
     *
     * @throws org.apache.axis.deployment.DeploymentException
     *
     */
    public void processTypeMapping() throws DeploymentException {
        //todo complete this method
        // and modify to return a type mapping object
        boolean END_TYPEMAPPING = false;
        try {
            while (!END_TYPEMAPPING) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_TYPEMAPPING = true;
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (TYPEMAPPINGST.equals(endtagname)) {
                        END_TYPEMAPPING = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }


    private AxisOperation processOperation() throws DeploymentException {
        //  String name = pullparser.getLocalName();
        AxisOperation operation = new AxisOperation();
        int attribCount = pullparser.getAttributeCount();
        if (attribCount < 5) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (ATTNAME.equals(attname)) {
                    operation.setName(new QName(attvalue));
                } else if (ATQNAME.equals(attname)) {
                    //TODO fill this after getting the reply for the mail
                    //operation.setQname(attvalue);
                } else if (STYLENAME.equals(attname)) {
                    //TODO to be implementd after clarfing style
                    //operation.setStyle(attvalue);
                } else if (ATUSE.equals(attname)) {
                    //TODO this is to be implemnt
                    //  operation.setUse(attvalue);
                }
            }
        } else {
            throw new DeploymentException("bad parameter arguments");
        }

        boolean END_OPERATION = false;
        try {
            while (!END_OPERATION) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_OPERATION = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (MODULEXMLST.equals(ST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (IN_FAILTFLOW.equals(ST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (INFLOWST.equals(ST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (OUTFLOWST.equals(ST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    }

                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (OPRATIONST.equals(endtagname)) {
                        END_OPERATION = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
        return operation;
    }


    /**
     * This method used to process the <typeMapping>..</typeMapping> tag
     * in the service.xml
     *
     * @throws org.apache.axis.deployment.DeploymentException
     *
     */
    public void processBeanMapping() throws DeploymentException {
        //todo complete this method
        // and modify to return a type mapping object
        boolean END_BEANMAPPING = false;
        try {
            while (!END_BEANMAPPING) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_BEANMAPPING = true;
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (BEANMAPPINGST.equals(endtagname)) {
                        END_BEANMAPPING = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
    }


    public void processModule(AxisModule module) throws DeploymentException {
        int attribCount = pullparser.getAttributeCount();
        boolean ref_name = false;
        boolean foundClass = false;

        if (attribCount > 0) {
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);

                if (ATTNAME.equals(attname)) {
                    if (ref_name) {
                        throw new DeploymentException("Module canot have both name and ref  " + attvalue);
                    } else {
                        module.setName(new QName(attvalue));
                        ref_name = true;
                    }
                }else if (CLASSNAME.equals(attname)) {
                    foundClass = true;
                    dpengine.getCurrentFileItem().setModuleClass(attvalue);
                }
                else if (REF.equals(attname)) {
                    if (ref_name) {
                        throw new DeploymentException("Module canot have both name and ref  " + attvalue);
                    } else {
                        //TODO implement this , boz this is not complete
                        //  module.setRef(attvalue);
                        ref_name = true;
                        throw new UnsupportedOperationException("This should be implemented");
                    }
                }
            }
        }
        boolean END_MODULE = false;
        try {
            while (!END_MODULE) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_MODULE = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (PARAMETERST.equals(ST)) {
                        Parameter parameter = processParameter();
                        module.addParameter(parameter);
                    } else if (IN_FAILTFLOW.equals(ST)) {
                        Flow faultFlow = processInFaultFlow();
                        module.setFaultInFlow(faultFlow);
                    } else if (OUT_FAILTFLOW.equals(ST)) {
                        Flow faultFlow = processOutFaultFlow();
                        module.setFaultOutFlow(faultFlow);
                    } else if (INFLOWST.equals(ST)) {
                        Flow inFlow = processInFlow();
                        module.setInFlow(inFlow);
                    } else if (OUTFLOWST.equals(ST)) {
                        Flow outFlow = processOutFlow();
                        module.setOutFlow(outFlow);
                    } else {
                        throw new UnsupportedOperationException(ST + "elment is not allowed in module.xml");
                    }
                    //todo has to be implemnt this
                    // complete implenatation
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (MODULEXMLST.equals(endtagname)) {
                        END_MODULE = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }

    }

    public Flow processInFlow() throws DeploymentException {
        Flow inFlow = new FlowImpl();
        boolean END_INFLOW = false;
        try {
            while (!END_INFLOW) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_INFLOW = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetadata handler = processHandler();
                        inFlow.addHandler(handler);
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (INFLOWST.equals(endtagname)) {
                        END_INFLOW = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return inFlow;
    }


    public Flow processOutFlow() throws DeploymentException {
        Flow outFlow = new FlowImpl();
        boolean END_OUTFLOW = false;
        try {
            while (!END_OUTFLOW) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_OUTFLOW = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetadata handler = processHandler();
                        outFlow.addHandler(handler);
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (OUTFLOWST.equals(endtagname)) {
                        END_OUTFLOW = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }

        return outFlow;
    }


    public Flow processInFaultFlow() throws DeploymentException {
        Flow faultFlow = new FlowImpl();
        boolean END_FAULTFLOW = false;
        try {
            while (!END_FAULTFLOW) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_FAULTFLOW = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetadata handler = processHandler();
                        faultFlow.addHandler(handler);
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (IN_FAILTFLOW.equals(endtagname)) {
                        END_FAULTFLOW = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return faultFlow;
    }

    public Flow processOutFaultFlow() throws DeploymentException {
        Flow faultFlow = new FlowImpl();
        boolean END_FAULTFLOW = false;
        try {
            while (!END_FAULTFLOW) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_FAULTFLOW = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetadata handler = processHandler();
                        faultFlow.addHandler(handler);
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (OUT_FAILTFLOW.equals(endtagname)) {
                        END_FAULTFLOW = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return faultFlow;
    }


    public ArrayList processPhaseOrder() throws DeploymentException {
        boolean END_PHASEORDER = false;
        ArrayList pahseList = new ArrayList();
        try {
            while (!END_PHASEORDER) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    END_PHASEORDER = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (PHASEST.equals(tagnae)) {
                        String attname = pullparser.getAttributeLocalName(0);
                        String attvalue = pullparser.getAttributeValue(0);
                        if (ATTNAME.equals(attname)) {
                            pahseList.add(attvalue);
                        }
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (PHASE_ORDER.equals(endtagname)) {
                        END_PHASEORDER = true;
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return pahseList;
    }


    /**
     * this method is to get the value of attribue
     * eg xsd:anyVal --> anyVal
     *
     * @return
     */
    private String getValue(String in) {
        char seperator = ':';
        String value = null;
        int index = in.indexOf(seperator);
        if (index > 0) {
            value = in.substring(index + 1, in.length());
            return value;
        }
        return in;
    }

    /**
     * This method is used to retrive service name form the arechive file name
     * if the archive file name is service1.aar , then axis service name would be service1
     *
     * @param fileName
     * @return
     */
    private String getAxisServiceName(String fileName) {
        char seperator = '.';
        String value = null;
        int index = fileName.indexOf(seperator);
        if (index > 0) {
            value = fileName.substring(0, index);
            return value;
        }
        return fileName;
    }

    /**
     * to process either module.xml or module elemnt in the service.xml
     */
    public void procesModuleXML(AxisModule module) throws DeploymentException {
        boolean END_DOCUMENT = false;
        try {
            while (!END_DOCUMENT) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (MODULEXMLST.equals(ST)) {
                        processModule(module);
                        // module.setArchiveName(archiveName);
                        // module.setName(archiveName);
                    } else {
                        throw new DeploymentException("parser Exception : un supported element" + ST);
                    }
                    //processStartElement();
                    break;//todo this has to be chenfed only for testng
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
    }
}
