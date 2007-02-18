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
package org.apache.axis2.jaxws.client.dispatch;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Service.Mode;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * The JAXBDispatchAsyncListener is an extension of the  
 * {@link org.apache.axis2.jaxws.client.async.AsyncResponse} class to provide JAX-B
 * specific function when processing an async response.
 */
public class JAXBDispatchAsyncListener extends AsyncResponse {
    
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
        return JAXBDispatch.getValue(mc.getMessage(), mode, jaxbContext);
    }
    
    public Throwable getFaultResponse(MessageContext mc) {
        return BaseDispatch.getFaultResponse(mc);
    }
}
