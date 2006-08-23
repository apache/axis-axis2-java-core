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
package org.apache.axis2.jaxws.client.factory;

import javax.jws.soap.SOAPBinding.Style;


import org.apache.axis2.jaxws.client.proxy.BaseProxyHandler;
import org.apache.axis2.jaxws.client.proxy.DocLitProxyHandler;
import org.apache.axis2.jaxws.client.proxy.ProxyDescriptor;
import org.apache.axis2.jaxws.client.proxy.RPCLitProxyHandler;
import org.apache.axis2.jaxws.spi.ServiceDelegate;


/**
 * ProxyHandler Factory looks at proxy descriptor object and create a doc/lit or rpc/lit proxy handler. 
 */
public class ProxyHandlerFactory {

	public BaseProxyHandler create(ProxyDescriptor descriptor, ServiceDelegate sd){
		if(descriptor.getBindingStyle() == Style.DOCUMENT){
			return new DocLitProxyHandler(descriptor, sd);
		}
		if(descriptor.getBindingStyle()== Style.RPC){
			return new RPCLitProxyHandler(descriptor, sd);
		}
		return null;
	}
}
