/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.impl.description;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.ParameterInclude;
import org.apache.wsdl.wom.MessageReference;
import org.apache.wsdl.wom.WSDLFeature;
import org.apache.wsdl.wom.WSDLOperation;
import org.apache.wsdl.wom.WSDLProperty;
import org.apache.wsdl.wom.impl.WSDLOperationImpl;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class SimpleAxisOperationImpl implements AxisOperation {
    protected WSDLOperation wsdlOperation;
    protected ParameterInclude parameters;
    
    /**
     * 
     */
    public SimpleAxisOperationImpl(QName name) {
        wsdlOperation = new WSDLOperationImpl();
        wsdlOperation.setName(name);
        parameters = new ParameterIncludeImpl();
    }
    /**
     * @param param
     */
    public void addParameter(Parameter param) {
        parameters.addParameter(param);
    }

    /**
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return parameters.getParameter(name);
    }

    /**
     * @param feature
     */
    public void addFeature(WSDLFeature feature) {
        wsdlOperation.addFeature(feature);
    }

    /**
     * @param wsdlProperty
     */
    public void addPorperty(WSDLProperty wsdlProperty) {
        wsdlOperation.addPorperty(wsdlProperty);
    }

    /**
     * @return
     */
    public HashMap getComponentProperties() {
        return wsdlOperation.getComponentProperties();
    }

    /**
     * @param key
     * @return
     */
    public Object getComponentProperty(Object key) {
        return wsdlOperation.getComponentProperty(key);
    }

    /**
     * @return
     */
    public List getFeatures() {
        return wsdlOperation.getFeatures();
    }

    /**
     * @return
     */
    public List getInfaults() {
        return wsdlOperation.getInfaults();
    }

    /**
     * @return
     */
    public MessageReference getInputMessage() {
        return wsdlOperation.getInputMessage();
    }

    /**
     * @return
     */
    public int getMessageExchangePattern() {
        return wsdlOperation.getMessageExchangePattern();
    }

    /**
     * @return
     */
    public QName getName() {
        return wsdlOperation.getName();
    }

    /**
     * @return
     */
    public List getOutfaults() {
        return wsdlOperation.getOutfaults();
    }

    /**
     * @return
     */
    public MessageReference getOutputMessage() {
        return wsdlOperation.getOutputMessage();
    }

    /**
     * @return
     */
    public List getProperties() {
        return wsdlOperation.getProperties();
    }

    /**
     * @return
     */
    public int getStyle() {
        return wsdlOperation.getStyle();
    }

    /**
     * @return
     */
    public URI getTargetnemespace() {
        return wsdlOperation.getTargetnemespace();
    }

    /**
     * @return
     */
    public boolean isSafe() {
        return wsdlOperation.isSafe();
    }

    /**
     * @param properties
     */
    public void setComponentProperties(HashMap properties) {
        wsdlOperation.setComponentProperties(properties);
    }

    /**
     * @param key
     * @param obj
     */
    public void setComponentProperty(Object key, Object obj) {
        wsdlOperation.setComponentProperty(key, obj);
    }

    /**
     * @param infaults
     */
    public void setInfaults(List infaults) {
        wsdlOperation.setInfaults(infaults);
    }

    /**
     * @param inputMessage
     */
    public void setInputMessage(MessageReference inputMessage) {
        wsdlOperation.setInputMessage(inputMessage);
    }

    /**
     * @param messageExchangePattern
     */
    public void setMessageExchangePattern(int messageExchangePattern) {
        wsdlOperation.setMessageExchangePattern(messageExchangePattern);
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        wsdlOperation.setName(name);
    }

    /**
     * @param outfaults
     */
    public void setOutfaults(List outfaults) {
        wsdlOperation.setOutfaults(outfaults);
    }

    /**
     * @param outputMessage
     */
    public void setOutputMessage(MessageReference outputMessage) {
        wsdlOperation.setOutputMessage(outputMessage);
    }

    /**
     * @param safe
     */
    public void setSafety(boolean safe) {
        wsdlOperation.setSafety(safe);
    }

    /**
     * @param style
     */
    public void setStyle(int style) {
        wsdlOperation.setStyle(style);
    }

    /**
     * @param targetnemespace
     */
    public void setTargetnemespace(URI targetnemespace) {
        wsdlOperation.setTargetnemespace(targetnemespace);
    }

}
