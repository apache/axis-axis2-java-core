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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;

public class Proxies extends BindingProvider implements InvocationHandler {
	private AxisService axisService = null;
	 private ServiceClient serviceClient = null;
	 private AxisController router = null;
	public Proxies(AxisController router){
		super();
		this.router = router;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
	
	 public void setAxisService(AxisService svc) {
	        axisService = svc;
	    }
	    
	    public void setServiceClient(ServiceClient svcClt) {
	        serviceClient = svcClt;
	    }

}
