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

package org.apache.axis2.jaxws;

import java.util.Hashtable;
import java.util.Map;

import javax.xml.ws.Binding;

import org.apache.axis2.jaxws.binding.SOAPBinding;

public class BindingProvider implements javax.xml.ws.BindingProvider {

	protected Map<String, Object> requestContext;
    protected Map<String, Object> responseContext;
    protected Binding binding;
    
    public BindingProvider() {
        requestContext = new Hashtable<String,Object>();
        responseContext = new Hashtable<String,Object>();
        
        //Setting standard property defaults for request context
        requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, new Boolean(false));
        requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, new Boolean(false));
        requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, "");
        
        //The default Binding is the SOAPBinding
        binding = new SOAPBinding();
    }
    
    public Binding getBinding() {
        return binding;
    }

    public Map<String, Object> getRequestContext() {
        return requestContext;
    }

    public Map<String, Object> getResponseContext() {
        return responseContext;
    }
    
    protected void initRequestContext(String endPointAddress, String soapAddress, String soapAction){
    	if (endPointAddress != null && !"".equals(endPointAddress)) {
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endPointAddress);
		} else if (soapAddress != null && !"".equals(soapAddress)) {
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					soapAddress);
		}
		if (soapAction != null && !"".equals(soapAction)) {
			getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,
					soapAction);
		}
    }
}
