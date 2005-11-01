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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.FlowImpl;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * To do the common tasks for all *Builder class
 */
public class DescriptionBuilder implements DeploymentConstants{

    protected Log log = LogFactory.getLog(getClass());
    protected InputStream des_inputStream;
    protected DeploymentEngine engine;

    public DescriptionBuilder(InputStream serviceInputStream, DeploymentEngine engine) {
        this.des_inputStream = serviceInputStream;
        this.engine = engine;
    }

    public DescriptionBuilder() {
    }

    /**
     * This will creat OMElemnt for a given descrition document (axis2.xml , services.xml and
     * module.xml)
     *
     * @return OMElement <code>OMElement</code>
     * @throws javax.xml.stream.XMLStreamException
     */
    public OMElement buildOM() throws XMLStreamException {
        XMLStreamReader xmlReader =
                XMLInputFactory.newInstance().createXMLStreamReader(des_inputStream);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        StAXOMBuilder staxOMBuilder = new StAXOMBuilder(fac, xmlReader);
        OMElement element = staxOMBuilder.getDocumentElement();
        element.build();
        return element;
    }


    /**
     * To process Flow elements in services.xml
     * @param flowelement       <code>OMElement</code>
     * @return
     * @throws DeploymentException  <code>DeploymentException</code>
     */
    protected Flow processFlow(OMElement flowelement, ParameterInclude parent) throws DeploymentException {
        Flow flow = new FlowImpl();
        if(flowelement == null){
            return flow;
        }
        Iterator handlers = flowelement.getChildrenWithName(
                new QName(HANDERST));
        while (handlers.hasNext()) {
            OMElement  handlerElement =  (OMElement)handlers.next();
            flow.addHandler(processHandler(handlerElement,parent));
        }
        return flow;
    }

    /**
     *  To process Handler element
     * @param handler_element    <code>OMElement</code>
     * @return
     * @throws DeploymentException    <code>DeploymentException</code>
     */
    protected HandlerDescription processHandler(OMElement handler_element, ParameterInclude parent)
            throws DeploymentException {
        HandlerDescription handler = new HandlerDescription();

        //Setting Handler name
        OMAttribute name_attribute = handler_element.getAttribute(
                new QName(ATTNAME));
        if(name_attribute == null){
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.INVALID_HANDLER,"Name missing"));
        } else {
            handler.setName(new QName(name_attribute.getAttributeValue()));
        }

        //Setting Handler Class name
        OMAttribute class_attribute = handler_element.getAttribute(
                new QName(CLASSNAME));
        if(class_attribute == null){
            throw new DeploymentException((Messages.getMessage(
                    DeploymentErrorMsgs.INVALID_HANDLER,"class name missing")));
        } else {
            handler.setClassName(class_attribute.getAttributeValue());
        }

        //processing phase Rules (order)
        OMElement order_element = handler_element.getFirstChildWithName(
                new QName(ORDER));
        if(order_element == null){
            throw new DeploymentException((Messages.getMessage(
                    DeploymentErrorMsgs.INVALID_HANDLER,"phase rule does not specify")));
        } else {
            Iterator order_itr = order_element.getAllAttributes();
            while (order_itr.hasNext()) {
                OMAttribute orderAttribute = (OMAttribute) order_itr.next();
                String name  = orderAttribute.getQName().getLocalPart();
                String value = orderAttribute.getAttributeValue();
                if (AFTER.equals(name)) {
                    handler.getRules().setAfter(value);
                } else if (BEFORE.equals(name)) {
                    handler.getRules().setBefore(value);
                } else if (PHASE.equals(name)) {
                    handler.getRules().setPhaseName(value);
                } else if (PHASEFIRST.equals(name)) {
                    String boolval = getValue(value);
                    if (boolval.equals("true")) {
                        handler.getRules().setPhaseFirst(true);
                    } else if (boolval.equals("false")) {
                        handler.getRules().setPhaseFirst(false);
                    }
                } else if (PHASELAST.equals(name)) {
                    String boolval = getValue(value);
                    if (boolval.equals("true")) {
                        handler.getRules().setPhaseLast(true);
                    } else if (boolval.equals("false")) {
                        handler.getRules().setPhaseLast(false);
                    }
                }

            }
            Iterator parameters = handler_element.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(parameters,handler,parent);
        }
        handler.setParent(parent);
        return handler;
    }

    /**
     * To get the Paramter object out from the OM
     * @param parameters <code>Parameter</code>
     * @param parameterInclude <code>ParameterInclude</code>
     * @param parent <code>ParameterInclude</code>
     * return : will retuen paramters , wchih name is WSA-Mapping , since we need to treat them
     * seperately
     */
    protected ArrayList processParameters(Iterator parameters, ParameterInclude parameterInclude,
                                          ParameterInclude parent )
            throws DeploymentException {
        ArrayList wsamapping = new ArrayList();
        while (parameters.hasNext()) {
            //this is to check whether some one has locked the parmter at the top level
            OMElement parameterElement = (OMElement) parameters.next();

            Parameter parameter = new ParameterImpl();
            //setting parameterElement
            parameter.setParameterElement(parameterElement);

            //setting parameter Name
            OMAttribute paraName = parameterElement.getAttribute(
                    new QName(ATTNAME));
            if(paraName == null ){
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.BAD_PARA_ARGU));
            }
            parameter.setName(paraName.getAttributeValue());

            //setting paramter Value (the chiled elemnt of the paramter)
            OMElement paraValue = parameterElement.getFirstElement();
            if(paraValue !=null){
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.DOM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();
                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }

            //setting locking attribute
            OMAttribute paraLocked = parameterElement.getAttribute(
                    new QName(ATTLOCKED));
            Parameter parentpara = null;
            if (parent !=null) {
                parentpara = parent.getParameter(parameter.getName());
            }
            if (paraLocked !=null) {
                String lockedValue = paraLocked.getAttributeValue();
                if("true".equals(lockedValue)){
                    //if the parameter is locked at some levle paramer value replace by that
                    if(parent!=null && parent.isParameterLocked(parameter.getName())){
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.CONFIG_NOT_FOUND,parameter.getName()));
                    } else{
                        parameter.setLocked(true);
                    }

                } else {
                    parameter.setLocked(false);
                }
            }
            if(Constants.WSA_ACTION.equals(paraName.getAttributeValue())){
                wsamapping.add(parameter);
                // no need to add this paramter , since this is just for mapping
                continue;
            }
            try {
                if(parent !=null){
                    if(parentpara == null | !parent.isParameterLocked(parameter.getName())){
                        parameterInclude.addParameter(parameter);
                    }
                } else {
                    parameterInclude.addParameter(parameter);
                }
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
        }
        return wsamapping;
    }


    protected void processOperationModuleRefs(Iterator moduleRefs
            , AxisOperation opeartion) throws DeploymentException {
        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(
                        new QName(REF));
                if (moduleRefAttribute !=null) {
                    String refName = moduleRefAttribute.getAttributeValue();
                    if(engine.getModule(new QName(refName)) == null) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.MODEULE_NOT_FOUND, refName));
                    } else {
                        opeartion.addModule(new QName(refName));
                    }
                }
            }
        }catch (AxisFault axisFault) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.MODEULE_NOT_FOUND, axisFault.getMessage()));
        }
    }

    protected MessageReceiver loadMessageReceiver(ClassLoader loader , OMElement reciverElement)
            throws DeploymentException {
        OMAttribute recieverName = reciverElement.getAttribute(
                new QName(CLASSNAME));
        String className = recieverName.getAttributeValue();
        MessageReceiver receiver = null;
        try{
            Class messageReceiver ;
            if (className != null &&!"".equals(className)) {
                messageReceiver = Class.forName(className,true,loader);
                receiver = (MessageReceiver) messageReceiver.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MR,"ClassNotFoundException", className));
        } catch (IllegalAccessException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MR,"IllegalAccessException", className));
        } catch (InstantiationException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MR,"InstantiationException", className));
        }
        return receiver;
    }

    protected MessageReceiver loadDefaultMessageReceiver() throws DeploymentException {
        MessageReceiver receiver;
        String defaultMessageReciver ="org.apache.axis2.receivers.RawXMLINOutMessageReceiver";
        try {
            /**
             * Setting default Message Recive as Message Receiver
             */
            ClassLoader loader1 = Thread.currentThread()
                    .getContextClassLoader();
            Class messageReceiver =
                    Class.forName(defaultMessageReciver,true,loader1);
            receiver = ((MessageReceiver) messageReceiver.newInstance());
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.ERROR_IN_LOADING_MR,
                    "ClassNotFoundException",defaultMessageReciver));
        } catch (IllegalAccessException e) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.ERROR_IN_LOADING_MR,
                    "IllegalAccessException",defaultMessageReciver));
        } catch (InstantiationException e) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.ERROR_IN_LOADING_MR,
                    "InstantiationException",defaultMessageReciver));
        }
        return receiver;
    }


    /**
     * This method is used to retrive service name form the arechive file name
     * if the archive file name is service1.aar , then axis service name would be service1
     *
     * @param fileName
     * @return String
     */
    public static String getShortFileName(String fileName) {
        char seperator = '.';
        String value;
        int index = fileName.lastIndexOf(seperator);
        if (index > 0) {
            value = fileName.substring(0, index);
            return value;
        }
        return fileName;
    }


    /**
     * this method is to get the value of attribue
     * eg xsd:anyVal --> anyVal
     *
     * @return String
     */
    protected String getValue(String in) {
        char seperator = ':';
        String value ;
        int index = in.indexOf(seperator);
        if (index > 0) {
            value = in.substring(index + 1, in.length());
            return value;
        }
        return in;
    }



}
