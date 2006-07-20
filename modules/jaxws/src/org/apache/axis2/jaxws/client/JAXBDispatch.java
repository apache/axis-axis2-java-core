/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.client;

import javax.xml.bind.JAXBContext;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.AxisController;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.param.JAXBParameter;
import org.apache.axis2.jaxws.param.Parameter;

public class JAXBDispatch<T> extends BaseDispatch<T> {

    private JAXBContext jaxbContext;
    
    public JAXBDispatch() {
        //do nothing
    }
    
    public JAXBDispatch(AxisController ac) {
        super(ac);
    }
    
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }
    
    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }
    
    public AsyncListener createAsyncListener() {
        JAXBDispatchAsyncListener listener = new JAXBDispatchAsyncListener();
        listener.setJAXBContext(jaxbContext);
        listener.setMode(mode);
        return listener;
    }
    
    public OMElement createMessageFromValue(Object value) {
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        JAXBParameter param = new JAXBParameter();
        param.setValue(value);
        param.setJAXBContext(jaxbContext);
        
        OMElement envelope = toOM(param, 
                axisController.getServiceClient().getOptions().getSoapVersionURI());
        return envelope;
    }

    public Object getValueFromMessage(OMElement message) {
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        JAXBParameter param = new JAXBParameter();
        param.setJAXBContext(jaxbContext);
        Parameter p = fromOM(message, param, 
                axisController.getServiceClient().getOptions().getSoapVersionURI());
        return p.getValue();
    }
}
