package org.apache.axis.deployment;

import org.apache.axis.deployment.metadata.*;
import org.apache.axis.deployment.metadata.phaserule.PhaseException;
import org.apache.axis.deployment.metadata.phaserule.PhaseMetaData;

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
 *
 * @author Deepal Jayasinghe
 *         Oct 19, 2004
 *         11:41:32 AM
 *
 */


/**
 * This class is used to parse the following xml douments
 * 1 server.xml
 * 2 service.xml
 * 3 module.xml
 *
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

    public ServiceMetaData parseServiceXML() throws DeploymentException, PhaseException {
        //To check whether document end tag has encountered
        boolean END_DOCUMENT = false;
        ServiceMetaData service = null;

        try {
            while (!END_DOCUMENT) {
                int eventType =  pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (ST.equals(serviceXMLST)) {
                        service = procesServiceXML();
                        service.setName(archiveName);
                        service.setArchiveName(archiveName);
                    }
                    //processStartElement();
                    break;//todo this has to be chenfed only for testng
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
        return service;
    }

    /**
     * To process server.xml
     */
    public  void procesServerXML(ServerMetaData serverMetaData ) throws DeploymentException{
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
                    if(ST.equals(serverXMLST)){
                        int attribCount = pullparser.getAttributeCount();
                        if (attribCount > 0) {
                            for (int i = 0; i < attribCount; i++) {
                                String attname = pullparser.getAttributeLocalName(i);
                                String attvalue = pullparser.getAttributeValue(i);
                                if(attname.equals(ServerMetaData.SERVERNAME)){
                                    serverMetaData.setName(attvalue);
                                }
                            }
                        }
                    }   else  if (ST.equals(PARAMETERST)) {
                        ParameterMetaData parameter = processParameter();
                        serverMetaData.appParameter(parameter);
                    } else if (ST.equals(TYPEMAPPINGST)) {
                        processTypeMapping();
                    } else if (ST.equals(MODULEST)){
                        ModuleMetaData metaData = processModule();
                        serverMetaData.addModule(metaData);
                    } else if (ST.equals(HANDERST)){
                        HandlerMetaData handler = processHandler();
                        serverMetaData.addHandlers(handler);
                    }   else if (ST.equals(PHASE_ORDER)){
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
    private ServiceMetaData procesServiceXML() throws DeploymentException {
        int attribCount = pullparser.getAttributeCount();
        ServiceMetaData service = new ServiceMetaData();
        if (attribCount == 3) {
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (attname.equals(ServiceMetaData.PROVIDERNAME)) {
                    service.setProvider(getValue(attvalue));
                } else if (attname.equals(ServiceMetaData.STYLENAME)) {
                    service.setStyle(getValue(attvalue));
                } else if (attname.equals(ServiceMetaData.CONTEXTPATHNAME)) {
                    service.setContextPath(getValue(attvalue));
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
                        ParameterMetaData parameter = processParameter();
                        service.appParameter(parameter);
                    } else if (ST.equals(TYPEMAPPINGST)) {
                        processTypeMapping();
                    } else if (ST.equals(BEANMAPPINGST)) {
                        processBeanMapping();
                    } else if (ST.equals(OPRATIONST)) {
                        OperationMetaData operation = processOperation();
                        service.setOperation(operation);
                    } else if (ST.equals(INFLOWST)) {
                        InFlowMetaData inFlow = processInFlow();
                        service.setInFlow(inFlow);
                    } else if (ST.equals(OUTFLOWST)) {
                        OutFlowMetaData outFlow = processOutFlow();
                        service.setOutFlow(outFlow);
                    } else if (ST.equals(FAILTFLOWST)) {
                        FaultFlowMetaData faultFlow = processFaultFlow();
                        service.setFaultFlow(faultFlow);
                    } else if (ST.equals(MODULEST)){
                        ModuleMetaData moduleMetaData = getModule();//processModule();
                        service.addModules(moduleMetaData);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
        return service;
    }

    /**
     * This will process the <ParameterMetaData>....</ParameterMetaData> tag and craete a
     * ParameterMetaData object using those values
     * @return  ParameterMetaData
     * @throws org.apache.axis.deployment.DeploymentException
     */
    private ParameterMetaData processParameter() throws DeploymentException {
        String name = pullparser.getLocalName();
        ParameterMetaData parameter = new ParameterMetaData(name);
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 2) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (attname.equals(ParameterMetaData.ATTNAME)) {
                    parameter.setName(attvalue);
                } else if (attname.equals(ParameterMetaData.ATTLOCKED)) {
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
                int eventType =  pullparser.next();
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
                }  else if (eventType == XMLStreamConstants.CHARACTERS) {
                    element = element + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
        // adding element to the parameter
        parameter.setElement(element);
        return parameter;
    }

    /**
     * this method is to process the HandlerMetaData tag in the either service.xml or server.xml
     * @return HandlerMetaData object
     * @throws org.apache.axis.deployment.DeploymentException
     */
    private HandlerMetaData processHandler() throws DeploymentException {
      //  String name = pullparser.getLocalName();
        boolean ref_name = false;
        HandlerMetaData handler = new HandlerMetaData();
        int attribCount = pullparser.getAttributeCount();

        for (int i = 0; i < attribCount; i++) {
            String attname = pullparser.getAttributeLocalName(i);
            String attvalue = pullparser.getAttributeValue(i);

            if (attname.equals(HandlerMetaData.CLASSNAME)) {
                handler.setClassName(attvalue);
            } else if (attname.equals(HandlerMetaData.NAME)) {
                if (ref_name) {
                    throw new DeploymentException("Hander canot have both name and ref  " + attvalue);
                } else {
                    handler.setName(attvalue);
                    ref_name = true;
                }
            } else if (attname.equals(HandlerMetaData.REF)) {
                if (ref_name) {
                    throw new DeploymentException("Hander canot have both name and ref  " + attvalue);
                } else {
                    handler.setRef(attvalue);
                    ref_name = true;
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
                    if (tagnae.equals(HandlerMetaData.ORDER)) {
                        attribCount = pullparser.getAttributeCount();
                        for (int i = 0; i < attribCount; i++) {
                            String attname = pullparser.getAttributeLocalName(i);
                            String attvalue = pullparser.getAttributeValue(i);

                            if (attname.equals(HandlerMetaData.AFTER)) {
                                handler.setAfter(attvalue);
                            } else if (attname.equals(HandlerMetaData.BEFORE)) {
                                handler.setBefore(attvalue);
                            } else if (attname.equals(HandlerMetaData.PHASE)) {
                                handler.setPhase(attvalue);
                            } else if (attname.equals(HandlerMetaData.PHASEFIRST)) {
                                String boolval = getValue(attvalue);
                                if (boolval.equals("true")) {
                                    handler.setPhaseFirst(true);
                                } else if (boolval.equals("false")) {
                                    handler.setPhaseFirst(false);
                                }
                            } else if (attname.equals(HandlerMetaData.PHASELAST)) {
                                String boolval = getValue(attvalue);
                                if (boolval.equals("true")) {
                                    handler.setPhaseLast(true);
                                } else if (boolval.equals("false")) {
                                    handler.setPhaseLast(false);
                                }
                            }

                        }
                    } else if (tagnae.equals(PARAMETERST)) {
                        ParameterMetaData parameter = processParameter();
                        handler.addParameter(parameter);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String endtagname = pullparser.getLocalName();
                    if (endtagname.equals(HANDERST)) {
                        END_HANDLER = true;
                        break;
                    }
                }else if (eventType == XMLStreamConstants.CHARACTERS) {
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
     * @throws org.apache.axis.deployment.DeploymentException
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
                }  else if (eventType == XMLStreamConstants.CHARACTERS) {
                    text = text + pullparser.getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
    }


    private OperationMetaData processOperation() throws DeploymentException {
      //  String name = pullparser.getLocalName();
        OperationMetaData operation = new OperationMetaData();
        int attribCount = pullparser.getAttributeCount();
        if (attribCount == 4) {  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);
                if (attname.equals(OperationMetaData.ATNAME)) {
                    operation.setName(attvalue);
                } else if (attname.equals(OperationMetaData.ATQNAME)) {
                    operation.setQname(attvalue);
                } else if (attname.equals(OperationMetaData.ATSTYLE)) {
                    operation.setStyle(attvalue);
                } else if (attname.equals(OperationMetaData.ATUSE)) {
                    operation.setUse(attvalue);
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
                        ModuleMetaData module = processModule();
                        operation.setModule(module);
                    } else if (ST.equals(FAILTFLOWST)) {
                        FaultFlowMetaData faultFlow = processFaultFlow();
                        operation.setFaultFlow(faultFlow);
                    } else if (ST.equals(INFLOWST)) {
                        InFlowMetaData inFlow = processInFlow();
                        operation.setInFlow(inFlow);
                    } else if (ST.equals(OUTFLOWST)) {
                        OutFlowMetaData outFlow = processOutFlow();
                        operation.setOutFlow(outFlow);
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
     * @throws org.apache.axis.deployment.DeploymentException
     */
    public void processBeanMapping() throws DeploymentException {
        //todo complete this method
        // and modify to return a type mapping object
        boolean END_BEANMAPPING = false;
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_BEANMAPPING) {
                int eventType =pullparser.next();
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
// document end tag met , break the loop
// but the doc end tag wont meet here :)
                    END_BEANMAPPING = true;
                }  else if (eventType == XMLStreamConstants.END_ELEMENT) {
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

    private  ModuleMetaData getModule() throws DeploymentException{
        String moduleref ="";
        int attribCount = pullparser.getAttributeCount();
     //   boolean ref_name = false;

        if(attribCount > 0 ){
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);

                if (attname.equals(ModuleMetaData.REF)) {
                    moduleref  = attvalue;
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
        ModuleMetaData module = dpengine.getModule(moduleref);
        if (module == null){
            throw new DeploymentException(moduleref + " is not a valid module ");
        } else
            return module;
    }

    public ModuleMetaData processModule() throws DeploymentException {
        ModuleMetaData module = new ModuleMetaData();
        int attribCount = pullparser.getAttributeCount();
        boolean ref_name = false;

        if(attribCount > 0 ){
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeLocalName(i);
                String attvalue = pullparser.getAttributeValue(i);

                if (attname.equals(ModuleMetaData.CLASSNAME)) {
                    module.setClassName(attvalue);
                } else if (attname.equals(ModuleMetaData.NAME)) {
                    if (ref_name) {
                        throw new DeploymentException("Module canot have both name and ref  " + attvalue);
                    } else {
                        module.setName(attvalue);
                        ref_name = true;
                    }
                } else if (attname.equals(ModuleMetaData.REF)) {
                    if (ref_name) {
                        throw new DeploymentException("Module canot have both name and ref  " + attvalue);
                    } else {
                        module.setRef(attvalue);
                        ref_name = true;
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
                        ParameterMetaData parameter = processParameter();
                        module.addParameter(parameter);
                    } else if (ST.equals(FAILTFLOWST)) {
                        FaultFlowMetaData faultFlow = processFaultFlow();
                        module.setFaultFlow(faultFlow);
                    } else if (ST.equals(INFLOWST)) {
                        InFlowMetaData inFlow = processInFlow();
                        module.setInFlow(inFlow);
                    } else if (ST.equals(OUTFLOWST)) {
                        OutFlowMetaData outFlow = processOutFlow();
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
        }catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }

        return module;
    }

    public InFlowMetaData processInFlow() throws DeploymentException {
        InFlowMetaData inFlow = new InFlowMetaData();
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
        }  catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
        return inFlow;
    }


    public OutFlowMetaData processOutFlow() throws DeploymentException {
        OutFlowMetaData outFlow = new OutFlowMetaData();
        boolean END_OUTFLOW = false;
        String text = ""; // to store the paramater elemnt
        try {
            while (!END_OUTFLOW) {
                int eventType =  pullparser.next();
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


    public FaultFlowMetaData processFaultFlow() throws DeploymentException {
        FaultFlowMetaData faultFlow = new FaultFlowMetaData();
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
                }  else if (eventType == XMLStreamConstants.CHARACTERS) {
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


    public void processPhaseOrder(ServerMetaData server) throws DeploymentException{
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
                        if(attname.equals(PhaseMetaData.PHASE_NAME)){
                            PhaseMetaData phase = new PhaseMetaData(attvalue);
                            server.addPhases(phase);
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
        }  catch (Exception e) {
            throw new DeploymentException("Unknown process Exception", e);
        }
    }


    /**
     * this method is to get the value of attribue
     * eg xsd:anyVal --> anyVal
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
    public ModuleMetaData procesModuleXML() throws DeploymentException {
        boolean END_DOCUMENT = false;
        ModuleMetaData module = null;

        try {
            while (!END_DOCUMENT) {
                int eventType =  pullparser.next();
               if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String ST = pullparser.getLocalName();
                    if (ST.equals(moduleXMLST)) {
                        module = processModule();
                        module.setArchiveName(archiveName);
                        // module.setName(archiveName);
                    }
                    //processStartElement();
                    break;//todo this has to be chenfed only for testng
                }
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException("parser Exception", e);
        }
        return module;
    }
}
