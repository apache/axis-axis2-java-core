package org.apache.axis2.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.AxisFault;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Iterator;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Aug 26, 2005
 * Time: 3:12:06 PM
 */

/**
 * To do the common tasks for all *Builder class
 */
public class DescriptionBuilder implements DeploymentConstants{

    protected Log log = LogFactory.getLog(getClass());
    protected InputStream serviceInputSteram;
    protected DeploymentEngine engine;

    public DescriptionBuilder(InputStream serviceInputSteram, DeploymentEngine engine) {
        this.serviceInputSteram = serviceInputSteram;
        this.engine = engine;
    }

    public DescriptionBuilder() {
    }

    /**
     * This will creat OMElemnt for a given service.xml
     *
     * @return OMElement <code>OMElement</code>
     * @throws javax.xml.stream.XMLStreamException
     */
    public OMElement buildOM() throws XMLStreamException {
        XMLStreamReader xmlReader =
                XMLInputFactory.newInstance().createXMLStreamReader(serviceInputSteram);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        StAXOMBuilder staxOMBuilder = new StAXOMBuilder(fac, xmlReader);
        OMElement element = staxOMBuilder.getDocumentElement();
        element.build();
        return element;
    }


    /**
     * To process Flow elements in service.xml
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
            throw new DeploymentException("Invalid Handler");
        } else {
            handler.setName(new QName(name_attribute.getValue()));
        }

        //Setting Handler Class name
        OMAttribute class_attribute = handler_element.getAttribute(
                new QName(CLASSNAME));
        if(class_attribute == null){
            throw new DeploymentException("Invalid Handler");
        } else {
            handler.setClassName(class_attribute.getValue());
        }

        //processing phase Rules (order)
        OMElement order_element = handler_element.getFirstChildWithName(
                new QName(ORDER));
        if(order_element == null){
            throw new DeploymentException("Invaid Handler , phase rule does not specify");
        } else {
            Iterator order_itr = order_element.getAttributes();
            while (order_itr.hasNext()) {
                OMAttribute orderAttribut = (OMAttribute) order_itr.next();
                String name  = orderAttribut.getQName().getLocalPart();
                String value = orderAttribut.getValue();
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
            Iterator paramters = handler_element.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(paramters,handler,parent);
        }
        handler.setParent(parent);
        return handler;
    }


    /**
     * To get the list og modules that is requird to be engage globally
     * @param moduleRefs  <code>java.util.Iterator</code>
     * @throws DeploymentException   <code>DeploymentException</code>
     */
//    protected void processModuleRefs(Iterator moduleRefs) throws DeploymentException {
//        try {
//            while (moduleRefs.hasNext()) {
//                OMElement moduleref = (OMElement) moduleRefs.next();
//                OMAttribute moduleRefAttribute = moduleref.getAttribute(
//                        new QName(REF));
//                if(moduleRefAttribute !=null){
//                    String refName = moduleRefAttribute.getValue();
//                    if(engine.getModule(new QName(refName)) == null) {
//                        throw new DeploymentException(Messages.getMessage(
//                                DeploymentErrorMsgs.MODEULE_NOT_FOUND, refName));
//                    } else {
//                        engine.getCurrentFileItem().addModule(new QName(refName));
//                    }
//                }
//            }
//        }catch (AxisFault axisFault) {
//            throw   new DeploymentException(axisFault);
//        }
//    }


    /**
     * To get the Paramter object out from the OM
     * @param paramters <code>Parameter</code>
     * @param parameterInclude <code>ParameterInclude</code>
     * @param parent <code>ParameterInclude</code>
     */
    protected void processParameters(Iterator paramters, ParameterInclude parameterInclude,
                                     ParameterInclude parent )
            throws DeploymentException {
        while (paramters.hasNext()) {
            //this is to check whether some one has locked the parmter at the top level
            OMElement paramterElement = (OMElement) paramters.next();

            Parameter paramter = new ParameterImpl();
            //setting paramterElement
            paramter.setParameterElement(paramterElement);

            //setting paramter Name
            OMAttribute paraName = paramterElement.getAttribute(
                    new QName(ATTNAME));
            if(paraName == null ){
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.BAD_PARA_ARGU));
            }
            paramter.setName(paraName.getValue());

            //setting paramter Value (the chiled elemnt of the paramter)
            OMElement paraValue = paramterElement.getFirstElement();
            if(paraValue !=null){
                paramter.setValue(paramterElement);
                paramter.setParamterType(Parameter.DOM_PARAMETER);
            } else {
                String paratestValue = paramterElement.getText();
                paramter.setValue(paratestValue);
                paramter.setParamterType(Parameter.TEXT_PARAMETER);
            }

            //setting locking attribute
            OMAttribute paraLocked = paramterElement.getAttribute(
                    new QName(ATTLOCKED));
            if (paraLocked !=null) {
                String lockedValue = paraLocked.getValue();
                if("true".equals(lockedValue)){
                    //if the parameter is locked at some levle paramer value replace by that
                    if(parent.isParamterLocked(paramter.getName())){
                        throw new DeploymentException("The paramter " + paramter.getName() + " has" +
                                " locked at top levle can not overide");
                    } else{
                        paramter.setLocked(true);
                    }

                } else {
                    paramter.setLocked(false);
                }
            }
            try {
                parameterInclude.addParameter(paramter);
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
        }
    }


    protected void processOpeasrtionModuleRefs(Iterator moduleRefs
            , OperationDescription opeartion) throws DeploymentException {
        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(
                        new QName(REF));
                if (moduleRefAttribute !=null) {
                    String refName = moduleRefAttribute.getValue();
                    if(engine.getModule(new QName(refName)) == null) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.MODEULE_NOT_FOUND, refName));
                    } else {
                        opeartion.addModule(new QName(refName));
                    }
                }
            }
        }catch (AxisFault axisFault) {
            throw   new DeploymentException("Porcessing Operations Modules" + axisFault);
        }
    }

    protected MessageReceiver loadMessageReceiver(ClassLoader loader , OMElement reciverElement)
            throws DeploymentException {
        OMAttribute recieverName = reciverElement.getAttribute(
                new QName(CLASSNAME));
        String className = recieverName.getValue();
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

    protected MessageReceiver loadDefaultMessageReciver() throws DeploymentException {
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
