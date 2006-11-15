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
package org.apache.axis2.jaxws.client.proxy;

import java.lang.reflect.Method;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RPCLitProxyHandler extends BaseProxyHandler {
	private static Log log = LogFactory.getLog(RPCLitProxyHandler.class);

	/**
	 * @param pd
	 * @param delegate
	 */
	public RPCLitProxyHandler(ProxyDescriptor pd, ServiceDelegate delegate) {
		super(pd, delegate);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.client.proxy.BaseProxyHandler#createRequest(java.lang.reflect.Method, java.lang.Object)
	 */
	@Override
	protected MessageContext createRequest(Method method, Object[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.client.proxy.BaseProxyHandler#createResponse(java.lang.reflect.Method, org.apache.axis2.jaxws.core.MessageContext)
	 */
	@Override
	protected Object createResponse(Method method,
			MessageContext responseContext) {
		// TODO Auto-generated method stub
		return null;
	}

}
