package org.apache.axis.deployment.schemaparser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.mxp1.MXParserFactory;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.deployment.DeployCons;
import org.apache.axis.deployment.module.Module;
import org.apache.axis.deployment.util.*;
import org.apache.axis.deployment.service.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


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
public class SchemaParser implements DeployCons{
    //server.xml starting tag
    private static final String serverXMLST = "server";
    //module.xml strating tag
    private static final String moduleXMLST = "module";
    // service.xml strating tag
    private static final String serviceXMLST = "service";
    //to get the input stream
    private InputStream inputStream;
    // Referance to XMLPullPasrser
    private XmlPullParser pullparser;


    public SchemaParser(InputStream inputStream) {
        this.inputStream = inputStream;
        try{
            XmlPullParserFactory xmlPullParserFactory = MXParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
            pullparser = xmlPullParserFactory.newPullParser();
            pullparser.setInput(new InputStreamReader(inputStream));
        }catch(Exception e){
            //todo handle this exception in good manner
            e.printStackTrace();
        }

    }


    public void parseXML() throws DeploymentException{
        //To check whether document end tag has encountered
        boolean END_DOCUMENT = false;

        try{
            while (! END_DOCUMENT) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                }else if (eventType==XmlPullParser.START_TAG){
                    processStartElement();
                    break;//todo this has to be chenfed only for testng
                }else if (eventType==XmlPullParser.END_TAG){
                    // procesEndElement();
                }else if (eventType==XmlPullParser.CDSECT){
                    // processCDATA();
                }else if (eventType==XmlPullParser.COMMENT){
                    // processComment();
                }else if (eventType==XmlPullParser.TEXT){
                    // if the event is not white space processText will invoke
                    //  if(! pullparser.isWhitespace())
                    //  processText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }
    }


    private void processStartElement(){
        String ST = pullparser.getName();
        try{
            if(ST.equals(serviceXMLST)){
                Service service= procesServiceXML();
                service.prinData();
            }

        }catch(Exception e){
            System.out.println("Exception occure");
            e.printStackTrace();
        }

    }

    /**
     * To process server.xml
     */
    private void procesServerXML(){
        String name = pullparser.getName();
        String namspace = pullparser.getNamespace();
        System.out.println( name  + namspace);
        int attribCount =  pullparser.getAttributeCount();
        if(attribCount > 0){
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeName(i);
                String attprifix = pullparser.getAttributePrefix(i);
                String attnamespace = pullparser.getAttributeNamespace(i);
                String attvalue = pullparser.getAttributeValue(i);
                System.out.println(attname + attprifix + "="  +  attnamespace + attvalue);

            }
        }
    }

    /**
     * to process service.xml
     */
    private Service procesServiceXML()throws DeploymentException{
        int attribCount =  pullparser.getAttributeCount();
        Service service = new Service();
        if(attribCount == 3){
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeName(i);
                String attprifix = pullparser.getAttributePrefix(i);
                String attnamespace = pullparser.getAttributeNamespace(i); // attprifix is same as attval
                String attvalue = pullparser.getAttributeValue(i);
                if(attname.equals(Service.PROVIDERNAME)){
                    service.setProvider(getValue(attvalue));
                } else if(attname.equals(Service.STYLENAME)){
                    service.setStyle(getValue(attvalue));
                } else if(attname.equals(Service.CONTEXTPATHNAME)){
                    service.setContextPath(getValue(attvalue));
                } else {
                    throw new DeploymentException("Bad arguments");
                }
            }
        }else
            throw new DeploymentException("Bad arguments");

        //*********************************************************************************************//
        // This is to process remainng part of the document
        /**
         * to check whether the End tage of the document has met
         */
        boolean END_DOCUMENT = false;
        try{
            while (! END_DOCUMENT) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    END_DOCUMENT = true;
                    break;
                }else if (eventType==XmlPullParser.START_TAG){
                    String ST = pullparser.getName(); //Staring tag name
                    if(ST.equals(PARAMETERST)){
                        Parameter parameter=processParameter();
                        service.appParameter(parameter);
                    } else if(ST.equals(TYPEMAPPINGST)){
                        processTypeMapping();
                    } else if(ST.equals(BEANMAPPINGST)){
                        processBeanMapping();
                    } else if(ST.equals(OPRATIONST)){
                        Operation operation =  processOperation();
                        service.setOperation(operation);
                    } else if(ST.equals(INFLOWST)){
                        InFlow inFlow = processInFlow();
                        service.setInFlow(inFlow);
                    } else if(ST.equals(OUTFLOWST)){
                        OutFlow outFlow = processOutFlow() ;
                        service.setOutFlow(outFlow);
                    } else if(ST.equals(FAILTFLOWST)){
                        FaultFlow faultFlow = processFaultFlow();
                        service.setFaultFlow(faultFlow);
                    }
                }else if (eventType==XmlPullParser.END_TAG){
                    // this wont meet here :)
                    // and to be removed
                }else if (eventType==XmlPullParser.CDSECT){
                    // I think it is not need to support this
                }else if (eventType==XmlPullParser.COMMENT){
                    // I think it is not need to support this
                }else if (eventType==XmlPullParser.TEXT){
                    // this wont meet here :)
                    // and to be removed
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }
        return service;
    }

    /**
     * This will process the <Parameter>....</Parameter> tag and craete a
     * Parameter object using those values
     * @return  Parameter
     * @throws DeploymentException
     */
    private Parameter processParameter()throws DeploymentException{
        String name = pullparser.getName();
        Parameter parameter = new Parameter(name);
        int attribCount =  pullparser.getAttributeCount();
        if(attribCount == 2){  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeName(i);
                String attprifix = pullparser.getAttributePrefix(i);
                String attnamespace = pullparser.getAttributeNamespace(i); // attprifix is same as attval
                String attvalue = pullparser.getAttributeValue(i);
                if(attname.equals(Parameter.ATTNAME)){
                    parameter.setName(attvalue);
                } else if(attname.equals(Parameter.ATTLOCKED)){
                    String boolval = getValue(attvalue);
                    if(boolval.equals("true")){
                        parameter.setLocked(true);
                    }else if(boolval.equals("false")){
                        parameter.setLocked(false);
                    }
                }

            }
        } else{
            throw new DeploymentException("bad parameter arguments");
        }

        boolean END_PARAMETER = false;
        String element = ""; // to store the paramater elemnt
        //todo this should change to support xsdany
        try{
            while (! END_PARAMETER) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_PARAMETER = true;
                }else if (eventType==XmlPullParser.START_TAG){
                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(PARAMETERST)){
                        END_PARAMETER = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    element = element + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }
        // adding element to the parameter
        parameter.setElement(element);
        return parameter;
    }

    /**
     * This method used to process the <typeMapping>..</typeMapping> tag
     * in the service.xml
     * @throws DeploymentException
     */
    public void  processTypeMapping()throws DeploymentException{
        //todo complete this method
        // and modify to return a type mapping object
        boolean END_TYPEMAPPING = false;
        String text = ""; // to store the paramater elemnt
        try{
            while (! END_TYPEMAPPING) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_TYPEMAPPING = true;
                }else if (eventType==XmlPullParser.START_TAG){

                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(TYPEMAPPINGST)){
                        END_TYPEMAPPING = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }
    }


    private Operation processOperation()throws DeploymentException{
        String name = pullparser.getName();
        Operation  operation = new Operation();
        int attribCount =  pullparser.getAttributeCount();
        if(attribCount == 4){  // there should be two attributes
            for (int i = 0; i < attribCount; i++) {
                String attname = pullparser.getAttributeName(i);
                String attprifix = pullparser.getAttributePrefix(i);
                String attnamespace = pullparser.getAttributeNamespace(i); // attprifix is same as attval
                String attvalue = pullparser.getAttributeValue(i);
                if(attname.equals(Operation.ATNAME)){
                    operation.setName(attvalue);
                } else if(attname.equals(Operation.ATQNAME)){
                    operation.setQname(attvalue);
                } else if(attname.equals(Operation.ATSTYLE)){
                    operation.setStyle(attvalue);
                } else if(attname.equals(Operation.ATUSE)){
                    operation.setUse(attvalue);
                }
            }
        } else{
            throw new DeploymentException("bad parameter arguments");
        }

        boolean END_OPERATION = false;
        String text = ""; // to store the paramater elemnt
        //todo this should change to support xsdany
        try{
            while (! END_OPERATION) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_OPERATION = true;
                }else if (eventType==XmlPullParser.START_TAG){
                    String ST = pullparser.getName();
                    if(ST.equals(moduleXMLST)){
                        Module module= processModule();
                        operation.setModule(module);
                    } else if(ST.equals(FAILTFLOWST)){
                        FaultFlow faultFlow = processFaultFlow();
                        operation.setFaultFlow(faultFlow);
                    }else if(ST.equals(INFLOWST)){
                        InFlow inFlow = processInFlow();
                        operation.setInFlow(inFlow);
                    } else if(ST.equals(OUTFLOWST)){
                        OutFlow outFlow = processOutFlow();
                        operation.setOutFlow(outFlow);
                    }

                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(OPRATIONST)){
                        END_OPERATION = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }

        return operation;
    }



    /**
     * This method used to process the <typeMapping>..</typeMapping> tag
     * in the service.xml
     * @throws DeploymentException
     */
    public void  processBeanMapping()throws DeploymentException{
        //todo complete this method
        // and modify to return a type mapping object
        boolean END_BEANMAPPING = false;
        String text = ""; // to store the paramater elemnt
        try{
            while (! END_BEANMAPPING) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_BEANMAPPING = true;
                }else if (eventType==XmlPullParser.START_TAG){

                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(BEANMAPPINGST)){
                        END_BEANMAPPING = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }
    }

    public Module processModule() throws DeploymentException{
        Module module = new Module();
        int attribCount =  pullparser.getAttributeCount();
        if(attribCount == 1){  // there should be two attributes
            String attname = pullparser.getAttributeName(0);
            String attprifix = pullparser.getAttributePrefix(0);
            String attnamespace = pullparser.getAttributeNamespace(0); // attprifix is same as attval
            String attvalue = pullparser.getAttributeValue(0);

            module.setRef(attvalue);

        } else{
            throw new DeploymentException("bad parameter arguments");
        }

        boolean END_MODULE = false;
        String text = ""; // to store the paramater elemnt
        try{
            while (! END_MODULE) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_MODULE = true;
                }else if (eventType==XmlPullParser.START_TAG){
                    String ST= pullparser.getName();
                    if(ST.equals(PARAMETERST)){
                        Parameter parameter=processParameter();
                        module.addParameter(parameter);
                    }
                    //todo has to be implemnt this
                    // complete implenatation
                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(moduleXMLST)){
                        END_MODULE = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }

        return module;
    }

    public InFlow  processInFlow()throws DeploymentException{
        InFlow inFlow = new InFlow();
        boolean END_INFLOW = false;
        String text = ""; // to store the paramater elemnt
        try{
            while (! END_INFLOW) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_INFLOW = true;
                }else if (eventType==XmlPullParser.START_TAG){

                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(INFLOWST)){
                        END_INFLOW = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }

        return inFlow;
    }


    public OutFlow  processOutFlow()throws DeploymentException{
        OutFlow outFlow = new OutFlow();
        boolean END_OUTFLOW = false;
        String text = ""; // to store the paramater elemnt
        try{
            while (! END_OUTFLOW) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_OUTFLOW = true;
                }else if (eventType==XmlPullParser.START_TAG){

                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(OUTFLOWST)){
                        END_OUTFLOW = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }

        return outFlow;
    }


    public FaultFlow  processFaultFlow()throws DeploymentException{
        FaultFlow faultFlow = new FaultFlow();
        boolean END_FAULTFLOW = false;
        String text = ""; // to store the paramater elemnt
        try{
            while (! END_FAULTFLOW) {
                pullparser.next();
                int eventType = pullparser.getEventType();
                if (eventType==XmlPullParser.START_DOCUMENT){
                    // processStartDocument();
                }else if (eventType==XmlPullParser.END_DOCUMENT){
                    // document end tag met , break the loop
                    // but the doc end tag wont meet here :)
                    END_FAULTFLOW = true;
                }else if (eventType==XmlPullParser.START_TAG){

                }else if (eventType==XmlPullParser.END_TAG){
                    String endtagname = pullparser.getName();
                    if(endtagname.equals(FAILTFLOWST)){
                        END_FAULTFLOW = true;
                        break;
                    }
                }else if (eventType==XmlPullParser.CDSECT){
                    //do nothing
                }else if (eventType==XmlPullParser.COMMENT){
                    // do nothing
                }else if (eventType==XmlPullParser.TEXT){
                    text = text + pullparser.getText();
                }else{
                    throw new UnsupportedOperationException();
                    //any other events are not interesting :)
                }
            }
        } catch (XmlPullParserException e) {
            throw new DeploymentException("parser Exception",e);
        } catch (IOException e) {
            throw new DeploymentException("IO Exception",e);
        } catch (Exception e) {
            throw new DeploymentException("Unknown process Exception",e);
        }

        return faultFlow;
    }




    /**
     * this method is to get the value of attribue
     * eg xsd:anyVal --> anyVal
     * @return
     */
    private String getValue(String in){
        char seperator = ':';
        String value = null;
        int index = in.indexOf(seperator);
        if(index > 0 ){
            value = in.substring(index+1,in.length());
            return value;
        }
        return in;
    }

    /**
     * to process either module.xml or module elemnt in the service.xml
     */
    private void procesModuleXML(){

    }



}
