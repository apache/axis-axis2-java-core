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
import javax.xml.ws.Service.Mode;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.param.JAXBParameter;
import org.apache.axis2.jaxws.param.ParameterUtils;

/**
 * The JAXBDispatchAsyncListener is an extension of the  
 * {@link org.apache.axis2.jaxws.impl.AsyncListener} class to provide JAX-B
 * specific function when processing an async response.
 */
public class JAXBDispatchAsyncListener extends AsyncListener {
    
    private Mode mode;
    private JAXBContext jaxbContext;
    
    public JAXBDispatchAsyncListener() {
        super();
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }
    
    public Object getResponseValueObject(MessageContext mc) {
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        SOAPEnvelope msg = (SOAPEnvelope) mc.getMessageAsOM();
        
        JAXBParameter param = new JAXBParameter();
        param.setJAXBContext(jaxbContext);
        ParameterUtils.fromEnvelope(mode, msg, param);
        
        return param.getValue();
    }
}
