/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.jaxws.binding;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author rott classes that would normally "implement javax.xml.ws.Binding"
 *         should extend this class instead.
 */
public class BindingImpl implements Binding {

    // an unsorted list of handlers
    private List<Handler> handlers = null;

    protected String bindingId = null;

    private EndpointDescription endpointDesc;

    protected Set<URI> roles = null;

    protected static final String SOAP11_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    protected static final String SOAP12_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";

    public BindingImpl(EndpointDescription endpointDesc) {
        this.endpointDesc = endpointDesc;
        this.bindingId = endpointDesc.getBindingType();
    }

    public List<Handler> getHandlerChain() {
        if (handlers == null) {
            handlers = new HandlerResolverImpl(endpointDesc).getHandlerChain(endpointDesc
                            .getPortInfo());
            if (handlers == null)
                handlers = new ArrayList<Handler>(); // non-null so client
                                                        // apps can manipulate
        }
        return handlers;
    }

    public void setHandlerChain(List<Handler> list) {
        // handlers cannot be null so a client app can request and manipulate it
        if (list == null)
            handlers = new ArrayList<Handler>(); // non-null, but rather
                                                    // empty
        else
            this.handlers = list;
    }

}
