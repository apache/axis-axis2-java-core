package org.apache.axis.deployment;

import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.description.FlowImpl;
import org.apache.axis.impl.description.ParameterImpl;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.engine.EngineRegistryImpl;
import org.apache.axis.phaseresolver.PhaseException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;


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
    //module.xml strating tag
    private static final String MODULEXMLST = "module";
    // service.xml strating tag
    private static final String SERVICEXMLST = "service";
    //to get the input stream
    private InputStream inputStream = null;
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
        pullparser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
    }


    public DeploymentParser(InputStream inputStream, DeploymentEngine engine) throws XMLStreamException {
        this.inputStream = inputStream;
        this.dpengine = engine;
        pullparser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
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
                    if (ST.equals(SERVICEXMLST)) {
                        procesServiceXML(axisService);
                        axisService.setName(new QName(getShortFileName(dpengine.getCurrentFileItem().getFile().getName())));
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
                    if (PARAMETERST.equals(ST)) {
                        Parameter parameter = processParameter();
                        serverMetaData.addParameter(parameter);
                    } else if (TRANSPORTSTAG.equals(ST)) {
                        ArrayList trasports = processTransport();
                        for (int i = 0; i < trasports.size(); i++) {
                            dpengine.getEngineRegistry().addTransport((AxisTransport)trasports.get(i));
                        }
                        
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
                        ((EngineRegistryImpl)dpengine.getEngineRegistry()).setPhases(processPhaseOrder());
                    } else if(SERVERST.equals(ST)){
                        //TODO process attributes
                    }  else {
                        throw new UnsupportedOperationException(ST + " element is not allowed in the server.xml");
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (AxisFault e) {
            throw new DeploymentException(e);
        }
    }


    public ArrayList processTransport() throws DeploymentException {
        boolean END_TRANSPORTS = false;
        ArrayList transportList = new ArrayList();
        AxisTransport transport = null;
        
        
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_TRANSPORTS) {
                int eventType = pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    END_TRANSPORTS = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String tagnae = pullparser.getLocalName();
                    if (TRANSPORTTAG.equals(tagnae)) {
                        String attname = pullparser.getAttributeLocalName(0);
                        String attvalue = pullparser.getAttributeValue(0);
                        if (ATTNAME.equals(attname)) {
                            transport = new AxisTransport(new QName(attvalue));
                            transportList.add(transport);
                        }
                    }else if (transport != null && PARAMETERST.equals(tagnae)) {
                        Parameter parameter =  processParameter();
                        transport.addParameter(parameter);
                        //axisService. .appParameter(parameter);
                    }  else if (transport != null && INFLOWST.equals(tagnae)) {
                        Flow inFlow = processInFlow();
                        transport.setInFlow(inFlow);
                    } else if (transport != null && OUTFLOWST.equals(tagnae)) {
                        Flow outFlow = processOutFlow();
                        transport.setOutFlow(outFlow);
                    } else if (transport != null && FAILTFLOWST.equals(tagnae)) {
                        Flow faultFlow = processFaultFlow();
                        transport.setFaultFlow(faultFlow);
                    } else{
                        throw new DeploymentException("Unknown element "+ tagnae);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (TRANSPORTSTAG.equals(endtagname)) {
                        END_TRANSPORTS = true;
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
        return transportList;
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
                if (PROVIDERNAME.equals(attname)) {
                    //TODO load the java clss for this
                    //TODO  somtimes Provider should be change
                    dpengine.getCurrentFileItem().setProvideName(attvalue);
                    //  Provider provider = new SimpleJavaProvider();
                    // provider. .setName(new QName(getValue(attvalue)));
                    // axisService.setProvider(provider);
                } else if (STYLENAME.equals(attname)) {
                    // axisService.setStyle();
                    //TODO setStyle should be handle latter
                } else if (CONTEXTPATHNAME.equals(attname)) {
                    axisService.setContextPath(getValue(attvalue));
                } else {
                    throw new DeploymentException("Bad arguments for the service" + axisService.getName());
                }
            }
        } else
            throw new DeploymentException("Bad arguments" +  axisService.getName());

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
                        Parameter parameter =  processParameter();
                        axisService.addParameter(parameter);
                        //axisService. .appParameter(parameter);
                    } else if (JAVAIMPL.equals(ST)){
                        if(JAVAST.equals(pullparser.getNamespaceURI())){
                            attribCount = pullparser.getAttributeCount();
                            if (attribCount > 0) {
                                for (int i = 0; i < attribCount; i++) {
                                    String attvalue = pullparser.getAttributeValue(i);
                                    dpengine.getCurrentFileItem().setClassName(attvalue);
                                }
                            }
                        } else {
                            throw new UnsupportedOperationException("Illegal namespace");
                        }
                    }else if (TYPEMAPPINGST.equals(ST)) {
                        throw new UnsupportedOperationException();
                        // todo this should implemnt latter
                        //  processTypeMapping();
                    } else if (BEANMAPPINGST.equals(ST)) {
                        throw new UnsupportedOperationException();
                        // todo this should implemnt latter
                        // processBeanMapping();
                    } else if (OPRATIONST.equals(ST)) {
                        AxisOperation  operation = processOperation();
                        axisService.addOperation(operation);
                    } else if (INFLOWST.equals(ST)) {
                        Flow inFlow = processInFlow();
                        axisService.setInFlow(inFlow);
                    } else if (OUTFLOWST.equals(ST)) {
                        Flow outFlow = processOutFlow();
                        axisService.setOutFlow(outFlow);
                    } else if (FAILTFLOWST.equals(ST)) {
                        Flow faultFlow = processFaultFlow();
                        axisService.setFaultFlow(faultFlow);
                    } else if (MODULEST.equals(ST)) {
                        attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if (REF.equals(attname)) {
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
        Parameter parameter = new ParameterImpl();
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 2) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (ATTNAME.equals(attname)){
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

            if (CLASSNAME.equals(attname)) {
                handler.setClassName(attvalue);
            } else if (ATTNAME.equals(attname)) {
                if (ref_name) {
                    throw new DeploymentException("Hander canot have both name and ref  " + attvalue);
                } else {
                    handler.setName(new QName(attvalue));
                    ref_name = true;
                }
            } else if (REF.equals(attname)) {
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
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (HANDERST.equals(endtagname)) {
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
                    if (TYPEMAPPINGST.equals(endtagname)) {
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
                    if (MODULEXMLST.equals(ST)) {
                        throw new UnsupportedOperationException("nexted elements are not allowed for M1");
                    } else if (FAILTFLOWST.equals(ST)) {
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
                    if (BEANMAPPINGST.equals(endtagname)) {
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

                if (ATTNAME.equals(attname)) {
                    if (ref_name) {
                        throw new DeploymentException("Module canot have both name and ref  " + attvalue);
                    } else {
                        module.setName(new QName(attvalue));
                        ref_name = true;
                    }
                } else if (REF.equals(attname)) {
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
                    if (PARAMETERST.equals(ST)) {
                        Parameter parameter = processParameter();
                        module.addParameter(parameter);
                    } else if (FAILTFLOWST.equals(ST)) {
                        Flow faultFlow = processFaultFlow();
                        module.setFaultFlow(faultFlow);
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
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetaData handler = processHandler();
                        inFlow.addHandler(handler);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (INFLOWST.equals(endtagname)) {
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
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetaData handler = processHandler();
                        outFlow.addHandler(handler);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (OUTFLOWST.equals(endtagname)) {
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
                    if (HANDERST.equals(tagnae)) {
                        HandlerMetaData handler = processHandler();
                        faultFlow.addHandler(handler);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (FAILTFLOWST.equals(endtagname)) {
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


    public ArrayList processPhaseOrder() throws DeploymentException {
        boolean END_PHASEORDER = false;
        ArrayList pahseList = new ArrayList();
        String text = ""; // to store the paramater elemnt
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
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (PHASE_ORDER.equals(endtagname)) {
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

    private String getShortFileName(String fileName){
        char seperator = '.';
        String value = null;
        int index = fileName.indexOf(seperator);
        if (index > 0) {
            value = fileName.substring(0 , index);
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
