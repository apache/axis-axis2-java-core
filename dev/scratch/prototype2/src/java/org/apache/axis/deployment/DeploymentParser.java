package org.apache.axis.deployment;

import org.apache.axis.deployment.metadata.ServerMetaData;
import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.description.FlowImpl;
import org.apache.axis.impl.description.ParameterImpl;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.providers.SimpleJavaProvider;
import org.apache.axis.phaseresolver.PhaseException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;


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
 */


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
    //server.xml starting tag
    private static final String serverXMLST = "server";
    //module.xml strating tag
    private static final String moduleXMLST = "module";
    // service.xml strating tag
    private static final String serviceXMLST = "service";
    //to get the input stream
    private InputStream inputStream;
    // Referance to XMLPullPasrser

    // private XmlPullParser pullparser;

    private XMLStreamReader pullparser;

    /**
     * referebce to the deployment engine
     */
    private DeploymentEngine dpengine;
    private String archiveName;

    /**
     * constructor to parce service.xml
     *
     * @param inputStream
     * @param engine
     * @param fileName
     */
    public DeploymentParser(InputStream inputStream, DeploymentEngine engine, String fileName) throws XMLStreamException {
        this.inputStream = inputStream;
        this.dpengine = engine;
        this.archiveName = fileName;

        //   try {
        pullparser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        //   } catch (XMLStreamException e) {
        //      e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        //  } catch (FactoryConfigurationError factoryConfigurationError) {
        //     factoryConfigurationError.printStackTrace();  //To change body of catch statement use Options | File Templates.
        // }
    }


    public DeploymentParser(InputStream inputStream, DeploymentEngine engine) throws XMLStreamException {
        this.inputStream = inputStream;
        this.dpengine = engine;
        //  try {
        pullparser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        // } catch (XMLStreamException e) {
        //    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        // } catch (FactoryConfigurationError factoryConfigurationError) {
        //    factoryConfigurationError.printStackTrace();  //To change body of catch statement use Options | File Templates.
        //}
    }

    public void parseServiceXML(AxisService axisService) throws DeploymentException, PhaseException {
        //To check whether document end tag has encountered
        boolean END_DOCUMENT = false;
        //   ServiceMetaData service = null;

        try {
            while (!END_DOCUMENT) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (ST.equals(serviceXMLST)) {
                        procesServiceXML(axisService);
                        axisService.setName(new QName(archiveName));
                    }
                    //processStartElement();
                    break;//todo this has to be chenfed only for testng
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
    }

    /**
     * To process server.xml
     */
    public void procesServerXML(ServerMetaData serverMetaData) throws DeploymentException {
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
                    if (ST.equals(serverXMLST)) {
                        int attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (attname.equals(ServerMetaData.SERVERNAME)) {
                                    serverMetaData.setName(attvalue);
                                }
                            }
                        }
                    } else if (ST.equals(PARAMETERST)) {
                        Parameter parameter = processParameter();
                        serverMetaData.appParameter(parameter);
                    } else if (ST.equals(TYPEMAPPINGST)) {
                        processTypeMapping();
                    } else if (ST.equals(MODULEST)) {
                        int attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (attname.equals(REF)) {
                                    serverMetaData.addModule(new QName(attvalue));
                                }
                            }
                        }
                    } else if (ST.equals(HANDERST)) {
                        HandlerMetaData handler = processHandler();
                        serverMetaData.addHandlers(handler);
                    } else if (ST.equals(PHASE_ORDER)) {
                        processPhaseOrder(serverMetaData);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (DeploymentException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * to process service.xml
     */
    private void procesServiceXML(AxisService axisService) throws DeploymentException {
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 3) {
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (attname.equals(PROVIDERNAME)) {
                    //TODO load the java clss for this
                    //TODO  somtimes Provider should be change
                    SimpleJavaProvider provider = new SimpleJavaProvider();
                    provider.setName(new QName(getValue(attvalue)));
                    axisService.setProvider(provider);
                } else if (attname.equals(STYLENAME)) {
                    // axisService.setStyle();
                    //TODO setStyle should be handle latter
                } else if (attname.equals(CONTEXTPATHNAME)) {
                    axisService.setContextPath(getValue(attvalue));
                } else {
                    throw new DeploymentException("Bad arguments");
                }
            }
        } else
            throw new DeploymentException("Bad arguments");

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
                    if (ST.equals(PARAMETERST)) {
                        Parameter parameter =  processParameter();
                        axisService.addParameter(parameter);
                        //axisService. .appParameter(parameter);
                    } else if (ST.equals(JAVAIMPL)){
                        if(pullparser.getNamespaceURI().equals(JAVAST)){
                            attribCount = pullparser.getAttributeCount();
                            if (attribCount > 0) {
                                for (int i = 0; i < attribCount; i++) {
                                    String attname = pullparser.getAttributeLocalName(i);
                                    String attvalue = pullparser.getAttributeValue(i);
                                    axisService.setServiceClassName(attvalue);
                                }
                            }
                        } else {
                            throw new UnsupportedOperationException("Illegal namespace");
                        }
                    }else if (ST.equals(TYPEMAPPINGST)) {
                        throw new UnsupportedOperationException();
                        // todo this should implemnt latter
                        //  processTypeMapping();
                    } else if (ST.equals(BEANMAPPINGST)) {
                        throw new UnsupportedOperationException();
                        // todo this should implemnt latter
                        // processBeanMapping();
                    } else if (ST.equals(OPRATIONST)) {
                        AxisOperation  operation = processOperation();
                        axisService.addOperation(operation);
                    } else if (ST.equals(INFLOWST)) {
                        Flow inFlow = processInFlow();
                        axisService.setInFlow(inFlow);
                    } else if (ST.equals(OUTFLOWST)) {
                        Flow outFlow = processOutFlow();
                        axisService.setOutFlow(outFlow);
                    } else if (ST.equals(FAILTFLOWST)) {
                        Flow faultFlow = processFaultFlow();
                        axisService.setFaultFlow(faultFlow);
                    } else if (ST.equals(MODULEST)) {
                        attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (attname.equals(REF)) {
                                    if(dpengine.getModule(new QName(attvalue)) == null){
                                        throw new DeploymentException(ST + " module is invalid or dose not have bean deployed");
                                    } else
                                        axisService.addModule(new QName(attvalue));
                                }
                            }
                        }

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
        String name = pullparser.getLocalName();
        Parameter parameter = new ParameterImpl();
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 2) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (attname.equals(ATTNAME)) {
                    parameter.setName(attvalue);
                } else if (attname.equals(ATTLOCKED)) {
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
                    if (endtagname.equals(PARAMETERST)) {
                        END_PARAMETER = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    element = element + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
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
    private HandlerMetaData processHandler() throws DeploymentException {
        //  String name = pullparser.getLocalName();
        boolean ref_name = false;
        HandlerMetaData handler = new HandlerMetaData();
        int attribCount = pullparser.getAttributeCount();

        for (int i = 0; i < attribCount; i++) {
            String attname = pullparser.getAttributeLocalName(i);
            String attvalue = pullparser.getAttributeValue(i);

            if (attname.equals(CLASSNAME)) {
                handler.setClassName(attvalue);
            } else if (attname.equals(ATTNAME)) {
                if (ref_name) {
                    throw new DeploymentException("Hander canot have both name and ref  " + attvalue);
                } else {
                    handler.setName(new QName(attvalue));
                    ref_name = true;
                }
            } else if (attname.equals(REF)) {
                if (ref_name) {
                    throw new DeploymentException("Hander canot have both name and ref  " + attvalue);
                } else {
                    ref_name = true;
                    throw new UnsupportedOperationException("This should be implmented");
                    //TODO implement this

                }
            }
        }

        boolean END_HANDLER = false;
        String element = ""; // to store the paramater elemnt
        //todo this should change to support xsdany
        try {
            while (!END_HANDLER) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_HANDLER = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (tagnae.equals(ORDER)) {
                        attribCount = pullparser.getAttributeCount();
                        for (int i = 0; i < attribCount; i++) {
                            String attname = pullparser.getAttributeLocalName(i);
                            String attvalue = pullparser.getAttributeValue(i);

                            if (attname.equals(AFTER)) {
                                handler.getRules().setAfter(attvalue);
                            } else if (attname.equals(BEFORE)) {
                                handler.getRules().setBefore(attvalue);
                            } else if (attname.equals(PHASE)) {
                                handler.getRules().setPhaseName(attvalue);
                            } else if (attname.equals(PHASEFIRST)) {
                                String boolval = getValue(attvalue);
                                if (boolval.equals("true")) {
                                    handler.getRules().setPhaseFirst(true);
                                } else if (boolval.equals("false")) {
                                    handler.getRules().setPhaseFirst(false);
                                }
                            } else if (attname.equals(PHASELAST)) {
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
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(HANDERST)) {
                        END_HANDLER = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    element = element + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
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
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_TYPEMAPPING) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_TYPEMAPPING = true;
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(TYPEMAPPINGST)) {
                        END_TYPEMAPPING = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
    }


    private AxisOperation processOperation() throws DeploymentException {
        //  String name = pullparser.getLocalName();
        AxisOperation operation = new SimpleAxisOperationImpl();
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 4) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (attname.equals(ATTNAME)) {
                    operation.setName(new QName(attvalue));
                } else if (attname.equals(ATQNAME)) {
                    //TODO fill this after getting the reply for the mail
                    //operation.setQname(attvalue);
                } else if (attname.equals(STYLENAME)) {
                    //TODO to be implementd after clarfing style
                    //operation.setStyle(attvalue);
                } else if (attname.equals(ATUSE)) {
                    //TODO this is to be implemnt
                    //  operation.setUse(attvalue);
                }
            }
        } else {
            throw new DeploymentException("bad parameter arguments");
        }

        boolean END_OPERATION = false;
        String text = ""; // to store the paramater elemnt
//todo this should change to support xsdany
        try {
            while (!END_OPERATION) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_OPERATION = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (ST.equals(moduleXMLST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (ST.equals(FAILTFLOWST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (ST.equals(INFLOWST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (ST.equals(OUTFLOWST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    }

                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(OPRATIONST)) {
                        END_OPERATION = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
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
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_BEANMAPPING) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_BEANMAPPING = true;
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(BEANMAPPINGST)) {
                        END_BEANMAPPING = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
    }


    public void processModule(AxisModule module) throws DeploymentException {
        int attribCount = pullparser.getAttributeCount();
        boolean ref_name = false;

        if (attribCount > 0) {
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);

                if (attname.equals(ATTNAME)) {
                    if (ref_name) {
                        throw new DeploymentException("Module canot have both name and ref  " + attvalue);
                    } else {
                        module.setName(new QName(attvalue));
                        ref_name = true;
                    }
                } else if (attname.equals(REF)) {
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
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_MODULE) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_MODULE = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (ST.equals(PARAMETERST)) {
                        Parameter parameter = processParameter();
                        module.addParameter(parameter);
                    } else if (ST.equals(FAILTFLOWST)) {
                        Flow faultFlow = processFaultFlow();
                        module.setFaultFlow(faultFlow);
                    } else if (ST.equals(INFLOWST)) {
                        Flow inFlow = processInFlow();
                        module.setInFlow(inFlow);
                    } else if (ST.equals(OUTFLOWST)) {
                        Flow outFlow = processOutFlow();
                        module.setOutFlow(outFlow);
                    }
                    //todo has to be implemnt this
                    // complete implenatation
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(moduleXMLST)) {
                        END_MODULE = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }

    }

    public Flow processInFlow() throws DeploymentException {
        Flow inFlow = new FlowImpl();
        boolean END_INFLOW = false;
        String text = ""; // to store the paramater elemnt
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
                    if (tagnae.equals(HANDERST)) {
                        HandlerMetaData handler = processHandler();
                        inFlow.addHandler(handler);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(INFLOWST)) {
                        END_INFLOW = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
        return inFlow;
    }


    public Flow processOutFlow() throws DeploymentException {
        Flow outFlow = new FlowImpl();
        boolean END_OUTFLOW = false;
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_OUTFLOW) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_OUTFLOW = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (tagnae.equals(HANDERST)) {
                        HandlerMetaData handler = processHandler();
                        outFlow.addHandler(handler);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(OUTFLOWST)) {
                        END_OUTFLOW = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }

        return outFlow;
    }


    public Flow processFaultFlow() throws DeploymentException {
        Flow faultFlow = new FlowImpl();
        boolean END_FAULTFLOW = false;
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_FAULTFLOW) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_FAULTFLOW = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (tagnae.equals(HANDERST)) {
                        HandlerMetaData handler = processHandler();
                        faultFlow.addHandler(handler);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(FAILTFLOWST)) {
                        END_FAULTFLOW = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
        return faultFlow;
    }


    public void processPhaseOrder(ServerMetaData server) throws DeploymentException {
        boolean END_PHASEORDER = false;
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_PHASEORDER) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    END_PHASEORDER = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (tagnae.equals(PHASEST)) {
                        String attname = pullparser.getAttributeLocalName(0);
                        String attvalue = pullparser.getAttributeValue(0);
                        if (attname.equals(ATTNAME)) {
                            server.addPhases(attvalue);
                        }
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(PHASE_ORDER)) {
                        END_PHASEORDER = true;
                        break;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
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
                    if (ST.equals(moduleXMLST)) {
                        processModule(module);
                        // module.setArchiveName(archiveName);
                        // module.setName(archiveName);
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
