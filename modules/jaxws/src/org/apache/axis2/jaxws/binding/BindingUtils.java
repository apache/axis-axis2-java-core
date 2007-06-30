/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.binding;

import javax.xml.ws.Binding;

import org.apache.axis2.jaxws.description.EndpointDescription;

public class BindingUtils {

    /**
     * Creates a Binding instance based on an EndpointDescription.
     * @param ed
     * @return
     */
    public static Binding createBinding(EndpointDescription ed) {
        if (ed == null) {
            // Do we default to the SOAPBinding?            
        }
        
        String bindingType = ed.getBindingType();
        if (BindingUtils.isSOAPBinding(bindingType)) {
            return new SOAPBinding(ed);
        }
        else if (BindingUtils.isHTTPBinding(bindingType)) { 
            return new HTTPBinding(ed);
        }
        else {
            // If we can't figure it out, let's default to 
            // a SOAPBinding
            return new SOAPBinding(ed);
        }
    }
    
    public static boolean isSOAPBinding(String url) {
        if (url != null && (url.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                url.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                url.equals(SOAPBinding.SOAP12HTTP_BINDING)|| 
                url.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING))) {
            return true;
        }
        return false;
    }
    
    public static boolean isHTTPBinding(String url) {
        if (url != null && url.equals(HTTPBinding.HTTP_BINDING)) {
            return true;
        }
        return false;
    }
}